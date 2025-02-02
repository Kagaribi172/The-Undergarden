package quek.undergarden.item.tool;

import net.minecraft.item.HoeItem;
import net.minecraft.item.IItemTier;
import quek.undergarden.registry.UGItemGroups;

import net.minecraft.item.Item.Properties;

public class UGHoeItem extends HoeItem {

    public UGHoeItem(IItemTier tier, int attack, float speed) {
        super(tier, attack, speed, new Properties()
                .stacksTo(1)
                .durability(tier.getUses())
                .tab(UGItemGroups.GROUP)
                .rarity(UGSwordItem.isForgotten(tier))
        );
    }
}
