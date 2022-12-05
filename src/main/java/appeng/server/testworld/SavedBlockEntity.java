package appeng.server.testworld;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SavedBlockEntity {

    private final PlotTestHelper helper;
    private BlockPos pos;
    @Nullable
    private BlockState blockState;
    private CompoundTag data;

    public SavedBlockEntity(PlotTestHelper helper) {
        this.helper = helper;
    }

    public void save(BlockPos pos) {
        this.pos = pos;
        blockState = helper.getBlockState(pos);
        var be = helper.getBlockEntity(pos);
        if (be == null) {
            helper.fail("No BlockEntity", pos);
            return;
        }
        data = be.saveWithId();
    }

    public void saveAndRemove(BlockPos pos) {
        save(pos);
        helper.destroyBlock(pos);
    }

    public BlockEntity restore() {
        if (pos == null) {
            helper.fail("No block entity was saved");
            return null;
        }

        helper.setBlock(BlockPos.ZERO, blockState);
        var be = BlockEntity.loadStatic(
                helper.absolutePos(BlockPos.ZERO),
                blockState,
                data);
        if (be == null) {
            helper.fail("Blockentity could not be restored", pos);
            return null;
        }
        helper.getLevel().setBlockEntity(be);
        return be;
    }

}
