package appeng.client.gui.implementations;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IParts;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.*;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.helpers.IPriorityHost;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartTerminal;
import appeng.tile.storage.TileChest;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Utility class for sub-screens of other containers that allow returning to the primary container UI.
 */
final class AESubGui {

    private final AEBaseGui<?> gui;
    private final ContainerType<?> previousContainerType;
    private final ItemStack previousContainerIcon;

    /**
     * Based on the container we're opening for, try to determine what it's "primary" GUI would be
     * so that we can go back to it.
     */
    public AESubGui(AEBaseGui<?> gui, Object containerTarget) {
        this.gui = gui;

        final IDefinitions definitions = AEApi.instance().definitions();
        final IParts parts = definitions.parts();

        if (containerTarget instanceof TileChest) {
            // A chest is also a priority host, but the priority _interface_ can only be opened from the
            // chest ui that doesn't actually show the contents of the inserted cell.
            IPriorityHost priorityHost = (IPriorityHost) containerTarget;
            this.previousContainerIcon = priorityHost.getItemStackRepresentation();
            this.previousContainerType = ContainerChest.TYPE;
        }

        else if (containerTarget instanceof IPriorityHost) {
            IPriorityHost priorityHost = (IPriorityHost) containerTarget;
            this.previousContainerIcon = priorityHost.getItemStackRepresentation();
            this.previousContainerType = priorityHost.getContainerType();
        }

        else if( containerTarget instanceof WirelessTerminalGuiObject)
        {
            this.previousContainerIcon = definitions.items().wirelessTerminal().maybeStack( 1 ).orElse( ItemStack.EMPTY );
            this.previousContainerType = ContainerWirelessTerm.TYPE;
        }

        else if( containerTarget instanceof PartTerminal)
        {
            this.previousContainerIcon = parts.terminal().maybeStack( 1 ).orElse( ItemStack.EMPTY );
            this.previousContainerType = ContainerMEMonitorable.TYPE;
        }

        else if( containerTarget instanceof PartCraftingTerminal)
        {
            this.previousContainerIcon = parts.craftingTerminal().maybeStack( 1 ).orElse( ItemStack.EMPTY );
            this.previousContainerType = ContainerCraftingTerm.TYPE;
        }

        else if( containerTarget instanceof PartPatternTerminal)
        {
            this.previousContainerIcon = parts.patternTerminal().maybeStack( 1 ).orElse( ItemStack.EMPTY );
            this.previousContainerType = ContainerPatternTerm.TYPE;
        }

        else  {
            this.previousContainerIcon = null;
            this.previousContainerType = null;
        }
    }

    public final GuiTabButton addBackButton(Consumer<GuiTabButton> buttonAdder, int x, int y) {
        return addBackButton(buttonAdder, x, y, null);
    }

    public final GuiTabButton addBackButton(Consumer<GuiTabButton> buttonAdder, int x, int y, @Nullable String label) {
        if( this.previousContainerType != null && !previousContainerIcon.isEmpty() )
        {
            if (label == null) {
                label = previousContainerIcon.getDisplayName().getString();
            }
            ItemRenderer itemRenderer = gui.getMinecraft().getItemRenderer();
            GuiTabButton button = new GuiTabButton( gui.getGuiLeft() + x, gui.getGuiTop() + y, previousContainerIcon, label, itemRenderer, btn -> goBack() );
            buttonAdder.accept(button);
            return button;
        }
        return null;
    }

    public final void goBack() {
        NetworkHandler.instance().sendToServer(new PacketSwitchGuis(this.previousContainerType));
    }

}
