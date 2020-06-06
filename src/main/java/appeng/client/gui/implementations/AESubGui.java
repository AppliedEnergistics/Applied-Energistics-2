package appeng.client.gui.implementations;

import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IParts;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.implementations.ContainerWirelessTerm;
import appeng.core.Api;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.helpers.IPriorityHost;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartTerminal;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Consumer;

final class AESubGui {

    private final AEBaseGui<?> gui;
    private final ContainerType<?> originalGui;
    private final ItemStack originalGuiIcon;

    /**
     * Based on the container we're opening for, try to determine what it's "primary" GUI would be
     * so that we can go back to it.
     */
    public AESubGui(AEBaseGui<?> gui, Object containerTarget) {
        this.gui = gui;

        final IDefinitions definitions = Api.INSTANCE.definitions();
        final IParts parts = definitions.parts();

        if (containerTarget instanceof IPriorityHost) {
            IPriorityHost priorityHost = (IPriorityHost) containerTarget;
            this.originalGuiIcon = priorityHost.getItemStackRepresentation();
            this.originalGui = priorityHost.getContainerType();
        }

        else if( containerTarget instanceof WirelessTerminalGuiObject)
        {
            this.originalGuiIcon = definitions.items().wirelessTerminal().maybeStack( 1 ).orElse( ItemStack.EMPTY );
            this.originalGui = ContainerWirelessTerm.TYPE;
        }

        else if( containerTarget instanceof PartTerminal)
        {
            this.originalGuiIcon = parts.terminal().maybeStack( 1 ).orElse( ItemStack.EMPTY );
            this.originalGui = ContainerMEMonitorable.TYPE;
        }

        else if( containerTarget instanceof PartCraftingTerminal)
        {
            this.originalGuiIcon = parts.craftingTerminal().maybeStack( 1 ).orElse( ItemStack.EMPTY );
            this.originalGui = ContainerCraftingTerm.TYPE;
        }

        else if( containerTarget instanceof PartPatternTerminal)
        {
            this.originalGuiIcon = parts.patternTerminal().maybeStack( 1 ).orElse( ItemStack.EMPTY );
            this.originalGui = ContainerPatternTerm.TYPE;
        }

        else  {
            this.originalGuiIcon = null;
            this.originalGui = null;
        }
    }

    public final GuiTabButton addBackButton(Consumer<GuiTabButton> buttonAdder, int x, int y) {
        return addBackButton(buttonAdder, x, y, null);
    }

    public final GuiTabButton addBackButton(Consumer<GuiTabButton> buttonAdder, int x, int y, @Nullable String label) {
        if( this.originalGui != null && !originalGuiIcon.isEmpty() )
        {
            if (label == null) {
                label = originalGuiIcon.getDisplayName().getString();
            }
            ItemRenderer itemRenderer = gui.getMinecraft().getItemRenderer();
            GuiTabButton button = new GuiTabButton( gui.getGuiLeft() + x, gui.getGuiTop() + y, originalGuiIcon, label, itemRenderer, btn -> goBack() );
            buttonAdder.accept(button);
            return button;
        }
        return null;
    }

    public final void goBack() {
        NetworkHandler.instance().sendToServer( new PacketSwitchGuis( this.originalGui ) );
    }

}
