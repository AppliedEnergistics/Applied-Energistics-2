package appeng.block.misc;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import appeng.block.AEBaseBlock;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.server.services.compass.CompassService;

public class MysteriousCubeBlock extends AEBaseBlock {
    public static final Properties PROPERTIES = defaultProps(Material.METAL).strength(10, 1000);

    public MysteriousCubeBlock() {
        super(PROPERTIES);
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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Tooltips.of(GuiText.MysteriousQuote, Tooltips.QUOTE_TEXT));
    }
}
