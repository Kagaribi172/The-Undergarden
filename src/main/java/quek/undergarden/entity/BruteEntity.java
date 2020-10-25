package quek.undergarden.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.TickRangeConverter;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import quek.undergarden.registry.UGBlocks;
import quek.undergarden.registry.UGSounds;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public class BruteEntity extends MonsterEntity implements IAngerable {

    private static final UUID field_234344_b_ = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
    private static final AttributeModifier field_234349_c_ = new AttributeModifier(field_234344_b_, "Attacking speed boost", 0.05D, AttributeModifier.Operation.ADDITION);
    private static final RangedInteger field_234350_d_ = TickRangeConverter.convertRange(0, 1);
    private int angerLevel;
    private static final RangedInteger field_234346_bv_ = TickRangeConverter.convertRange(20, 39);
    private int field_234347_bw_;
    private UUID field_234348_bx_;
    private static final RangedInteger field_241403_bz_ = TickRangeConverter.convertRange(4, 6);
    private int field_241401_bA_;

    public BruteEntity(EntityType<? extends BruteEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0F, false));
        this.targetSelector.addGoal(0, (new HurtByTargetGoal(this)));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(6, new LookAtGoal(this, BruteEntity.class, 6.0F));
        this.goalSelector.addGoal(7, new LookRandomlyGoal(this));
    }

    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        return MobEntity.func_233666_p_()
                .createMutableAttribute(Attributes.MAX_HEALTH, 20.0D)
                .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.23D)
                .createMutableAttribute(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    public static boolean canBruteSpawn(EntityType<? extends MonsterEntity> animal, IWorld worldIn, SpawnReason reason, BlockPos pos, Random random) {
        return worldIn.getBlockState(pos.down()).getBlock() == UGBlocks.deepturf_block.get();
    }

    @Override
    protected boolean isDespawnPeaceful() {
        return false;
    }

    @Override
    public int getAngerTime() {
        return this.field_234347_bw_;
    }

    @Override
    public void setAngerTime(int time) {
        this.field_234347_bw_ = time;
    }

    @Nullable
    @Override
    public UUID getAngerTarget() {
        return this.field_234348_bx_;
    }

    @Override
    public void setAngerTarget(@Nullable UUID target) {
        this.field_234348_bx_ = target;
    }

    @Override
    public void func_230258_H__() {
        this.setAngerTime(field_234346_bv_.getRandomWithinRange(this.rand));
    }

    @Override
    public void setRevengeTarget(@Nullable LivingEntity livingBase) {
        super.setRevengeTarget(livingBase);
        if (livingBase != null) {
            this.field_234348_bx_ = livingBase.getUniqueID();
        }

    }

    @Override
    protected void updateAITasks() {
        ModifiableAttributeInstance modifiableattributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (this.func_233678_J__()) {
            if (!this.isChild() && !modifiableattributeinstance.hasModifier(field_234349_c_)) {
                modifiableattributeinstance.applyNonPersistentModifier(field_234349_c_);
            }

            this.func_241409_eY_();
        } else if (modifiableattributeinstance.hasModifier(field_234349_c_)) {
            modifiableattributeinstance.removeModifier(field_234349_c_);
        }

        this.func_241359_a_((ServerWorld)this.world, true);
        if (this.getAttackTarget() != null) {
            this.func_241410_eZ_();
        }

        if (this.func_233678_J__()) {
            this.recentlyHit = this.ticksExisted;
        }

        super.updateAITasks();
    }

    private void func_241409_eY_() {
        if (this.angerLevel > 0) {
            --this.angerLevel;
            if (this.angerLevel == 0) {
                this.angrySound();
            }
        }

    }

    private void angrySound() {
        this.playSound(UGSounds.BRUTE_ANGRY, this.getSoundVolume() * 2.0F, this.getSoundPitch() * 1.8F);
    }

    private void func_241410_eZ_() {
        if (this.field_241401_bA_ > 0) {
            --this.field_241401_bA_;
        } else {
            if (this.getEntitySenses().canSee(this.getAttackTarget())) {
                this.func_241411_fa_();
            }

            this.field_241401_bA_ = field_241403_bz_.getRandomWithinRange(this.rand);
        }
    }

    private void func_241411_fa_() {
        double d0 = this.getAttributeValue(Attributes.MOVEMENT_SPEED);
        AxisAlignedBB axisalignedbb = AxisAlignedBB.fromVector(this.getPositionVec()).grow(d0, 10.0D, d0);
        this.world.getLoadedEntitiesWithinAABB(BruteEntity.class, axisalignedbb).stream().filter((brute) -> brute != this).filter((brute) -> brute.getAttackTarget() == null).filter((brute) -> !brute.isOnSameTeam(this.getAttackTarget())).forEach((brute) -> brute.setAttackTarget(this.getAttackTarget()));
    }

    public void setAttackTarget(@Nullable LivingEntity entitylivingbaseIn) {
        if (this.getAttackTarget() == null && entitylivingbaseIn != null) {
            this.angerLevel = field_234350_d_.getRandomWithinRange(this.rand);
            this.field_241401_bA_ = field_241403_bz_.getRandomWithinRange(this.rand);
        }

        if (entitylivingbaseIn instanceof PlayerEntity) {
            this.func_230246_e_((PlayerEntity)entitylivingbaseIn);
        }

        super.setAttackTarget(entitylivingbaseIn);
    }

    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        this.writeAngerNBT(compound);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.readAngerNBT((ServerWorld)this.world, compound);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return UGSounds.BRUTE_LIVING;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return UGSounds.BRUTE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return UGSounds.BRUTE_DEATH;
    }
}


