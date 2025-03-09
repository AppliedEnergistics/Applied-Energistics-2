package appeng.parts.automation;

import java.util.List;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.items.parts.PartItem;

/**
 * Special part item for {@link AnnihilationPlanePart} to handle enchants and extended tooltips.
 */
public class AnnihilationPlanePartItem extends PartItem<AnnihilationPlanePart> {
    /**
     * Workaround to make annihilation planes combinable in anvils.
     * <p>
     * null = false, non-null = true
     */
    public static final ThreadLocal<Object> CALLING_DAMAGEABLE_FROM_ANVIL = ThreadLocal.withInitial(() -> null);

    public AnnihilationPlanePartItem(Properties properties) {
        super(properties.component(DataComponents.ENCHANTABLE, new Enchantable(10)), AnnihilationPlanePart.class,
                AnnihilationPlanePart::new);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return CALLING_DAMAGEABLE_FROM_ANVIL.get() != null ? 1 : super.getMaxDamage(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines,
            TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, lines, isAdvanced);

        var enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) {
            lines.add(Tooltips.of(GuiText.CanBeEnchanted));
        } else {
            lines.add(Tooltips.of(GuiText.IncreasedEnergyUseFromEnchants));
        }
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        super.addToMainCreativeTab(parameters, output);

        var enchantmentRegistry = parameters.holders().lookupOrThrow(Registries.ENCHANTMENT);

        var enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchantments.set(enchantmentRegistry.getOrThrow(Enchantments.SILK_TOUCH), 1);

        var silkTouch = new ItemStack(this);
        silkTouch.set(DataComponents.ENCHANTMENTS, enchantments.toImmutable());
        output.accept(silkTouch);
    }
}
