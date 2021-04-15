package appeng.client.gui;

import appeng.client.gui.implementations.CellWorkbenchScreen;
import appeng.client.gui.implementations.ChestScreen;
import appeng.client.gui.implementations.CondenserScreen;
import appeng.client.gui.implementations.DriveScreen;
import appeng.client.gui.implementations.FormationPlaneScreen;
import appeng.client.gui.implementations.GrinderScreen;
import appeng.client.gui.implementations.IOBusScreen;
import appeng.client.gui.implementations.IOPortScreen;
import appeng.client.gui.implementations.InscriberScreen;
import appeng.client.gui.implementations.InterfaceScreen;
import appeng.client.gui.implementations.LevelEmitterScreen;
import appeng.client.gui.implementations.MolecularAssemblerScreen;
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
import appeng.client.gui.me.common.TerminalStyles;
import appeng.client.gui.me.crafting.CraftAmountScreen;
import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.client.gui.me.crafting.CraftingStatusScreen;
import appeng.client.gui.me.fluids.FluidTerminalScreen;
import appeng.client.gui.me.interfaceterminal.InterfaceTerminalScreen;
import appeng.client.gui.me.items.CraftingTermScreen;
import appeng.client.gui.me.items.ItemTerminalScreen;
import appeng.client.gui.me.items.MEPortableCellScreen;
import appeng.client.gui.me.items.PatternTermScreen;
import appeng.client.gui.me.items.WirelessTermScreen;
import appeng.client.gui.me.networktool.NetworkStatusScreen;
import appeng.client.gui.me.networktool.NetworkToolScreen;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.CellWorkbenchContainer;
import appeng.container.implementations.ChestContainer;
import appeng.container.implementations.CondenserContainer;
import appeng.container.implementations.DriveContainer;
import appeng.container.implementations.FormationPlaneContainer;
import appeng.container.implementations.GrinderContainer;
import appeng.container.implementations.IOBusContainer;
import appeng.container.implementations.IOPortContainer;
import appeng.container.implementations.InscriberContainer;
import appeng.container.implementations.InterfaceContainer;
import appeng.container.implementations.InterfaceTerminalContainer;
import appeng.container.implementations.LevelEmitterContainer;
import appeng.container.implementations.MolecularAssemblerContainer;
import appeng.container.implementations.PriorityContainer;
import appeng.container.implementations.QNBContainer;
import appeng.container.implementations.QuartzKnifeContainer;
import appeng.container.implementations.SecurityStationContainer;
import appeng.container.implementations.SkyChestContainer;
import appeng.container.implementations.SpatialAnchorContainer;
import appeng.container.implementations.SpatialIOPortContainer;
import appeng.container.implementations.StorageBusContainer;
import appeng.container.implementations.VibrationChamberContainer;
import appeng.container.implementations.WirelessContainer;
import appeng.container.me.crafting.CraftAmountContainer;
import appeng.container.me.crafting.CraftConfirmContainer;
import appeng.container.me.crafting.CraftingCPUContainer;
import appeng.container.me.crafting.CraftingStatusContainer;
import appeng.container.me.fluids.FluidTerminalContainer;
import appeng.container.me.items.CraftingTermContainer;
import appeng.container.me.items.ItemTerminalContainer;
import appeng.container.me.items.MEPortableCellContainer;
import appeng.container.me.items.PatternTermContainer;
import appeng.container.me.items.WirelessTermContainer;
import appeng.container.me.networktool.NetworkStatusContainer;
import appeng.container.me.networktool.NetworkToolContainer;
import appeng.fluids.client.gui.FluidFormationPlaneScreen;
import appeng.fluids.client.gui.FluidIOScreen;
import appeng.fluids.client.gui.FluidInterfaceScreen;
import appeng.fluids.client.gui.FluidLevelEmitterScreen;
import appeng.fluids.client.gui.FluidStorageBusScreen;
import appeng.fluids.container.FluidFormationPlaneContainer;
import appeng.fluids.container.FluidIOContainer;
import appeng.fluids.container.FluidInterfaceContainer;
import appeng.fluids.container.FluidLevelEmitterContainer;
import appeng.fluids.container.FluidStorageBusContainer;
import com.google.common.annotations.VisibleForTesting;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * The server sends the client a container identifier, which the client then maps onto a screen using
 * {@link net.minecraft.client.gui.ScreenManager}. This class registers our screens.
 */
public class ScreenRegistration {

    @VisibleForTesting
    static final Map<ContainerType<?>, String> CONTAINER_STYLES = new IdentityHashMap<>();

    public static void register() {
        register(GrinderContainer.TYPE, GrinderScreen::new, "/screens/grinder.json");
        register(QNBContainer.TYPE, QNBScreen::new, "/screens/qnb.json");
        register(SkyChestContainer.TYPE, SkyChestScreen::new, "/screens/sky_chest.json");
        register(ChestContainer.TYPE, ChestScreen::new, "/screens/chest.json");
        register(WirelessContainer.TYPE, WirelessScreen::new, "/screens/wireless.json");
        register(
                ItemTerminalContainer.TYPE,
                TerminalStyles.ITEM_TERMINAL.<ItemTerminalContainer, ItemTerminalScreen<ItemTerminalContainer>>factory(ItemTerminalScreen::new),
                "/screens/item_terminal.json"
        );
        register(
                MEPortableCellContainer.TYPE,
                TerminalStyles.PORTABLE_CELL.factory(MEPortableCellScreen::new),
                "/screens/portable_cell.json"
        );
        register(
                WirelessTermContainer.TYPE,
                TerminalStyles.WIRELESS_TERMINAL.factory(WirelessTermScreen::new),
                "/screens/wireless_terminal.json"
        );
        register(NetworkStatusContainer.TYPE, NetworkStatusScreen::new, "/screens/network_status.json");
        ScreenRegistration.<CraftingCPUContainer, CraftingCPUScreen<CraftingCPUContainer>>register(
                CraftingCPUContainer.TYPE,
                CraftingCPUScreen::new,
                "/screens/crafting_cpu.json"
        );
        register(NetworkToolContainer.TYPE, NetworkToolScreen::new, "/screens/network_tool.json");
        register(QuartzKnifeContainer.TYPE, QuartzKnifeScreen::new, "/screens/quartz_knife.json");
        register(DriveContainer.TYPE, DriveScreen::new, "/screens/drive.json");
        register(VibrationChamberContainer.TYPE, VibrationChamberScreen::new, "/screens/vibration_chamber.json");
        register(CondenserContainer.TYPE, CondenserScreen::new, "/screens/condenser.json");
        register(InterfaceContainer.TYPE, InterfaceScreen::new, "/screens/interface.json");
        register(FluidInterfaceContainer.TYPE, FluidInterfaceScreen::new, "/screens/fluid_interface.json");
        register(IOBusContainer.TYPE, IOBusScreen::new, "/screens/io_bus.json");
        register(FluidIOContainer.IMPORT_TYPE, FluidIOScreen::new, "/screens/fluid_import_bus.json");
        register(FluidIOContainer.EXPORT_TYPE, FluidIOScreen::new, "/screens/fluid_export_bus.json");
        register(IOPortContainer.TYPE, IOPortScreen::new, "/screens/io_port.json");
        register(StorageBusContainer.TYPE, StorageBusScreen::new, "/screens/storage_bus.json");
        register(FluidStorageBusContainer.TYPE, FluidStorageBusScreen::new, "/screens/fluid_storage_bus.json");
        register(FormationPlaneContainer.TYPE, FormationPlaneScreen::new, "/screens/formation_plane.json");
        register(FluidFormationPlaneContainer.TYPE, FluidFormationPlaneScreen::new, "/screens/fluid_formation_plane.json");
        register(PriorityContainer.TYPE, PriorityScreen::new, "/screens/priority.json");
        register(SecurityStationContainer.TYPE,
                TerminalStyles.SECURITY_STATION
                        .<SecurityStationContainer, SecurityStationScreen>factory(SecurityStationScreen::new), "/screens/security_station.json");
        register(
                CraftingTermContainer.TYPE,
                TerminalStyles.CRAFTING_TERMINAL
                        .<CraftingTermContainer, CraftingTermScreen>factory(CraftingTermScreen::new), "/screens/crafting_terminal.json");
        register(
                PatternTermContainer.TYPE,
                TerminalStyles.PATTERN_TERMINAL
                        .<PatternTermContainer, PatternTermScreen>factory(PatternTermScreen::new), "/screens/pattern_terminal.json");
        register(
                FluidTerminalContainer.TYPE,
                TerminalStyles.FLUID_TERMINAL
                        .<FluidTerminalContainer, FluidTerminalScreen>factory(FluidTerminalScreen::new), "/screens/fluid_terminal.json");
        register(LevelEmitterContainer.TYPE, LevelEmitterScreen::new, "/screens/level_emitter.json");
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
    }

    /**
     * Registers a screen for a given container and ensures the given style is applied after opening the screen.
     */
    private static <M extends AEBaseContainer, U extends AEBaseScreen<M>> void register(ContainerType<M> type, ScreenManager.IScreenFactory<M, U> factory, String stylePath) {
        CONTAINER_STYLES.put(type, stylePath);
        ScreenManager.<M, U>registerFactory(type, (container, playerInv, title) -> {
            U screen = factory.create(container, playerInv, title);
            screen.loadStyle(stylePath);
            return screen;
        });
    }

}
