package appeng.items.storage;

import java.util.List;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.GenericStack;

public record StorageCellTooltipComponent(List<ItemStack> upgrades,
        List<GenericStack> content,
        boolean hasMoreContent) implements TooltipComponent {
}
