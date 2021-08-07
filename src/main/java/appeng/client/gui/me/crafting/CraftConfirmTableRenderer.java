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

package appeng.client.gui.me.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.client.gui.AEBaseScreen;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;
import appeng.core.localization.GuiText;
import appeng.util.ReadableNumberConverter;

public class CraftConfirmTableRenderer extends AbstractTableRenderer<CraftingPlanSummaryEntry> {

    public CraftConfirmTableRenderer(AEBaseScreen<?> screen, int x, int y) {
        super(screen, x, y);
    }

    @Override
    protected List<Component> getEntryDescription(CraftingPlanSummaryEntry entry) {
        List<Component> lines = new ArrayList<>(3);
        if (entry.getStoredAmount() > 0) {
            String amount = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getStoredAmount());
            lines.add(GuiText.FromStorage.text(amount));
        }

        if (entry.getMissingAmount() > 0) {
            String amount = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getMissingAmount());
            lines.add(GuiText.Missing.text(amount));
        }

        if (entry.getCraftAmount() > 0) {
            String amount = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getCraftAmount());
            lines.add(GuiText.ToCraft.text(amount));
        }
        return lines;
    }

    @Override
    protected ItemStack getEntryItem(CraftingPlanSummaryEntry entry) {
        return entry.getItem();
    }

    @Override
    protected List<Component> getEntryTooltip(CraftingPlanSummaryEntry entry) {
        List<Component> lines = new ArrayList<>(screen.getTooltipFromItem(entry.getItem()));

        // The tooltip compares the unabbreviated amounts
        if (entry.getStoredAmount() > 0) {
            lines.add(GuiText.FromStorage.text(entry.getStoredAmount()));
        }
        if (entry.getMissingAmount() > 0) {
            lines.add(GuiText.Missing.text(entry.getMissingAmount()));
        }
        if (entry.getCraftAmount() > 0) {
            lines.add(GuiText.ToCraft.text(entry.getCraftAmount()));
        }

        return lines;

    }

    @Override
    protected int getEntryOverlayColor(CraftingPlanSummaryEntry entry) {
        return entry.getMissingAmount() > 0 ? 0x1AFF0000 : 0;
    }

}
