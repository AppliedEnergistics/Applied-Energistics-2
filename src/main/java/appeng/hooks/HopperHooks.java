package appeng.hooks;

import alexiil.mc.lib.attributes.SearchOption;
import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.item.ItemAttributes;
import alexiil.mc.lib.attributes.item.ItemExtractable;
import alexiil.mc.lib.attributes.item.ItemInsertable;
import alexiil.mc.lib.attributes.item.ItemInvUtil;
import alexiil.mc.lib.attributes.item.compat.FixedInventoryVanillaWrapper;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

// SEE: https://github.com/AlexIIL/LibBlockAttributes/pull/27
public final class HopperHooks {

    private HopperHooks() {
    }

    public static ActionResult tryInsert(HopperBlockEntity hopper) {
        World world = hopper.getWorld();
        Direction towards = hopper.getCachedState().get(HopperBlock.FACING);
        BlockPos targetPos = hopper.getPos().offset(towards);

        if (isVanillaInventoryAt(world, targetPos)) {
            return ActionResult.PASS;
        }

        ItemInsertable insertable = ItemAttributes.INSERTABLE.getFirstOrNull(world, targetPos, SearchOptions.inDirection(towards));
        if (insertable == null) {
            return ActionResult.PASS; // Let Vanilla handle non-LBA enabled inventories and Entities
        }

        // Get an Extractable for the Hopper's internal inventory
        ItemExtractable extractable = new FixedInventoryVanillaWrapper(hopper).getExtractable();

        // Try to move any one item from hopper->inventory
        if (ItemInvUtil.move(extractable, insertable, 1) > 0) {
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.FAIL;
        }
    }

    /**
     * Hoppers always search UP for adjacent inventories to extract from.
     */
    private static final SearchOption<? super ItemExtractable> EXTRACT_SEARCH = SearchOptions.inDirection(Direction.UP);

    /**
     * Tries to extract items from an inventory above the given hopper into the hopper.
     * Note that the given hopper can also be a hopper minecart.
     */
    public static ActionResult tryExtract(Hopper hopper) {
        World world = hopper.getWorld();
        BlockPos blockAbove = new BlockPos(hopper.getHopperX(), hopper.getHopperY() + 1, hopper.getHopperZ());

        if (isVanillaInventoryAt(world, blockAbove)) {
            return ActionResult.PASS;
        }

        // Get an Extractable for the inventory above the hopper
        ItemExtractable extractable = ItemAttributes.EXTRACTABLE.getFirstOrNull(world, blockAbove, EXTRACT_SEARCH);
        if (extractable == null) {
            return ActionResult.PASS; // Let Vanilla handle non-LBA enabled inventories and Entities
        }

        // Get an Insertable for the Hopper's internal inventory
        ItemInsertable insertable = new FixedInventoryVanillaWrapper(hopper).getInsertable();

        // Try to move any one item from inventory->hopper
        if (ItemInvUtil.move(extractable, insertable, 1) > 0) {
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.FAIL;
        }
    }

    private static boolean isVanillaInventoryAt(World world, BlockPos pos) {
        // If there's a TE at the target position that implements Inventory (such that Hopper would handle it itself)
        // defer to Vanilla to avoid injecting ourselves inbetween Vanilla blocks needlessly.
        // ItemAttributes.INSERTABLE would return an auto-converted Inventory in such cases.
        return world.getBlockEntity(pos) instanceof Inventory;
    }

}
