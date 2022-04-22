package appeng.items.tools.fluix;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantments;

public class FluixSwordItem extends SwordItem {
    public FluixSwordItem(Properties props) {
        super(FluixToolType.FLUIX.getToolTier(), 3, -2.4F, props);
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (allowdedIn(tab)) {
            ItemStack item = new ItemStack(this);
            item.enchant(Enchantments.SHARPNESS, 1);
            items.add(item);
        }
    }
}
