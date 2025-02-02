package quek.undergarden.item.tool;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import quek.undergarden.registry.UGItemGroups;
import quek.undergarden.registry.UGItems;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.item.Item.Properties;

@Mod.EventBusSubscriber
public class UGShovelItem extends ShovelItem {
    public UGShovelItem(IItemTier tier) {
        super(tier, 1.5F, -3, new Properties()
                .stacksTo(1)
                .defaultDurability(tier.getUses())
                .tab(UGItemGroups.GROUP)
                .rarity(UGSwordItem.isForgotten(tier))
        );
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if(stack.getItem() == UGItems.FROSTSTEEL_SHOVEL.get()) {
            tooltip.add(new TranslationTextComponent("tooltip.froststeel_sword").withStyle(TextFormatting.GRAY));
        }
    }

    @SubscribeEvent
    public static void attackEvent(LivingHurtEvent event) {
        Entity source = event.getSource().getEntity();

        if(source instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) source;

            if(player.getMainHandItem().getItem() == UGItems.FROSTSTEEL_SHOVEL.get()) {
                event.getEntityLiving().addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 600, 2));
            }
        }
    }
}