package appeng.items.tools.fluix;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import appeng.core.localization.GuiText;
import appeng.hooks.IntrinsicEnchantItem;

public class FluixSwordItem extends SwordItem implements IntrinsicEnchantItem {
    public FluixSwordItem(Properties props) {
        super(FluixToolType.FLUIX.getToolTier(), 3, -2.4F, props);
    }

    @Override
    public int getIntrinsicEnchantLevel(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.MOB_LOOTING ? 1 : 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
            TooltipFlag isAdvanced) {
        tooltipComponents.add(GuiText.IntrinsicEnchant.text(Enchantments.MOB_LOOTING.getFullname(1)));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
