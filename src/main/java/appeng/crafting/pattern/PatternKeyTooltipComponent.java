package appeng.crafting.pattern;

import java.util.List;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

import appeng.api.stacks.GenericStack;

public record PatternKeyTooltipComponent(
        List<GenericStack> inputs,
        List<GenericStack> outputs,
        String author,
        boolean isCrafting,
        boolean canSubstitute,
        boolean canSubstituteFluids,
        boolean showAmounts) implements TooltipComponent {
}
