package appeng.server.testplots;

import java.util.Locale;
import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.RotationSegment;

import appeng.server.testworld.PlotBuilder;

/**
 * Test plot that sets up a working area for working on Guidebook structures.
 */
public final class GuidebookPlot {
    private GuidebookPlot() {
    }

    @TestPlot("guidebook_structure_workarea")
    public static void guidebookStructureWorkArea(PlotBuilder plot) {
        plot.block("[0,15] -1 [0,15]", Blocks.BLACK_CONCRETE);
        plot.block("-1 -1 [0,15]", Blocks.BLUE_TERRACOTTA);
        plot.block("[0,15] -1 -1", Blocks.RED_TERRACOTTA);

        var controlPos = BlockPos.ZERO.north(2).east(1);
        control(plot, controlPos.east(0), "LOAD", (blockEntity, origin) -> {
            return String.format(Locale.ROOT, "ae2guide importstructure %s", formatBlockPos(origin));
        });
        control(plot, controlPos.east(1), "SAVE", (blockEntity, origin) -> {
            return String.format(Locale.ROOT, "ae2guide exportstructure %s %d %d %d", formatBlockPos(origin), 16, 16,
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
        plot.customizeBlockEntity(pos, BlockEntityType.SIGN, sign -> {
            sign.getFrontText().setMessage(0, Component.literal(label));
        });

        pos = pos.north();
        var cmdBlockRelPos = pos.below().below();
        plot.block(cmdBlockRelPos, Blocks.COMMAND_BLOCK);
        plot.customizeBlockEntity(cmdBlockRelPos, BlockEntityType.COMMAND_BLOCK, cmdBlock -> {
            var origin = cmdBlock.getBlockPos().offset(
                    -cmdBlockRelPos.getX(),
                    -cmdBlockRelPos.getY(),
                    -cmdBlockRelPos.getZ());
            cmdBlock.getCommandBlock().setCommand(commandSupplier.apply(cmdBlock, origin));
        });
        plot.buttonOn(pos.below(), Direction.UP);
    }
}
