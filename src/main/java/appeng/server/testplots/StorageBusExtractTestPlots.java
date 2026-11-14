package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.core.definitions.AEParts;
import appeng.me.helpers.BaseActionSource;
import appeng.server.testworld.PlotBuilder;

@TestPlotClass
public final class StorageBusExtractTestPlots {
    private StorageBusExtractTestPlots() {
    }

    /**
     * Cauldrons only allow extracting their entire contents, so the extractable-only filter does not list them. They
     * must still be extractable through the storage bus.
     */
    @TestPlot("storagebus_extract_unlisted_cauldron")
    public static void extractUnlistedCauldron(PlotBuilder plot) {
        plot.creativeEnergyCell("-1 0 0");
        plot.cable("0 0 0").part(Direction.UP, AEParts.STORAGE_BUS);
        plot.blockState(new BlockPos(0, 1, 0), Blocks.LAVA_CAULDRON.defaultBlockState());
        plot.test(helper -> helper.startSequence()
                .thenIdle(20)
                .thenExecute(() -> {
                    var storage = helper.getGrid(BlockPos.ZERO).getStorageService().getInventory();
                    // Make sure the storage bus has scanned the cauldron
                    storage.getAvailableStacks();
                    var extracted = storage.extract(AEFluidKey.of(Fluids.LAVA), AEFluidKey.AMOUNT_BUCKET,
                            Actionable.SIMULATE, new BaseActionSource());
                    helper.check(extracted == AEFluidKey.AMOUNT_BUCKET, "could not extract lava: " + extracted);
                })
                .thenSucceed());
    }

    /**
     * Items inserted into the external inventory by something other than AE must be extractable right away, not only
     * after the storage bus has polled the inventory again.
     */
    @TestPlot("storagebus_extract_after_external_insert")
    public static void extractAfterExternalInsert(PlotBuilder plot) {
        var barrelPos = new BlockPos(0, 1, 0);
        plot.creativeEnergyCell("-1 0 0");
        plot.cable("0 0 0").part(Direction.UP, AEParts.STORAGE_BUS);
        plot.blockState(barrelPos, Blocks.BARREL.defaultBlockState());
        plot.test(helper -> helper.startSequence()
                .thenIdle(100)
                .thenExecute(() -> {
                    var storage = helper.getGrid(BlockPos.ZERO).getStorageService().getInventory();
                    // Make sure the storage bus has scanned the empty barrel before the insertion
                    storage.getAvailableStacks();
                    var barrel = (Container) helper.getLevel().getBlockEntity(helper.absolutePos(barrelPos));
                    barrel.setItem(0, new ItemStack(Items.DIAMOND, 8));
                    barrel.setChanged();
                    var extracted = storage.extract(AEItemKey.of(Items.DIAMOND), 8, Actionable.SIMULATE,
                            new BaseActionSource());
                    helper.check(extracted == 8, "could not extract diamonds: " + extracted);
                })
                .thenSucceed());
    }
}
