package appeng.blockentity.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.blockentity.AEBaseBlockEntity;

/**
 * This is only really a block entity to allow better tracking of it.
 */
public class MysteriousCubeBlockEntity extends AEBaseBlockEntity {
    public MysteriousCubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }
}
