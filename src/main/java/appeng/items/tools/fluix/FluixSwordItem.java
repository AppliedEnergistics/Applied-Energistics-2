package appeng.items.tools.fluix;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import appeng.hooks.IntrinsicEnchantItem;

public class FluixSwordItem extends SwordItem implements IntrinsicEnchantItem {
    private final IntrinsicEnchantment intrinsicEnchantment = new IntrinsicEnchantment(Enchantments.LOOTING, 1);

    public FluixSwordItem(Properties props) {
        super(FluixToolType.FLUIX.getToolTier(),
                props.attributes(createAttributes(FluixToolType.FLUIX.getToolTier(), 3, -2.4f)));
    }

    @Override
    public int getIntrinsicEnchantLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return intrinsicEnchantment.getLevel(enchantment);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag isAdvanced) {
        intrinsicEnchantment.appendHoverText(context, tooltipComponents);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
