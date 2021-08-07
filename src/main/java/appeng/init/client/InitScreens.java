/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.init.client;

import java.io.FileNotFoundException;
import java.util.IdentityHashMap;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.CellWorkbenchScreen;
import appeng.client.gui.implementations.ChestScreen;
import appeng.client.gui.implementations.CondenserScreen;
import appeng.client.gui.implementations.DriveScreen;
import appeng.client.gui.implementations.FluidFormationPlaneScreen;
import appeng.client.gui.implementations.FluidIOBusScreen;
import appeng.client.gui.implementations.FluidInterfaceScreen;
import appeng.client.gui.implementations.FluidLevelEmitterScreen;
import appeng.client.gui.implementations.FluidStorageBusScreen;
import appeng.client.gui.implementations.GrinderScreen;
import appeng.client.gui.implementations.IOPortScreen;
import appeng.client.gui.implementations.InscriberScreen;
import appeng.client.gui.implementations.ItemFormationPlaneScreen;
import appeng.client.gui.implementations.ItemIOBusScreen;
import appeng.client.gui.implementations.ItemInterfaceScreen;
import appeng.client.gui.implementations.ItemLevelEmitterScreen;
import appeng.client.gui.implementations.ItemStorageBusScreen;
import appeng.client.gui.implementations.MolecularAssemblerScreen;
import appeng.client.gui.implementations.PriorityScreen;
import appeng.client.gui.implementations.QNBScreen;
import appeng.client.gui.implementations.QuartzKnifeScreen;
import appeng.client.gui.implementations.SecurityStationScreen;
import appeng.client.gui.implementations.SkyChestScreen;
import appeng.client.gui.implementations.SpatialAnchorScreen;
import appeng.client.gui.implementations.SpatialIOPortScreen;
import appeng.client.gui.implementations.VibrationChamberScreen;
import appeng.client.gui.implementations.WirelessScreen;
import appeng.client.gui.me.crafting.CraftAmountScreen;
import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.client.gui.me.crafting.CraftingStatusScreen;
import appeng.client.gui.me.fluids.FluidTerminalScreen;
import appeng.client.gui.me.interfaceterminal.InterfaceTerminalScreen;
import appeng.client.gui.me.items.CraftingTermScreen;
import appeng.client.gui.me.items.ItemTerminalScreen;
import appeng.client.gui.me.items.PatternTermScreen;
import appeng.client.gui.me.networktool.NetworkStatusScreen;
import appeng.client.gui.me.networktool.NetworkToolScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.menu.AEBaseContainer;
import appeng.menu.implementations.CellWorkbenchContainer;
import appeng.menu.implementations.ChestContainer;
import appeng.menu.implementations.CondenserContainer;
import appeng.menu.implementations.DriveContainer;
import appeng.menu.implementations.FluidFormationPlaneContainer;
import appeng.menu.implementations.FluidIOBusContainer;
import appeng.menu.implementations.FluidInterfaceContainer;
import appeng.menu.implementations.FluidLevelEmitterContainer;
import appeng.menu.implementations.FluidStorageBusContainer;
import appeng.menu.implementations.GrinderContainer;
import appeng.menu.implementations.IOPortContainer;
import appeng.menu.implementations.InscriberContainer;
import appeng.menu.implementations.InterfaceTerminalContainer;
import appeng.menu.implementations.ItemFormationPlaneContainer;
import appeng.menu.implementations.ItemIOBusContainer;
import appeng.menu.implementations.ItemInterfaceContainer;
import appeng.menu.implementations.ItemLevelEmitterContainer;
import appeng.menu.implementations.ItemStorageBusContainer;
import appeng.menu.implementations.MolecularAssemblerContainer;
import appeng.menu.implementations.PriorityContainer;
import appeng.menu.implementations.QNBContainer;
import appeng.menu.implementations.QuartzKnifeContainer;
import appeng.menu.implementations.SecurityStationContainer;
import appeng.menu.implementations.SkyChestContainer;
import appeng.menu.implementations.SpatialAnchorContainer;
import appeng.menu.implementations.SpatialIOPortContainer;
import appeng.menu.implementations.VibrationChamberContainer;
import appeng.menu.implementations.WirelessContainer;
import appeng.menu.me.crafting.CraftAmountContainer;
import appeng.menu.me.crafting.CraftConfirmContainer;
import appeng.menu.me.crafting.CraftingCPUContainer;
import appeng.menu.me.crafting.CraftingStatusContainer;
import appeng.menu.me.fluids.FluidTerminalContainer;
import appeng.menu.me.items.CraftingTermContainer;
import appeng.menu.me.items.ItemTerminalContainer;
import appeng.menu.me.items.MEPortableCellContainer;
import appeng.menu.me.items.PatternTermContainer;
import appeng.menu.me.items.WirelessTermContainer;
import appeng.menu.me.networktool.NetworkStatusContainer;
import appeng.menu.me.networktool.NetworkToolContainer;

/**
 * The server sends the client a menu identifier, which the client then maps onto a screen using
 * {@link MenuScreens}. This class registers our screens.
 */
public final class InitScreens {

    @VisibleForTesting
    static final Map<MenuType<?>, String> CONTAINER_STYLES = new IdentityHashMap<>();

    private InitScreens() {
    }

    public static void init() {
        register(GrinderContainer.TYPE, GrinderScreen::new, "/screens/grinder.json");
        register(QNBContainer.TYPE, QNBScreen::new, "/screens/qnb.json");
        register(SkyChestContainer.TYPE, SkyChestScreen::new, "/screens/sky_chest.json");
        register(ChestContainer.TYPE, ChestScreen::new, "/screens/chest.json");
        register(WirelessContainer.TYPE, WirelessScreen::new, "/screens/wireless.json");
        register(NetworkStatusContainer.TYPE, NetworkStatusScreen::new, "/screens/network_status.json");
        InitScreens.<CraftingCPUContainer, CraftingCPUScreen<CraftingCPUContainer>>register(
                CraftingCPUContainer.TYPE,
                CraftingCPUScreen::new,
                "/screens/crafting_cpu.json");
        register(NetworkToolContainer.TYPE, NetworkToolScreen::new, "/screens/network_tool.json");
        register(QuartzKnifeContainer.TYPE, QuartzKnifeScreen::new, "/screens/quartz_knife.json");
        register(DriveContainer.TYPE, DriveScreen::new, "/screens/drive.json");
        register(VibrationChamberContainer.TYPE, VibrationChamberScreen::new, "/screens/vibration_chamber.json");
        register(CondenserContainer.TYPE, CondenserScreen::new, "/screens/condenser.json");
        register(ItemInterfaceContainer.TYPE, ItemInterfaceScreen::new, "/screens/interface.json");
        register(FluidInterfaceContainer.TYPE, FluidInterfaceScreen::new, "/screens/fluid_interface.json");
        register(ItemIOBusContainer.EXPORT_TYPE, ItemIOBusScreen::new, "/screens/export_bus.json");
        register(ItemIOBusContainer.IMPORT_TYPE, ItemIOBusScreen::new, "/screens/import_bus.json");
        register(FluidIOBusContainer.IMPORT_TYPE, FluidIOBusScreen::new, "/screens/fluid_import_bus.json");
        register(FluidIOBusContainer.EXPORT_TYPE, FluidIOBusScreen::new, "/screens/fluid_export_bus.json");
        register(IOPortContainer.TYPE, IOPortScreen::new, "/screens/io_port.json");
        register(ItemStorageBusContainer.TYPE, ItemStorageBusScreen::new, "/screens/storage_bus.json");
        register(FluidStorageBusContainer.TYPE, FluidStorageBusScreen::new, "/screens/fluid_storage_bus.json");
        register(ItemFormationPlaneContainer.TYPE, ItemFormationPlaneScreen::new, "/screens/formation_plane.json");
        register(FluidFormationPlaneContainer.TYPE, FluidFormationPlaneScreen::new,
                "/screens/fluid_formation_plane.json");
        register(PriorityContainer.TYPE, PriorityScreen::new, "/screens/priority.json");
        register(ItemLevelEmitterContainer.TYPE, ItemLevelEmitterScreen::new, "/screens/level_emitter.json");
        register(FluidLevelEmitterContainer.TYPE, FluidLevelEmitterScreen::new, "/screens/fluid_level_emitter.json");
        register(SpatialIOPortContainer.TYPE, SpatialIOPortScreen::new, "/screens/spatial_io_port.json");
        register(InscriberContainer.TYPE, InscriberScreen::new, "/screens/inscriber.json");
        register(CellWorkbenchContainer.TYPE, CellWorkbenchScreen::new, "/screens/cell_workbench.json");
        register(MolecularAssemblerContainer.TYPE, MolecularAssemblerScreen::new, "/screens/molecular_assembler.json");
        register(CraftAmountContainer.TYPE, CraftAmountScreen::new, "/screens/craft_amount.json");
        register(CraftConfirmContainer.TYPE, CraftConfirmScreen::new, "/screens/craft_confirm.json");
        register(InterfaceTerminalContainer.TYPE, InterfaceTerminalScreen::new, "/screens/interface_terminal.json");
        register(CraftingStatusContainer.TYPE, CraftingStatusScreen::new, "/screens/crafting_status.json");
        register(SpatialAnchorContainer.TYPE, SpatialAnchorScreen::new, "/screens/spatial_anchor.json");

        // Terminals
        InitScreens.<ItemTerminalContainer, ItemTerminalScreen<ItemTerminalContainer>>register(
                ItemTerminalContainer.TYPE,
                ItemTerminalScreen::new,
                "/screens/terminals/item_terminal.json");
        InitScreens.<MEPortableCellContainer, ItemTerminalScreen<MEPortableCellContainer>>register(
                MEPortableCellContainer.TYPE,
                ItemTerminalScreen::new,
                "/screens/terminals/portable_cell.json");
        InitScreens.<WirelessTermContainer, ItemTerminalScreen<WirelessTermContainer>>register(
                WirelessTermContainer.TYPE,
                ItemTerminalScreen::new,
                "/screens/terminals/wireless_terminal.json");
        register(SecurityStationContainer.TYPE,
                SecurityStationScreen::new,
                "/screens/terminals/security_station.json");
        register(
                CraftingTermContainer.TYPE,
                CraftingTermScreen::new,
                "/screens/terminals/crafting_terminal.json");
        register(
                PatternTermContainer.TYPE,
                PatternTermScreen::new,
                "/screens/terminals/pattern_terminal.json");
        register(
                FluidTerminalContainer.TYPE,
                FluidTerminalScreen::new,
                "/screens/terminals/fluid_terminal.json");
    }

    /**
     * Registers a screen for a given menu and ensures the given style is applied after opening the screen.
     */
    private static <M extends AEBaseContainer, U extends AEBaseScreen<M>> void register(MenuType<M> type,
            StyledScreenFactory<M, U> factory,
            String stylePath) {
        CONTAINER_STYLES.put(type, stylePath);
        MenuScreens.<M, U>register(type, (container, playerInv, title) -> {
            ScreenStyle style;
            try {
                style = StyleManager.loadStyleDoc(stylePath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Failed to read Screen JSON file: " + stylePath + ": " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException("Failed to read Screen JSON file: " + stylePath, e);
            }

            return factory.create(container, playerInv, title, style);
        });
    }

    /**
     * A type definition that matches the constructors of our screens, which take an additional {@link ScreenStyle}
     * argument.
     */
    @FunctionalInterface
    public interface StyledScreenFactory<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> {
        U create(T t, Inventory pi, Component title, ScreenStyle style);
    }

}
