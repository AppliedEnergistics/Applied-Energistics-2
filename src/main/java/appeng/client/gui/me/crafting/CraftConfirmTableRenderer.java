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

import appeng.api.client.AEStackRendering;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;
import appeng.client.gui.AEBaseScreen;
import appeng.core.localization.GuiText;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;

public class CraftConfirmTableRenderer extends AbstractTableRenderer<CraftingPlanSummaryEntry> {

    public CraftConfirmTableRenderer(AEBaseScreen<?> screen, int x, int y) {
        super(screen, x, y);
    }

    @Override
    protected List<Component> getEntryDescription(CraftingPlanSummaryEntry entry) {
        List<Component> lines = new ArrayList<>(3);
        if (entry.getStoredAmount() > 0) {
            String amount = entry.getWhat().formatAmount(entry.getStoredAmount(), AmountFormat.PREVIEW_REGULAR);
            lines.add(GuiText.FromStorage.text(amount));
        }

        if (entry.getMissingAmount() > 0) {
            String amount = entry.getWhat().formatAmount(entry.getMissingAmount(), AmountFormat.PREVIEW_REGULAR);
            lines.add(GuiText.Missing.text(amount));
        }

        if (entry.getCraftAmount() > 0) {
            String amount = entry.getWhat().formatAmount(entry.getCraftAmount(), AmountFormat.PREVIEW_REGULAR);
            lines.add(GuiText.ToCraft.text(amount));
        }
        return lines;
    }

    @Override
    protected AEKey getEntryStack(CraftingPlanSummaryEntry entry) {
        return entry.getWhat();
    }

    @Override
    protected List<Component> getEntryTooltip(CraftingPlanSummaryEntry entry) {
        List<Component> lines = AEStackRendering.getTooltip(entry.getWhat());

        // The tooltip compares the unabbreviated amounts
        if (entry.getStoredAmount() > 0) {
            lines.add(GuiText.FromStorage
                    .text(entry.getWhat().formatAmount(entry.getStoredAmount(), AmountFormat.FULL)));
        }
        if (entry.getMissingAmount() > 0) {
            lines.add(GuiText.Missing.text(
                    entry.getWhat().formatAmount(entry.getMissingAmount(), AmountFormat.FULL)));
        }
        if (entry.getCraftAmount() > 0) {
            lines.add(GuiText.ToCraft
                    .text(entry.getWhat().formatAmount(entry.getCraftAmount(), AmountFormat.FULL)));
        }

        return lines;

    }

    @Override
    protected int getEntryOverlayColor(CraftingPlanSummaryEntry entry) {
        return entry.getMissingAmount() > 0 ? 0x1AFF0000 : 0;
    }

}
