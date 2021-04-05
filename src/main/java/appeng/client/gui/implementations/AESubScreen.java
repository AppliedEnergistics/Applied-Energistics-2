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

package appeng.client.gui.implementations;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IParts;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.TabButton;
import appeng.container.implementations.ChestContainer;
import appeng.container.implementations.CraftingTermContainer;
import appeng.container.implementations.MEMonitorableContainer;
import appeng.container.implementations.PatternTermContainer;
import appeng.container.implementations.WirelessTermContainer;
import appeng.core.Api;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.SwitchGuisPacket;
import appeng.helpers.IPriorityHost;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.parts.reporting.PatternTerminalPart;
import appeng.parts.reporting.TerminalPart;
import appeng.tile.storage.ChestTileEntity;

/**
 * Utility class for sub-screens of other containers that allow returning to the primary container UI.
 */
final class AESubScreen {

    private final AEBaseScreen<?> gui;
    private final ContainerType<?> previousContainerType;
    private final ItemStack previousContainerIcon;

    /**
     * Based on the container we're opening for, try to determine what it's "primary" GUI would be so that we can go
     * back to it.
     */
    public AESubScreen(AEBaseScreen<?> gui, Object containerTarget) {
        this.gui = gui;

        final IDefinitions definitions = Api.instance().definitions();
        final IParts parts = definitions.parts();

        if (containerTarget instanceof ChestTileEntity) {
            // A chest is also a priority host, but the priority _interface_ can only be
            // opened from the
            // chest ui that doesn't actually show the contents of the inserted cell.
            IPriorityHost priorityHost = (IPriorityHost) containerTarget;
            this.previousContainerIcon = priorityHost.getItemStackRepresentation();
            this.previousContainerType = ChestContainer.TYPE;
        } else if (containerTarget instanceof IPriorityHost) {
            IPriorityHost priorityHost = (IPriorityHost) containerTarget;
            this.previousContainerIcon = priorityHost.getItemStackRepresentation();
            this.previousContainerType = priorityHost.getContainerType();
        } else if (containerTarget instanceof WirelessTerminalGuiObject) {
            this.previousContainerIcon = definitions.items().wirelessTerminal().maybeStack(1).orElse(ItemStack.EMPTY);
            this.previousContainerType = WirelessTermContainer.TYPE;
        } else if (containerTarget instanceof TerminalPart) {
            this.previousContainerIcon = parts.terminal().maybeStack(1).orElse(ItemStack.EMPTY);
            this.previousContainerType = MEMonitorableContainer.TYPE;
        } else if (containerTarget instanceof CraftingTerminalPart) {
            this.previousContainerIcon = parts.craftingTerminal().maybeStack(1).orElse(ItemStack.EMPTY);
            this.previousContainerType = CraftingTermContainer.TYPE;
        } else if (containerTarget instanceof PatternTerminalPart) {
            this.previousContainerIcon = parts.patternTerminal().maybeStack(1).orElse(ItemStack.EMPTY);
            this.previousContainerType = PatternTermContainer.TYPE;
        } else {
            this.previousContainerIcon = null;
            this.previousContainerType = null;
        }
    }

    public TabButton addBackButton(Consumer<TabButton> buttonAdder, int x, int y) {
        return addBackButton(buttonAdder, x, y, null);
    }

    public TabButton addBackButton(Consumer<TabButton> buttonAdder, int x, int y,
            @Nullable ITextComponent label) {
        if (this.previousContainerType != null && !previousContainerIcon.isEmpty()) {
            if (label == null) {
                label = previousContainerIcon.getDisplayName();
            }
            ItemRenderer itemRenderer = gui.getMinecraft().getItemRenderer();
            TabButton button = new TabButton(gui.getGuiLeft() + x, gui.getGuiTop() + y, previousContainerIcon, label,
                    itemRenderer, btn -> goBack());
            buttonAdder.accept(button);
            return button;
        }
        return null;
    }

    public void goBack() {
        NetworkHandler.instance().sendToServer(new SwitchGuisPacket(this.previousContainerType));
    }

}
