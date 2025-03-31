package appeng.items.tools.fluix;

import java.util.function.Consumer;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import appeng.hooks.IntrinsicEnchantItem;

public class FluixSpadeItem extends ShovelItem implements IntrinsicEnchantItem {
    private final IntrinsicEnchantment intrinsicEnchantment = new IntrinsicEnchantment(Enchantments.FORTUNE, 1);

    public FluixSpadeItem(Properties props) {
        super(FluixToolType.FLUIX.getMaterial(), 1.5F, -3.0F,
                props.repairable(FluixToolType.FLUIX.getRepairIngredient()));
    }

    @Override
    public int getIntrinsicEnchantLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return intrinsicEnchantment.getLevel(enchantment);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipComponents,
            TooltipFlag isAdvanced) {
        intrinsicEnchantment.appendHoverText(context, tooltipComponents);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
