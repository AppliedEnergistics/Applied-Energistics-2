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
import appeng.api.util.AEColor;
import appeng.client.gui.AEBaseScreen;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.menu.me.crafting.CraftingStatusEntry;

public class CraftingStatusTableRenderer extends AbstractTableRenderer<CraftingStatusEntry> {

    private static final int BACKGROUND_ALPHA = 0x5A000000;

    public CraftingStatusTableRenderer(AEBaseScreen<?> screen, int x, int y) {
        super(screen, x, y);
    }

    @Override
    protected List<Component> getEntryDescription(CraftingStatusEntry entry) {
        List<Component> lines = new ArrayList<>(3);
        if (entry.getStoredAmount() > 0) {
            String amount = entry.getWhat().formatAmount(entry.getStoredAmount(), AmountFormat.SLOT);
            lines.add(GuiText.FromStorage.text(amount));
        }

        if (entry.getActiveAmount() > 0) {
            String amount = entry.getWhat().formatAmount(entry.getActiveAmount(), AmountFormat.SLOT);
            lines.add(GuiText.Crafting.text(amount));
        }

        if (entry.getPendingAmount() > 0) {
            String amount = entry.getWhat().formatAmount(entry.getPendingAmount(), AmountFormat.SLOT);
            lines.add(GuiText.Scheduled.text(amount));
        }
        return lines;
    }

    @Override
    protected AEKey getEntryStack(CraftingStatusEntry entry) {
        return entry.getWhat();
    }

    @Override
    protected List<Component> getEntryTooltip(CraftingStatusEntry entry) {
        List<Component> lines = AEKeyRendering.getTooltip(entry.getWhat());

        // The tooltip compares the unabbreviated amounts
        if (entry.getStoredAmount() > 0) {
            lines.add(GuiText.FromStorage
                    .text(entry.getWhat().formatAmount(entry.getStoredAmount(), AmountFormat.FULL)));
        }
        if (entry.getActiveAmount() > 0) {
            lines.add(GuiText.Crafting
                    .text(entry.getWhat().formatAmount(entry.getActiveAmount(), AmountFormat.FULL)));
        }
        if (entry.getPendingAmount() > 0) {
            lines.add(GuiText.Scheduled.text(
                    entry.getWhat().formatAmount(entry.getPendingAmount(), AmountFormat.FULL)));
        }

        return lines;

    }

    @Override
    protected int getEntryBackgroundColor(CraftingStatusEntry entry) {
        if (AEConfig.instance().isUseColoredCraftingStatus()) {
            if (entry.getActiveAmount() > 0) {
                return AEColor.GREEN.blackVariant | BACKGROUND_ALPHA;
            } else if (entry.getPendingAmount() > 0) {
                return AEColor.YELLOW.blackVariant | BACKGROUND_ALPHA;
            }
        }
        return 0;
    }

}
