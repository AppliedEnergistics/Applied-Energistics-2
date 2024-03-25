package appeng.integration.modules.itemlists;

import net.minecraft.world.item.ItemStack;

import appeng.api.util.AEColor;
import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.parts.FacadeItem;

public final class ItemPredicates {
    private ItemPredicates() {
    }

    public static boolean shouldBeHidden(ItemStack stack) {
        if (isInternal(stack)) {
            return true;
        }

        if (!AEConfig.instance().isDebugToolsEnabled() && isDeveloperTool(stack)) {
            return true;
        }

        if (!AEConfig.instance().isEnableFacadesInRecipeViewer() && isFacade(stack)) {
            return true;
        }

        if (AEConfig.instance().isDisableColoredCableRecipesInRecipeViewer() && isColoredCable(stack)) {
            return true;
        }

        return false;
    }

    private static boolean isInternal(ItemStack stack) {
        return AEItems.WRAPPED_GENERIC_STACK.isSameAs(stack)
                || isBrokenFacade(stack) // REI will add a broken facade with no NBT
                || AEBlocks.CABLE_BUS.isSameAs(stack)
                || AEBlocks.MATRIX_FRAME.isSameAs(stack)
                || AEBlocks.PAINT.isSameAs(stack);
    }

    private static boolean isBrokenFacade(ItemStack stack) {
        return stack.getItem() instanceof FacadeItem && !stack.hasTag();
    }

    private static boolean isFacade(ItemStack stack) {
        return stack.getItem() instanceof FacadeItem;
    }

    private static boolean isDeveloperTool(ItemStack stack) {
        return AEBlocks.DEBUG_CUBE_GEN.isSameAs(stack) ||
                AEBlocks.DEBUG_ENERGY_GEN.isSameAs(stack) ||
                AEBlocks.DEBUG_ITEM_GEN.isSameAs(stack) ||
                AEBlocks.DEBUG_PHANTOM_NODE.isSameAs(stack) ||
                AEItems.DEBUG_CARD.isSameAs(stack) ||
                AEItems.DEBUG_ERASER.isSameAs(stack) ||
                AEItems.DEBUG_METEORITE_PLACER.isSameAs(stack) ||
                AEItems.DEBUG_REPLICATOR_CARD.isSameAs(stack);
    }

    private static boolean isColoredCable(ItemStack stack) {
        for (var color : AEColor.values()) {
            if (color == AEColor.TRANSPARENT) {
                continue; // Keep the Fluix variant
            }
            if (stack.getItem() == AEParts.COVERED_CABLE.item(color) ||
                    stack.getItem() == AEParts.COVERED_DENSE_CABLE.item(color) ||
                    stack.getItem() == AEParts.GLASS_CABLE.item(color) ||
                    stack.getItem() == AEParts.SMART_CABLE.item(color) ||
                    stack.getItem() == AEParts.SMART_DENSE_CABLE.item(color)) {
                return true;
            }
        }
        return false;
    }
}
