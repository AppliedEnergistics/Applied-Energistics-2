package appeng.server.testplots;

import java.util.Objects;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

import appeng.blockentity.networking.EnergyCellBlockEntity;
import appeng.blockentity.storage.SkyStoneTankBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.me.service.P2PService;
import appeng.parts.AEBasePart;
import appeng.parts.p2p.P2PTunnelPart;
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
                        FluidVariant.of(Fluids.WATER).equals(storage.variant),
                        "No water stored");
                helper.check(
                        storage.amount > 0,
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

    private static <T extends P2PTunnelPart<?>> void placeTunnel(PlotBuilder plot, ItemDefinition<PartItem<T>> tunnel) {
        var origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.cable(origin);
        plot.cable(origin.west()).part(Direction.WEST, tunnel);
        plot.cable(origin.east()).part(Direction.EAST, tunnel);
        plot.afterGridInitAt(origin, (grid, gridNode) -> {
            BlockPos absOrigin = ((AEBasePart) gridNode.getOwner()).getBlockEntity().getBlockPos();

            var p2p = P2PService.get(grid);
            T inputTunnel = null;
            T outputTunnel = null;
            for (T p2pPart : grid.getMachines(tunnel.asItem().getPartClass())) {
                if (p2pPart.getBlockEntity().getBlockPos().equals(absOrigin.west())) {
                    inputTunnel = p2pPart;
                } else if (p2pPart.getBlockEntity().getBlockPos().equals(absOrigin.east())) {
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
        });
    }
}
