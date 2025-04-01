package appeng.block.storage;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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
    protected InteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (super.useItemOn(heldItem, state, level, pos, player, hand, hit) == InteractionResult.PASS) {
            if (level.getBlockEntity(pos) instanceof SkyStoneTankBlockEntity tank && tank.onPlayerUse(player, hand)) {
                return InteractionResult.SUCCESS;
            }

        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, Consumer<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.accept(Tooltips.of(GuiText.TankBucketCapacity, SkyStoneTankBlockEntity.BUCKET_CAPACITY));
    }
}
