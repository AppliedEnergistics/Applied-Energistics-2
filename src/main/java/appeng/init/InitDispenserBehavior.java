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
        DispenserBlock.registerDispenseBehavior(ApiBlocks.tinyTNT, new TinyTNTDispenseItemBehavior());
        DispenserBlock.registerDispenseBehavior(ApiItems.ENTROPY_MANIPULATOR, new BlockToolDispenseItemBehavior());
        DispenserBlock.registerDispenseBehavior(ApiItems.MASS_CANNON, new MatterCannonDispenseItemBehavior());
        DispenserBlock.registerDispenseBehavior(ApiItems.COLOR_APPLICATOR, new BlockToolDispenseItemBehavior());
    }

}
