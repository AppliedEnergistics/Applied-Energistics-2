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
        var be = helper.getBlockEntity(pos, BlockEntity.class);
        if (be == null) {
            throw helper.assertionException(pos, "No BlockEntity");
        }
        data = be.saveWithId(helper.getLevel().registryAccess());
    }

    public void saveAndRemove(BlockPos pos) {
        save(pos);
        helper.destroyBlock(pos);
    }

    public BlockEntity restore() {
        if (pos == null) {
            throw helper.assertionException("No block entity was saved");
        }

        helper.setBlock(BlockPos.ZERO, blockState);
        var be = BlockEntity.loadStatic(
                helper.absolutePos(BlockPos.ZERO),
                blockState,
                data,
                helper.getLevel().registryAccess());
        if (be == null) {
            throw helper.assertionException(pos, "Blockentity could not be restored");
        }
        helper.getLevel().setBlockEntity(be);
        return be;
    }

}
