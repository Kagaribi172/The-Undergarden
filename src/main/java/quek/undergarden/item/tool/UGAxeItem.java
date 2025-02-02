package quek.undergarden.item.tool;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
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
import quek.undergarden.registry.UGTags;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.item.Item.Properties;

@Mod.EventBusSubscriber
public class UGAxeItem extends AxeItem {
    public UGAxeItem(IItemTier tier, float attack, float speed) {
        super(tier, attack, speed, new Properties()
                .stacksTo(1)
                .defaultDurability(tier.getUses())
                .tab(UGItemGroups.GROUP)
                .rarity(UGSwordItem.isForgotten(tier))
        );
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if(stack.getItem() == UGItems.UTHERIUM_AXE.get()) {
            tooltip.add(new TranslationTextComponent("tooltip.utheric_sword").withStyle(TextFormatting.GRAY));
        }
        if(stack.getItem() == UGItems.FROSTSTEEL_AXE.get()) {
            tooltip.add(new TranslationTextComponent("tooltip.froststeel_sword").withStyle(TextFormatting.GRAY));
        }
        if(stack.getItem() == UGItems.FORGOTTEN_AXE.get()) {
            tooltip.add(new TranslationTextComponent("tooltip.forgotten_sword").withStyle(TextFormatting.GRAY));
        }
    }

    @SubscribeEvent
    public static void attackEvent(LivingHurtEvent event) {
        Entity source = event.getSource().getEntity();
        float damage = event.getAmount();

        if(source instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) source;
            if(player.getMainHandItem().getItem() == UGItems.UTHERIUM_AXE.get()) {
                if(event.getEntityLiving().getType().is(UGTags.Entities.ROTSPAWN)) {
                    event.setAmount(damage * 1.5F);
                }
                else event.setAmount(damage);
            }
            else if(player.getMainHandItem().getItem() == UGItems.FROSTSTEEL_AXE.get()) {
                event.getEntityLiving().addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 600, 3));
            }
            else if(player.getMainHandItem().getItem() == UGItems.FORGOTTEN_AXE.get()) {
                if(event.getEntityLiving().getType().getRegistryName().getNamespace().equals("undergarden") && event.getEntityLiving().canChangeDimensions()) {
                    event.setAmount(damage * 2F);
                }
                else event.setAmount(damage);
            }
        }
    }
}