package appeng.server.testplots;

import java.util.Objects;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.mutable.MutableShort;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.networking.IGrid;
import appeng.blockentity.networking.EnergyCellBlockEntity;
import appeng.blockentity.storage.SkyStoneTankBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.me.service.P2PService;
import appeng.parts.AEBasePart;
import appeng.parts.p2p.MEP2PTunnelPart;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.parts.reporting.AbstractPanelPart;
import appeng.parts.reporting.PanelPart;
import appeng.server.testworld.PlotBuilder;
import appeng.util.SettingsFrom;

public class P2PTestPlots {
    @TestPlot("p2p_me")
    public static void me(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        placeTunnel(plot, AEParts.ME_P2P_TUNNEL);

        // Import bus to import from a chest and place it into storage bus.
        // this tests that the import bus on one end can see the storage bus
        // on the other of the P2P tunnel
        plot.cable(origin.west().west())
                .part(Direction.WEST, AEParts.IMPORT_BUS);
        plot.chest(origin.west().west().west(),
                new ItemStack(Items.BEDROCK));

        // Storage bus for the import bus on the P2P
        plot.cable(origin.east().east())
                .part(Direction.EAST, AEParts.STORAGE_BUS);
        plot.chest(origin.east().east().east());

        // High-priority storage bus on the main network to make sure the import bus
        // cannot see the carrier network
        plot.part(origin, Direction.UP, AEParts.STORAGE_BUS, storageBus -> {
            storageBus.setPriority(1);
        });
        plot.chest(origin.above());

        // Energy connection between the P2P-net and the carrier net
        plot.cable(origin.east().above());
        plot.cable(origin.east().east().above())
                .part(Direction.WEST, AEParts.QUARTZ_FIBER);

        plot.test(helper -> {
            helper.succeedWhen(() -> {
                helper.assertContainerEmpty(origin.west().west().west());
                helper.assertContainerContains(origin.east().east().east(), Items.BEDROCK);
            });
        });
    }

    @TestPlot("p2p_items")
    public static void item(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        placeTunnel(plot, AEParts.ITEM_P2P_TUNNEL);

        // Hopper pointing into the input P2P
        plot.hopper(origin.west().west(), Direction.EAST, new ItemStack(Items.BEDROCK));
        // Chest adjacent to output
        var chestPos = origin.east().east();
        plot.chest(chestPos);

        plot.test(helper -> {
            helper.succeedWhen(() -> {
                helper.assertContainerContains(chestPos, Items.BEDROCK);
            });
        });
    }

    /**
     * Builds a system that uses an export bus to export fluids into a tank via a P2P fluid tunnel.
     */
    @TestPlot("p2p_fluids")
    public static void fluid(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        placeTunnel(plot, AEParts.FLUID_P2P_TUNNEL);

        var outputPos = origin.east().east();
        plot.block(outputPos, AEBlocks.SKY_STONE_TANK);
        plot.cable(origin.west().west())
                .part(Direction.EAST, AEParts.EXPORT_BUS, part -> {
                    part.getConfig().addFilter(Fluids.WATER);
                });
        plot.creativeEnergyCell(origin.west().west().below());
        plot.drive(origin.west().west().above())
                .addCreativeCell()
                .add(Fluids.WATER);
        plot.test(helper -> {
            helper.succeedWhen(() -> {
                var tank = (SkyStoneTankBlockEntity) helper.getBlockEntity(outputPos);
                var storage = tank.getStorage();
                helper.check(
                        new FluidStack(Fluids.WATER, 1).isFluidEqual(storage.getFluid()),
                        "No water stored");
                helper.check(
                        storage.getFluidAmount() > 0,
                        "No amount >0 stored");
            });
        });
    }

    @TestPlot("p2p_energy")
    public static void energy(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        placeTunnel(plot, AEParts.FE_P2P_TUNNEL);

        plot.block(origin.west().west(), AEBlocks.DEBUG_ENERGY_GEN);
        plot.block(origin.east().east(), AEBlocks.ENERGY_ACCEPTOR);
        var cellPos = origin.east().east().above();
        plot.block(cellPos, AEBlocks.ENERGY_CELL);
        var cellEnergy = new MutableDouble(0);
        plot.test(helper -> {
            helper.startSequence()
                    .thenIdle(10)
                    .thenWaitUntil(() -> {
                        var cell = (EnergyCellBlockEntity) helper.getBlockEntity(cellPos);
                        cellEnergy.setValue(cell.getAECurrentPower());
                    })
                    .thenIdle(10)
                    .thenWaitUntil(() -> {
                        var cell = (EnergyCellBlockEntity) helper.getBlockEntity(cellPos);
                        helper.check(
                                cell.getAECurrentPower() > cellEnergy.getValue(),
                                "Cell should start charging through the P2P tunnel");
                    })
                    .thenSucceed();
        });
    }

    @TestPlot("p2p_light")
    public static void light(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        placeTunnel(plot, AEParts.LIGHT_P2P_TUNNEL);
        plot.block(origin.west().west(), Blocks.REDSTONE_LAMP);
        var leverPos = origin.west().west().above();
        plot.block(leverPos, Blocks.LEVER);
        var outputPos = origin.east().east();
        plot.test(helper -> {
            MutableInt lightLevel = new MutableInt(0);
            helper.startSequence()
                    .thenIdle(20)
                    .thenExecute(() -> {
                        lightLevel.setValue(
                                helper.getLevel().getBrightness(LightLayer.BLOCK, helper.absolutePos(outputPos)));
                        helper.pullLever(leverPos);
                    })
                    .thenWaitUntil(() -> {
                        var newLightLevel = helper.getLevel().getBrightness(LightLayer.BLOCK,
                                helper.absolutePos(outputPos));
                        helper.check(
                                newLightLevel > lightLevel.getValue(),
                                "Light-Level didn't increase");
                    })
                    .thenExecute(() -> helper.pullLever(leverPos))
                    .thenWaitUntil(() -> {
                        var newLightLevel = helper.getLevel().getBrightness(LightLayer.BLOCK,
                                helper.absolutePos(outputPos));
                        helper.check(
                                newLightLevel <= lightLevel.getValue(),
                                "Light-Level didn't reset");
                    })
                    .thenSucceed();
        });
    }

    /**
     * Tests that when a P2P tunnel is allocated/unallocated a channel, it connects/disconnects correctly.
     */
    @TestPlot("p2p_channel_reconnect_behavior")
    public static void testOutOfChannelReconnectBehavior(PlotBuilder plot) {
        var origin = BlockPos.ZERO;

        plot.creativeEnergyCell(origin.below());
        plot.block(origin, AEBlocks.CONTROLLER);

        // Build the west loop with a power connection into the P2P grid
        plot.cable(origin.west()).part(Direction.WEST, AEParts.ME_P2P_TUNNEL);
        plot.cable(origin.west().north());
        plot.cable(origin.west().north().west());
        plot.cable(origin.west().north().west().south())
                .part(Direction.NORTH, AEParts.QUARTZ_FIBER);

        // Build the east loop with the toggleable channel-sink
        // first block uses 4 channels
        plot.cable(origin.east())
                .part(Direction.NORTH, AEParts.TERMINAL)
                .part(Direction.SOUTH, AEParts.TERMINAL)
                .part(Direction.UP, AEParts.TERMINAL)
                .part(Direction.DOWN, AEParts.TERMINAL);
        // next block uses 3 to leave 1 for the P2P
        plot.cable(origin.east().east())
                .part(Direction.NORTH, AEParts.TERMINAL)
                .part(Direction.UP, AEParts.TERMINAL)
                .part(Direction.DOWN, AEParts.TERMINAL)
                .part(Direction.SOUTH, AEParts.TOGGLE_BUS);
        // lever to toggle the toggle bus
        var leverPos = origin.east().east().north();
        plot.block(leverPos, Blocks.REDSTONE_LAMP);
        plot.leverOn(leverPos.above().above(), Direction.DOWN);
        // the toggle-bus-branch with another terminal on it to consume the full 8 channels
        plot.cable(origin.east().east().south())
                .part(Direction.UP, AEParts.TERMINAL);

        // Make a somewhat winded cable to be long enough, so it's longer than the path via the toggle-bus node
        // to the terminal
        plot.cable(origin.east().east().east())
                .part(Direction.EAST, AEParts.CABLE_ANCHOR);
        plot.cable(origin.east().east().east().north());
        plot.cable(origin.east().east().east().north().east())
                .part(Direction.EAST, AEParts.CABLE_ANCHOR);
        plot.cable(origin.east().east().east().north().east().south());
        plot.cable(origin.east().east().east().north().east().south().east());
        var p2pOutputPos = origin.east().east().east().north().east().south().east().north();
        plot.cable(p2pOutputPos).part(Direction.EAST, AEParts.ME_P2P_TUNNEL);
        // This monitor will only be lit if the connection exists since it would have no power otherwise
        plot.cable(p2pOutputPos.east()).part(Direction.UP, AEParts.MONITOR);

        plot.test(helper -> {
            var freq = new MutableShort();
            var lightPanel = new MutableObject<AbstractPanelPart>();
            helper.startSequence()
                    .thenWaitUntil(() -> helper.getGrid(origin))
                    .thenExecute(() -> {
                        lightPanel.setValue(helper.getPart(p2pOutputPos.east(), Direction.UP, PanelPart.class));

                        var grid = helper.getGrid(origin);
                        var inputPos = helper.absolutePos(origin.west());
                        var outputPos = helper.absolutePos(p2pOutputPos);
                        freq.setValue(linkTunnels(grid, MEP2PTunnelPart.class, inputPos, outputPos));
                    })
                    .thenWaitUntil(() -> {
                        helper.check(
                                lightPanel.getValue().getMainNode().isOnline(),
                                "The panel should initially be on");
                    })
                    // This toggles the toggle bus and will make the P2P run out of channels
                    .thenExecute(() -> helper.pullLever(leverPos.above()))
                    .thenWaitUntil(() -> {
                        var inputTunnel = helper.getPart(p2pOutputPos, Direction.EAST, MEP2PTunnelPart.class);
                        if (inputTunnel.getMainNode().isOnline()) {
                            helper.fail("should be offline", p2pOutputPos);
                        }
                    })
                    .thenWaitUntil(() -> {
                        if (lightPanel.getValue().getMainNode().isOnline()) {
                            helper.fail("should be offline", p2pOutputPos.east());
                        }
                    })
                    // This toggles the toggle bus and will make the P2P get its channel back
                    .thenExecute(() -> helper.pullLever(leverPos.above()))
                    .thenWaitUntil(() -> {
                        var inputTunnel = helper.getPart(p2pOutputPos, Direction.EAST, MEP2PTunnelPart.class);
                        if (!inputTunnel.getMainNode().isOnline()) {
                            helper.fail("should be online", p2pOutputPos);
                        }
                    })
                    .thenWaitUntil(() -> {
                        if (!lightPanel.getValue().getMainNode().isOnline()) {
                            helper.fail("should be online", p2pOutputPos.east());
                        }
                    })
                    .thenSucceed();
        });
    }

    private static <T extends P2PTunnelPart<?>> void placeTunnel(PlotBuilder plot, ItemDefinition<PartItem<T>> tunnel) {
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

    private static <T extends P2PTunnelPart<?>> short linkTunnels(IGrid grid, Class<T> tunnelType, BlockPos inputPos,
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
}
