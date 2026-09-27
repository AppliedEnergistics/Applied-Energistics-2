package appeng.block.misc;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.misc.MysteriousCubeBlockEntity;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.server.services.compass.ServerCompassService;

public class MysteriousCubeBlock extends AEBaseEntityBlock<MysteriousCubeBlockEntity> {
    // Not a redstone conductor to prevent using it as a facade.
    public static Properties properties(Properties p) {
        return metalProps(p).strength(10, 1000).isRedstoneConductor(Blocks::never);
    }

    public MysteriousCubeBlock(Properties p) {
        super(properties(p));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (level instanceof ServerLevel serverLevel) {
            ServerCompassService.notifyBlockChange(serverLevel, pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean p_394545_) {
        ServerCompassService.notifyBlockChange(level, pos);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, Consumer<Component> tooltip,
            TooltipFlag flag) {
        tooltip.accept(Tooltips.of(GuiText.MysteriousQuote, Tooltips.QUOTE_TEXT));
    }
}
