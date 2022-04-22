package appeng.items.tools.fluix;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.Enchantments;

public class FluixSpadeItem extends ShovelItem {
    public FluixSpadeItem(Properties props) {
        super(FluixToolType.FLUIX.getToolTier(), 1.5F, -3.0F, props);
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
