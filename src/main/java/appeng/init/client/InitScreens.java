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

import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
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
import appeng.client.gui.implementations.EnergyLevelEmitterScreen;
import appeng.client.gui.implementations.FormationPlaneScreen;
import appeng.client.gui.implementations.IOBusScreen;
import appeng.client.gui.implementations.IOPortScreen;
import appeng.client.gui.implementations.InscriberScreen;
import appeng.client.gui.implementations.InterfaceScreen;
import appeng.client.gui.implementations.LevelEmitterScreen;
import appeng.client.gui.implementations.MolecularAssemblerScreen;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.implementations.PriorityScreen;
import appeng.client.gui.implementations.QNBScreen;
import appeng.client.gui.implementations.QuartzKnifeScreen;
import appeng.client.gui.implementations.SecurityStationScreen;
import appeng.client.gui.implementations.SkyChestScreen;
import appeng.client.gui.implementations.SpatialAnchorScreen;
import appeng.client.gui.implementations.SpatialIOPortScreen;
import appeng.client.gui.implementations.StorageBusScreen;
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
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.CellWorkbenchMenu;
import appeng.menu.implementations.ChestMenu;
import appeng.menu.implementations.CondenserMenu;
import appeng.menu.implementations.DriveMenu;
import appeng.menu.implementations.EnergyLevelEmitterMenu;
import appeng.menu.implementations.FormationPlaneMenu;
import appeng.menu.implementations.IOBusMenu;
import appeng.menu.implementations.IOPortMenu;
import appeng.menu.implementations.InscriberMenu;
import appeng.menu.implementations.InterfaceMenu;
import appeng.menu.implementations.InterfaceTerminalMenu;
import appeng.menu.implementations.LevelEmitterMenu;
import appeng.menu.implementations.MolecularAssemblerMenu;
import appeng.menu.implementations.PatternProviderMenu;
import appeng.menu.implementations.PriorityMenu;
import appeng.menu.implementations.QNBMenu;
import appeng.menu.implementations.QuartzKnifeMenu;
import appeng.menu.implementations.SecurityStationMenu;
import appeng.menu.implementations.SkyChestMenu;
import appeng.menu.implementations.SpatialAnchorMenu;
import appeng.menu.implementations.SpatialIOPortMenu;
import appeng.menu.implementations.StorageBusMenu;
import appeng.menu.implementations.VibrationChamberMenu;
import appeng.menu.implementations.WirelessMenu;
import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingCPUMenu;
import appeng.menu.me.crafting.CraftingStatusMenu;
import appeng.menu.me.fluids.FluidTerminalMenu;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.me.items.ItemTerminalMenu;
import appeng.menu.me.items.MEPortableCellMenu;
import appeng.menu.me.items.PatternTermMenu;
import appeng.menu.me.items.WirelessTermMenu;
import appeng.menu.me.networktool.NetworkStatusMenu;
import appeng.menu.me.networktool.NetworkToolMenu;

/**
 * The server sends the client a menu identifier, which the client then maps onto a screen using {@link MenuScreens}.
 * This class registers our screens.
 */
public final class InitScreens {

    @VisibleForTesting
    static final Map<MenuType<?>, String> MENU_STYLES = new IdentityHashMap<>();

    private InitScreens() {
    }

    public static void init() {
        register(QNBMenu.TYPE, QNBScreen::new, "/screens/qnb.json");
        register(SkyChestMenu.TYPE, SkyChestScreen::new, "/screens/sky_chest.json");
        register(ChestMenu.TYPE, ChestScreen::new, "/screens/chest.json");
        register(WirelessMenu.TYPE, WirelessScreen::new, "/screens/wireless.json");
        register(NetworkStatusMenu.TYPE, NetworkStatusScreen::new, "/screens/network_status.json");
        InitScreens.<CraftingCPUMenu, CraftingCPUScreen<CraftingCPUMenu>>register(
                CraftingCPUMenu.TYPE,
                CraftingCPUScreen::new,
                "/screens/crafting_cpu.json");
        register(NetworkToolMenu.TYPE, NetworkToolScreen::new, "/screens/network_tool.json");
        register(QuartzKnifeMenu.TYPE, QuartzKnifeScreen::new, "/screens/quartz_knife.json");
        register(DriveMenu.TYPE, DriveScreen::new, "/screens/drive.json");
        register(VibrationChamberMenu.TYPE, VibrationChamberScreen::new, "/screens/vibration_chamber.json");
        register(CondenserMenu.TYPE, CondenserScreen::new, "/screens/condenser.json");
        register(InterfaceMenu.ITEM_TYPE, InterfaceScreen::new, "/screens/item_interface.json");
        register(InterfaceMenu.FLUID_TYPE, InterfaceScreen::new, "/screens/fluid_interface.json");
        register(IOBusMenu.ITEM_EXPORT_TYPE, IOBusScreen::new, "/screens/item_export_bus.json");
        register(IOBusMenu.ITEM_IMPORT_TYPE, IOBusScreen::new, "/screens/item_import_bus.json");
        register(IOBusMenu.FLUID_IMPORT_TYPE, IOBusScreen::new, "/screens/fluid_import_bus.json");
        register(IOBusMenu.FLUID_EXPORT_TYPE, IOBusScreen::new, "/screens/fluid_export_bus.json");
        register(IOPortMenu.TYPE, IOPortScreen::new, "/screens/io_port.json");
        register(StorageBusMenu.ITEM_TYPE, StorageBusScreen::new, "/screens/item_storage_bus.json");
        register(StorageBusMenu.FLUID_TYPE, StorageBusScreen::new, "/screens/fluid_storage_bus.json");
        register(FormationPlaneMenu.ITEM_TYPE, FormationPlaneScreen::new, "/screens/item_formation_plane.json");
        register(FormationPlaneMenu.FLUID_TYPE, FormationPlaneScreen::new, "/screens/fluid_formation_plane.json");
        register(PriorityMenu.TYPE, PriorityScreen::new, "/screens/priority.json");
        register(LevelEmitterMenu.ITEM_TYPE, LevelEmitterScreen::new, "/screens/item_level_emitter.json");
        register(LevelEmitterMenu.FLUID_TYPE, LevelEmitterScreen::new, "/screens/fluid_level_emitter.json");
        register(EnergyLevelEmitterMenu.TYPE, EnergyLevelEmitterScreen::new, "/screens/energy_level_emitter.json");
        register(SpatialIOPortMenu.TYPE, SpatialIOPortScreen::new, "/screens/spatial_io_port.json");
        register(InscriberMenu.TYPE, InscriberScreen::new, "/screens/inscriber.json");
        register(CellWorkbenchMenu.TYPE, CellWorkbenchScreen::new, "/screens/cell_workbench.json");
        register(PatternProviderMenu.TYPE, PatternProviderScreen::new, "/screens/pattern_provider.json");
        register(MolecularAssemblerMenu.TYPE, MolecularAssemblerScreen::new, "/screens/molecular_assembler.json");
        register(CraftAmountMenu.TYPE, CraftAmountScreen::new, "/screens/craft_amount.json");
        register(CraftConfirmMenu.TYPE, CraftConfirmScreen::new, "/screens/craft_confirm.json");
        register(InterfaceTerminalMenu.TYPE, InterfaceTerminalScreen::new, "/screens/interface_terminal.json");
        register(CraftingStatusMenu.TYPE, CraftingStatusScreen::new, "/screens/crafting_status.json");
        register(SpatialAnchorMenu.TYPE, SpatialAnchorScreen::new, "/screens/spatial_anchor.json");

        // Terminals
        InitScreens.<ItemTerminalMenu, ItemTerminalScreen<ItemTerminalMenu>>register(
                ItemTerminalMenu.TYPE,
                ItemTerminalScreen::new,
                "/screens/terminals/item_terminal.json");
        InitScreens.<MEPortableCellMenu, ItemTerminalScreen<MEPortableCellMenu>>register(
                MEPortableCellMenu.TYPE,
                ItemTerminalScreen::new,
                "/screens/terminals/portable_cell.json");
        InitScreens.<WirelessTermMenu, ItemTerminalScreen<WirelessTermMenu>>register(
                WirelessTermMenu.TYPE,
                ItemTerminalScreen::new,
                "/screens/terminals/wireless_terminal.json");
        register(SecurityStationMenu.TYPE,
                SecurityStationScreen::new,
                "/screens/terminals/security_station.json");
        register(
                CraftingTermMenu.TYPE,
                CraftingTermScreen::new,
                "/screens/terminals/crafting_terminal.json");
        register(
                PatternTermMenu.TYPE,
                PatternTermScreen::new,
                "/screens/terminals/pattern_terminal.json");
        register(
                FluidTerminalMenu.TYPE,
                FluidTerminalScreen::new,
                "/screens/terminals/fluid_terminal.json");
    }

    /**
     * Registers a screen for a given menu and ensures the given style is applied after opening the screen.
     */
    private static <M extends AEBaseMenu, U extends AEBaseScreen<M>> void register(MenuType<M> type,
            StyledScreenFactory<M, U> factory,
            String stylePath) {
        MENU_STYLES.put(type, stylePath);
        ScreenRegistry.<M, U>register(type, (menu, playerInv, title) -> {
            ScreenStyle style;
            try {
                style = StyleManager.loadStyleDoc(stylePath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Failed to read Screen JSON file: " + stylePath + ": " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException("Failed to read Screen JSON file: " + stylePath, e);
            }

            return factory.create(menu, playerInv, title, style);
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
