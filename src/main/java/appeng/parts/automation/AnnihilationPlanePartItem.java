package appeng.parts.automation;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.items.parts.PartItem;

/**
 * Special part item for {@link AnnihilationPlanePart} to handle enchants and extended tooltips.
 */
public class AnnihilationPlanePartItem extends PartItem<AnnihilationPlanePart> {
    public AnnihilationPlanePartItem(Properties properties) {
        super(properties, AnnihilationPlanePart.class, AnnihilationPlanePart::new);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category == EnchantmentCategory.DIGGER;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, lines, isAdvanced);

        var enchantments = EnchantmentHelper.getEnchantments(stack);
        if (enchantments.isEmpty()) {
            lines.add(Tooltips.of(GuiText.CanBeEnchanted));
        } else {
            lines.add(Tooltips.of(GuiText.IncreasedEnergyUseFromEnchants));
        }
    }
}
