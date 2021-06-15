package appeng.init;

import net.minecraft.block.DispenserBlock;

import appeng.core.api.definitions.ApiBlocks;
import appeng.core.api.definitions.ApiItems;
import appeng.hooks.BlockToolDispenseItemBehavior;
import appeng.hooks.MatterCannonDispenseItemBehavior;
import appeng.hooks.TinyTNTDispenseItemBehavior;

/**
 * Registers custom {@link DispenserBlock} behaviors for our items.
 */
public final class InitDispenserBehavior {

    private InitDispenserBehavior() {
    }

    public static void init() {
        DispenserBlock.registerDispenseBehavior(ApiBlocks.tinyTNT(), new TinyTNTDispenseItemBehavior());
        DispenserBlock.registerDispenseBehavior(ApiItems.entropyManipulator(), new BlockToolDispenseItemBehavior());
        DispenserBlock.registerDispenseBehavior(ApiItems.massCannon(), new MatterCannonDispenseItemBehavior());
        DispenserBlock.registerDispenseBehavior(ApiItems.colorApplicator(), new BlockToolDispenseItemBehavior());
    }

}
