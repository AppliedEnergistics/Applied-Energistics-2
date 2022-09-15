package appeng.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import appeng.block.AEBaseBlock;
import appeng.server.services.compass.CompassService;

public class MysteriousCubeBlock extends AEBaseBlock {
    public MysteriousCubeBlock() {
        super(defaultProps(Material.METAL).strength(10, 1000));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (level instanceof ServerLevel serverLevel) {
            CompassService.notifyBlockChange(serverLevel, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == state.getBlock()) {
            return; // Just a block state change
        }

        super.onRemove(state, level, pos, newState, isMoving);

        if (level instanceof ServerLevel serverLevel) {
            CompassService.notifyBlockChange(serverLevel, pos);
        }
    }
}
