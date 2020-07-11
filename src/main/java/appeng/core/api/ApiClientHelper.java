/*
 * This file is part of Applied Energistics 2.
 * 
 * Copyright (c) 2013 - 2020, AlgorithmX2, All rights reserved.
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

package appeng.core.api;

import java.util.List;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import appeng.api.client.ICellModelRegistry;
import appeng.api.client.IClientHelper;
import appeng.api.config.IncludeExclude;
import appeng.api.storage.cells.ICellInventory;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.api.storage.data.IAEStack;
import appeng.core.ApiDefinitions;
import appeng.core.api.client.ApiCellModelRegistry;
import appeng.core.localization.GuiText;

public class ApiClientHelper implements IClientHelper {

    private final ICellModelRegistry cells;

    public ApiClientHelper(ApiDefinitions definitions) {
        this.cells = new ApiCellModelRegistry(definitions);
    }

    @Override
    public <T extends IAEStack<T>> void addCellInformation(ICellInventoryHandler<T> handler,
            List<ITextComponent> lines) {
        if (handler == null) {
            return;
        }

        final ICellInventory<?> cellInventory = handler.getCellInv();

        if (cellInventory != null) {
            lines.add(new StringTextComponent(cellInventory.getUsedBytes() + " ")
                    .appendSibling(GuiText.Of.textComponent()).appendText(" " + cellInventory.getTotalBytes() + " ")
                    .appendSibling(GuiText.BytesUsed.textComponent()));

            lines.add(new StringTextComponent(cellInventory.getStoredItemTypes() + " ")
                    .appendSibling(GuiText.Of.textComponent()).appendText(" " + cellInventory.getTotalItemTypes() + " ")
                    .appendSibling(GuiText.Types.textComponent()));
        }

        if (handler.isPreformatted()) {
            final String list = (handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST ? GuiText.Included
                    : GuiText.Excluded).getLocal();

            if (handler.isFuzzy()) {
                lines.add(GuiText.Partitioned.textComponent().appendText(" - " + list + " ")
                        .appendSibling(GuiText.Fuzzy.textComponent()));
            } else {
                lines.add(GuiText.Partitioned.textComponent().appendText(" - " + list + " ")
                        .appendSibling(GuiText.Precise.textComponent()));
            }
        }

    }

    @Override
    public ICellModelRegistry cells() {
        return this.cells;
    }

}