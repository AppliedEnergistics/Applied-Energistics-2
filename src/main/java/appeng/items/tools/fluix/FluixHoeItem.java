package appeng.items.tools.fluix;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

public class FluixHoeItem extends HoeItem {
    public FluixHoeItem(Properties props) {
        super(FluixToolType.FLUIX.getToolTier(), -2, -1.0F, props);
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (allowdedIn(tab)) {
            ItemStack item = new ItemStack(this);
            item.enchant(Enchantments.BLOCK_FORTUNE, 1);
            items.add(item);
        }
    }
}
