package appeng.server.testworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.IGridConnectedBlockEntity;

public class PlotTestHelper extends GameTestHelper {
    private final BlockPos plotTranslation;

    public PlotTestHelper(BlockPos plotTranslation, GameTestInfo gameTestInfo) {
        super(gameTestInfo);
        this.plotTranslation = plotTranslation;
    }

    @Override
    public BlockPos absolutePos(BlockPos pos) {
        return super.absolutePos(pos.offset(plotTranslation).offset(0, 1, 0));
    }

    @Override
    public BlockPos relativePos(BlockPos pos) {
        return super.relativePos(pos)
                .offset(
                        -plotTranslation.getX(),
                        -plotTranslation.getY(),
                        -plotTranslation.getZ())
                .offset(0, -1, 0);
    }

    @Override
    public Vec3 absoluteVec(Vec3 relativeVec3) {
        return super.absoluteVec(relativeVec3)
                .add(
                        plotTranslation.getX(),
                        plotTranslation.getY(),
                        plotTranslation.getZ());
    }

    /**
     * Find all grids in the area and return the biggest one.
     */
    public IGrid getGrid(BlockPos pos) {
        var be = getBlockEntity(pos);
        if (be instanceof IGridConnectedBlockEntity gridConnectedBlockEntity) {
            return gridConnectedBlockEntity.getMainNode().getGrid();
        } else if (be instanceof IInWorldGridNodeHost nodeHost) {
            for (var side : Direction.values()) {
                var node = nodeHost.getGridNode(side);
                if (node != null) {
                    return node.getGrid();
                }
            }
        }
        throw new GameTestAssertException("No grid @ " + pos);
    }

    public void assertContains(IGrid grid, Item item) {
        var storage = grid.getStorageService().getInventory();
        assertContains(storage, AEItemKey.of(item));
    }

    public void assertContains(MEStorage storage, AEKey key) {
        var count = storage.getAvailableStacks().get(key);
        if (count <= 0) {
            throw new GameTestAssertException("Network storage does not contain " + key + ". Available keys: "
                    + storage.getAvailableStacks().keySet());
        }
    }

    public void clearStorage(IGrid grid) {
        clearStorage(grid.getStorageService().getInventory());
    }

    public void clearStorage(MEStorage storage) {
        var counter = storage.getAvailableStacks();
        for (var key : counter.keySet()) {
            storage.extract(key, Long.MAX_VALUE, Actionable.MODULATE, new BaseActionSource());
        }
    }

    public void check(boolean test, String errorMessage) throws GameTestAssertException {
        if (!test) {
            throw new GameTestAssertException(errorMessage);
        }
    }
}
