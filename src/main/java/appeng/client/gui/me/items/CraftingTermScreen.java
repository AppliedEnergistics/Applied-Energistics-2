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

package appeng.client.gui.me.items;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.api.config.ActionItems;
import appeng.client.gui.me.common.TerminalStyle;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.container.me.items.CraftingTermContainer;

/**
 * This screen extends the item terminal with a crafting grid. The content of the crafting grid is stored server-side in
 * the crafting terminal itself.
 */
public class CraftingTermScreen extends ItemTerminalScreen<CraftingTermContainer> {

    public CraftingTermScreen(TerminalStyle style, CraftingTermContainer container, PlayerInventory playerInventory,
            ITextComponent title, ScreenStyle style1) {
        super(style, container, playerInventory, title, style1);
    }

    @Override
    public void init() {
        super.init();
        ActionButton clearBtn = this.addButton(
                new ActionButton(this.guiLeft + 92, this.guiTop + this.ySize - 156, ActionItems.STASH,
                        btn -> container.clearCraftingGrid()));
        clearBtn.setHalfSize(true);
    }

}
