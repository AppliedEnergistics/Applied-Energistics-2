/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.client.gui.me.networktool;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ToggleButton;
import appeng.container.me.networktool.NetworkToolContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

public class NetworkToolScreen extends AEBaseScreen<NetworkToolContainer> {

    private final ToggleButton transparentFacadesButton;

    public NetworkToolScreen(NetworkToolContainer container, PlayerInventory playerInventory, ITextComponent title,
            ScreenStyle style) {
        super(container, playerInventory, title, style);

        this.transparentFacadesButton = new ToggleButton(Icon.TRANSPARENT_FACADES_OFF, Icon.TRANSPARENT_FACADES_ON,
                GuiText.TransparentFacades.getLocal(), GuiText.TransparentFacadesHint.getLocal(),
                btn -> toggleFacades());

        addToLeftToolbar(this.transparentFacadesButton);
    }

    private void toggleFacades() {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("NetworkTool", "Toggle"));
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.transparentFacadesButton.setState(container.isFacadeMode());
    }

}
