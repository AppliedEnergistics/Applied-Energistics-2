package appeng.block.storage;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.storage.SkyStoneTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SkyStoneTankBlock extends AEBaseEntityBlock<SkyStoneTankBlockEntity> {

    public SkyStoneTankBlock(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (super.use(state, level, pos, player, hand, hit) == InteractionResult.PASS) {
            SkyStoneTankBlockEntity be = (SkyStoneTankBlockEntity) level.getBlockEntity(pos);
            if (be.onPlayerUse(player)) {
                return InteractionResult.SUCCESS;
            }

        }
        return InteractionResult.PASS;
    }
}
