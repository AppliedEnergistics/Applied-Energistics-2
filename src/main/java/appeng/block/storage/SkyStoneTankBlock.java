package appeng.block.storage;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.storage.SkyStoneTankBlockEntity;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;

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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Tooltips.of(GuiText.TankBucketCapacity, SkyStoneTankBlockEntity.BUCKET_CAPACITY));
    }
}
