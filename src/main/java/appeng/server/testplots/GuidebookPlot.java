package appeng.server.testplots;

import java.util.Locale;
import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.properties.RotationSegment;

import appeng.server.testworld.PlotBuilder;

/**
 * Test plot that sets up a working area for working on Guidebook structures.
 */
@TestPlotClass
public final class GuidebookPlot {
    private GuidebookPlot() {
    }

    @TestPlot(value = "guidebook_structure_workarea", gameTest = false)
    public static void guidebookStructureWorkArea(PlotBuilder plot) {
        plot.block("[0,15] -1 [0,15]", Blocks.CONCRETE.pick(DyeColor.BLACK));
        plot.block("-1 -1 [0,15]", Blocks.DYED_TERRACOTTA.pick(DyeColor.BLUE));
        plot.block("[0,15] -1 -1", Blocks.DYED_TERRACOTTA.pick(DyeColor.RED));

        var controlPos = BlockPos.ZERO.north(2).east(1);
        control(plot, controlPos.east(0), "LOAD", (blockEntity, origin) -> {
            return String.format(Locale.ROOT, "guideme importstructure %s", formatBlockPos(origin));
        });
        control(plot, controlPos.east(1), "SAVE", (blockEntity, origin) -> {
            return String.format(Locale.ROOT, "guideme exportstructure %s %d %d %d", formatBlockPos(origin), 16, 16,
                    16);
        });
        control(plot, controlPos.east(2), "CLEAR", (blockEntity, origin) -> {
            var to = origin.offset(16, 16, 16);
            return String.format(Locale.ROOT, "fill %s %s air", formatBlockPos(origin), formatBlockPos(to));
        });
    }

    private static String formatBlockPos(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private static void control(PlotBuilder plot, BlockPos pos, String label,
            BiFunction<BlockEntity, BlockPos, String> commandSupplier) {
        plot.blockState(pos, Blocks.DARK_OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION,
                RotationSegment.convertToSegment(Direction.NORTH)));
        plot.customizeBlockEntity(pos, BlockEntityTypes.SIGN, sign -> {
            var text = sign.getFrontText().setMessage(0, Component.literal(label)).setColor(DyeColor.WHITE);
            sign.setText(text, true);
        });

        pos = pos.north();
        var cmdBlockRelPos = pos.below().below();
        plot.block(cmdBlockRelPos, Blocks.COMMAND_BLOCK);
        plot.customizeBlockEntity(cmdBlockRelPos, BlockEntityTypes.COMMAND_BLOCK, cmdBlock -> {
            var origin = cmdBlock.getBlockPos().offset(
                    -cmdBlockRelPos.getX(),
                    -cmdBlockRelPos.getY(),
                    -cmdBlockRelPos.getZ());
            cmdBlock.getCommandBlock().setCommand(commandSupplier.apply(cmdBlock, origin));
        });
        plot.buttonOn(pos.below(), Direction.UP);
    }
}
