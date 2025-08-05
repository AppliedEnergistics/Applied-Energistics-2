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

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;
import appeng.client.gui.AEBaseScreen;
import appeng.core.localization.GuiText;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;
import appeng.util.NumberUtil;

public class CraftConfirmTableRenderer extends AbstractTableRenderer<CraftingPlanSummaryEntry> {

    public CraftConfirmTableRenderer(AEBaseScreen<?> screen, int x, int y) {
        super(screen, x, y, 5);
    }

    @Override
    protected List<Component> getEntryDescription(CraftingPlanSummaryEntry entry) {
        List<Component> lines = new ArrayList<>(3);
        if (entry.storedAmount() > 0) {
            String amount = entry.what().formatAmount(entry.storedAmount(), AmountFormat.SLOT);
            lines.add(GuiText.FromStorage.text(amount));
        }

        if (entry.missingAmount() > 0) {
            String amount = entry.what().formatAmount(entry.missingAmount(), AmountFormat.SLOT);
            lines.add(GuiText.Missing.text(amount));
        }

        if (entry.craftAmount() > 0) {
            String amount = entry.what().formatAmount(entry.craftAmount(), AmountFormat.SLOT);
            lines.add(GuiText.ToCraft.text(amount));
        }
        // Same check as above because we want the percentage to be the last element
        if (entry.storedAmount() > 0) {
            var percentage = NumberUtil.createPercentageComponent(
                    entry.missingAmount() == 0 ? entry.storedAmount() : entry.missingAmount(),
                    entry.availableAmount());
            lines.add(GuiText.UsedAmount.text().withStyle(percentage.getStyle()).append(percentage));
        }
        return lines;
    }

    @Override
    protected AEKey getEntryStack(CraftingPlanSummaryEntry entry) {
        return entry.what();
    }

    @Override
    protected List<Component> getEntryTooltip(CraftingPlanSummaryEntry entry) {
        List<Component> lines = AEKeyRendering.getTooltip(entry.what());
        // The tooltip compares the unabbreviated amounts
        if (entry.storedAmount() > 0) {
            lines.add(GuiText.FromStorage
                    .text(entry.what().formatAmount(entry.storedAmount(), AmountFormat.FULL)));
        }
        if (entry.missingAmount() > 0) {
            lines.add(GuiText.Missing.text(
                    entry.what().formatAmount(entry.missingAmount(), AmountFormat.FULL)));
        }

        if (entry.craftAmount() > 0) {
            lines.add(GuiText.ToCraft
                    .text(entry.what().formatAmount(entry.craftAmount(), AmountFormat.FULL)));
        }
        // Same check as above because we want the percentage to be the last element
        if (entry.storedAmount() > 0) {
            var percentage = NumberUtil.createPercentageComponent(
                    entry.missingAmount() == 0 ? entry.storedAmount() : entry.missingAmount(),
                    entry.availableAmount());
            lines.add(GuiText.UsedAmount.text().withStyle(percentage.getStyle()).append(percentage));
        }

        return lines;
    }

    @Override
    protected int getEntryOverlayColor(CraftingPlanSummaryEntry entry) {
        return entry.missingAmount() > 0 ? 0x1AFF0000 : 0;
    }

}
