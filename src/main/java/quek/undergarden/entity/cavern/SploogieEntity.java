package quek.undergarden.entity.cavern;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import quek.undergarden.entity.projectile.SlingshotAmmoEntity;

public class SploogieEntity extends AbstractCavernCreatureEntity implements IRangedAttackMob {

    public SploogieEntity(EntityType<? extends MonsterEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(0, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0D, 20, 15.0F));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, false));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        return AbstractCavernCreatureEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        SlingshotAmmoEntity pebble = new SlingshotAmmoEntity(this.level, this);
        double xDistance = target.getX() - this.getX();
        double yDistance = target.getY(0.3333333333333333D) - pebble.getY();
        double zDistance = target.getZ() - this.getZ();
        double yMath = MathHelper.sqrt((xDistance * xDistance) + (zDistance * zDistance));
        pebble.shoot(xDistance, yDistance + yMath * 0.1D, zDistance, 1.6F, 1.0F);
        this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level.addFreshEntity(pebble);
    }
}