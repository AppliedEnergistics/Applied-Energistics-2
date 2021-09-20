package appeng.client.gui.implementations;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.GenericSlotWidget;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigButtonPacket;
import appeng.menu.SlotSemantic;
import appeng.menu.implementations.CraftingInterfaceMenu;

public class CraftingInterfaceScreen extends AEBaseScreen<CraftingInterfaceMenu> {

    private final SettingToggleButton<YesNo> blockingModeButton;
    private final ToggleButton showInInterfaceTerminalButton;

    public CraftingInterfaceScreen(CraftingInterfaceMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);

        widgets.addOpenPriorityButton();

        this.blockingModeButton = new ServerSettingToggleButton<>(Settings.BLOCKING_MODE, YesNo.NO);
        this.addToLeftToolbar(this.blockingModeButton);

        this.showInInterfaceTerminalButton = new ToggleButton(Icon.INTERFACE_TERMINAL_SHOW,
                Icon.INTERFACE_TERMINAL_HIDE,
                GuiText.InterfaceTerminal.text(), GuiText.InterfaceTerminalHint.text(),
                btn -> selectNextInterfaceMode());
        this.addToLeftToolbar(this.showInInterfaceTerminalButton);

        var inv = menu.getReturnInv();
        for (int i = 0; i < inv.size(); ++i) {
            addSlot(new GenericSlotWidget(this, inv, i), SlotSemantic.READONLY_STACKS);
        }
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.blockingModeButton.set(this.menu.getBlockingMode());
        this.showInInterfaceTerminalButton.setState(this.menu.getShowInInterfaceTerminal() == YesNo.YES);
    }

    private void selectNextInterfaceMode() {
        final boolean backwards = isHandlingRightClick();
        NetworkHandler.instance().sendToServer(new ConfigButtonPacket(Settings.INTERFACE_TERMINAL, backwards));
    }
}
