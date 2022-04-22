package appeng.items.tools.fluix;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantments;

public class FluixPickaxeItem extends PickaxeItem {
    public FluixPickaxeItem(Properties props) {
        super(FluixToolType.FLUIX.getToolTier(), 1, -2.8F, props);
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
