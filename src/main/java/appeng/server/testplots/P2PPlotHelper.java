package appeng.server.testplots;

import java.util.Collection;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import appeng.api.networking.IGrid;
import appeng.api.parts.PartHelper;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.me.service.P2PService;
import appeng.parts.AEBasePart;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.server.testworld.PlotBuilder;
import appeng.util.SettingsFrom;

public final class P2PPlotHelper {
    private P2PPlotHelper() {
    }

    public static <T extends P2PTunnelPart<?>> void placeTunnel(PlotBuilder plot, ItemDefinition<PartItem<T>> tunnel) {
        var origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.cable(origin);
        plot.cable(origin.west()).part(Direction.WEST, tunnel);
        plot.cable(origin.east()).part(Direction.EAST, tunnel);
        plot.afterGridInitAt(origin, (grid, gridNode) -> {
            BlockPos absOrigin = ((AEBasePart) gridNode.getOwner()).getBlockEntity().getBlockPos();

            linkTunnels(grid, tunnel.asItem().getPartClass(), absOrigin.west(), absOrigin.east());
        });
    }

    public static <T extends P2PTunnelPart<?>> short linkTunnels(IGrid grid,
            Class<T> tunnelType,
            BlockPos inputPos,
            BlockPos outputPos) {
        var p2p = P2PService.get(grid);
        T inputTunnel = null;
        T outputTunnel = null;
        for (T p2pPart : grid.getMachines(tunnelType)) {
            if (p2pPart.getBlockEntity().getBlockPos().equals(inputPos)) {
                inputTunnel = p2pPart;
            } else if (p2pPart.getBlockEntity().getBlockPos().equals(outputPos)) {
                outputTunnel = p2pPart;
            }
        }

        Objects.requireNonNull(inputTunnel, "inputTunnel");
        Objects.requireNonNull(outputTunnel, "outputTunnel");

        inputTunnel.setFrequency(p2p.newFrequency());
        p2p.updateFreq(inputTunnel, inputTunnel.getFrequency());

        // Link to output
        var settings = new CompoundTag();
        inputTunnel.exportSettings(SettingsFrom.MEMORY_CARD, settings);
        outputTunnel.importSettings(SettingsFrom.MEMORY_CARD, settings, null);

        return inputTunnel.getFrequency();
    }

    public static short linkTunnels(IGrid grid,
            PosAndSide inputPos,
            Collection<PosAndSide> outputPositions) {
        var p2p = P2PService.get(grid);

        var level = grid.getPivot().getLevel();
        var inputTunnel = getTunnelAt(level, inputPos);

        inputTunnel.setFrequency(p2p.newFrequency());
        p2p.updateFreq(inputTunnel, inputTunnel.getFrequency());

        // Link to output
        var settings = new CompoundTag();
        inputTunnel.exportSettings(SettingsFrom.MEMORY_CARD, settings);

        for (var outputPos : outputPositions) {
            var outputTunnel = getTunnelAt(level, outputPos);
            outputTunnel.importSettings(SettingsFrom.MEMORY_CARD, settings, null);
        }

        return inputTunnel.getFrequency();
    }

    private static P2PTunnelPart<?> getTunnelAt(Level level, PosAndSide posAndSide) {
        var part = PartHelper.getPart(level, posAndSide.pos(), posAndSide.side());
        if (!(part instanceof P2PTunnelPart<?>p2PTunnelPart)) {
            throw new IllegalStateException("No P2P @ " + posAndSide);
        }
        return p2PTunnelPart;
    }

}
