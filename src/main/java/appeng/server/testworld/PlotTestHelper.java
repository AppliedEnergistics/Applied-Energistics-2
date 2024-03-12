package appeng.server.testworld;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;

import appeng.api.config.Actionable;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.parts.IPartHost;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.IGridConnectedBlockEntity;
import appeng.parts.AEBasePart;
import appeng.util.Platform;

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

    public <T extends AEBasePart> T getPart(BlockPos pos, @Nullable Direction side, Class<T> partClass) {
        var be = getBlockEntity(pos);
        if (!(be instanceof IPartHost partHost)) {
            fail("not a part host", pos);
            return null;
        }

        var part = partHost.getPart(side);
        if (part == null) {
            fail("part missing", pos);
        }

        if (!partClass.isInstance(part)) {
            fail("wrong part", pos);
        }

        return partClass.cast(part);
    }

    /**
     * Find all grids in the area and return the biggest one.
     */
    @NotNull
    public IGrid getGrid(BlockPos pos) {
        checkAllInitialized();

        var be = getBlockEntity(pos);
        if (be instanceof IGridConnectedBlockEntity gridConnectedBlockEntity) {
            IGrid grid = gridConnectedBlockEntity.getMainNode().getGrid();
            check(grid != null, "no grid", pos);
            return grid;
        }

        var nodeHost = GridHelper.getNodeHost(getLevel(), this.absolutePos(pos));
        if (nodeHost != null) {
            for (var side : Direction.values()) {
                var node = nodeHost.getGridNode(side);
                if (node != null) {
                    return node.getGrid();
                }
            }
        }
        fail("no grid", pos);
        return null;
    }

    /**
     * Checks that everything is initialized
     */
    public void checkAllInitialized() {
        forEveryBlockInStructure(blockPos -> {
            var be = getBlockEntity(blockPos);
            if (be instanceof IGridConnectedBlockEntity gridConnectedBlockEntity) {
                check(gridConnectedBlockEntity.getMainNode().isReady(), "BE " + be + " is not ready");
            } else if (be instanceof IPartHost partHost) {
                for (var side : Platform.DIRECTIONS_WITH_NULL) {
                    var part = partHost.getPart(side);
                    if (part instanceof AEBasePart basePart) {
                        var mainNode = basePart.getMainNode();
                        check(mainNode.isReady(), "Part " + part + " is not ready");
                    }
                }
            }
        });
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

    public void assertContainsNot(MEStorage storage, AEKey key) {
        var count = storage.getAvailableStacks().get(key);
        if (count > 0) {
            throw new GameTestAssertException("Network storage does contains " + key + ".");
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

    public <T, C> T getCapability(BlockPos ref, BlockCapability<T, C> cap, C context) {
        return getLevel().getCapability(cap, absolutePos(ref), context);
    }

    public void assertEquals(BlockPos ref, Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            String message = actual + " was not " + expected;
            fail(message, ref);
        }
    }

    public void check(boolean test, String errorMessage) throws GameTestAssertException {
        if (!test) {
            fail(errorMessage);
        }
    }

    public void check(boolean test, String errorMessage, BlockPos pos) throws GameTestAssertException {
        if (!test) {
            fail(errorMessage, pos);
        }
    }

    public KeyCounter countContainerContentAt(BlockPos pos) {
        var counter = new KeyCounter();
        countContainerContentAt(pos, counter);
        return counter;
    }

    public void countContainerContentAt(BlockPos pos, KeyCounter counter) {
        var be = getBlockEntity(pos);
        if (be instanceof BaseContainerBlockEntity container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                var item = container.getItem(i);
                if (!item.isEmpty()) {
                    counter.add(AEItemKey.of(item), item.getCount());
                }
            }
        } else if (be instanceof AEBaseInvBlockEntity aeBe) {
            var internalInv = aeBe.getInternalInventory();
            for (var item : internalInv) {
                counter.add(AEItemKey.of(item), item.getCount());
            }
        } else {
            throw new RuntimeException("Unsupported BE: " + be);
        }
    }

}
