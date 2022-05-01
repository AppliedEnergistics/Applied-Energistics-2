package appeng.items.tools.fluix;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import appeng.core.localization.GuiText;
import appeng.hooks.IntrinsicEnchantItem;

public class FluixPickaxeItem extends PickaxeItem implements IntrinsicEnchantItem {
    public FluixPickaxeItem(Properties props) {
        super(FluixToolType.FLUIX.getToolTier(), 1, -2.8F, props);
    }

    @Override
    public int getIntrinsicEnchantLevel(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.BLOCK_FORTUNE ? 1 : 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
            TooltipFlag isAdvanced) {
        tooltipComponents.add(GuiText.IntrinsicEnchant.text(Enchantments.BLOCK_FORTUNE.getFullname(1)));
    }
}
