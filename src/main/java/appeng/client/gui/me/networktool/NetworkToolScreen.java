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

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Blitter;
import appeng.client.gui.widgets.ToggleButton;
import appeng.container.me.networktool.NetworkToolContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class NetworkToolScreen extends AEBaseScreen<NetworkToolContainer> {

    private static final Blitter BACKGROUND = Blitter.texture("guis/toolbox.png").src(0, 0, 176, 166);

    private ToggleButton tFacades;

    public NetworkToolScreen(NetworkToolContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title, BACKGROUND);
    }

    @Override
    public void init() {
        super.init();

        this.tFacades = new ToggleButton(0, 0, 23, 22,
                GuiText.TransparentFacades.getLocal(), GuiText.TransparentFacadesHint.getLocal(),
                btn -> toggleFacades());

        addToLeftToolbar(this.tFacades);
    }

    private void toggleFacades() {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("NetworkTool", "Toggle"));
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.tFacades.setState(container.isFacadeMode());
    }

}
