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

import appeng.api.util.AEColor;
import appeng.client.gui.AEBaseScreen;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.menu.me.crafting.CraftingStatusEntry;
import appeng.util.ReadableNumberConverter;

public class CraftingStatusTableRenderer extends AbstractTableRenderer<CraftingStatusEntry> {

    private static final int BACKGROUND_ALPHA = 0x5A000000;

    public CraftingStatusTableRenderer(AEBaseScreen<?> screen, int x, int y) {
        super(screen, x, y);
    }

    @Override
    protected List<Component> getEntryDescription(CraftingStatusEntry entry) {
        List<Component> lines = new ArrayList<>(3);
        if (entry.getStoredAmount() > 0) {
            String amount = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getStoredAmount());
            lines.add(GuiText.FromStorage.text(amount));
        }

        if (entry.getActiveAmount() > 0) {
            String amount = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getActiveAmount());
            lines.add(GuiText.Crafting.text(amount));
        }

        if (entry.getPendingAmount() > 0) {
            String amount = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getPendingAmount());
            lines.add(GuiText.Scheduled.text(amount));
        }
        return lines;
    }

    @Override
    protected ItemStack getEntryItem(CraftingStatusEntry entry) {
        return entry.getItem();
    }

    @Override
    protected List<Component> getEntryTooltip(CraftingStatusEntry entry) {
        List<Component> lines = new ArrayList<>(screen.getTooltipFromItem(entry.getItem()));

        // The tooltip compares the unabbreviated amounts
        if (entry.getStoredAmount() > 0) {
            lines.add(GuiText.FromStorage.text(entry.getStoredAmount()));
        }
        if (entry.getActiveAmount() > 0) {
            lines.add(GuiText.Crafting.text(entry.getActiveAmount()));
        }
        if (entry.getPendingAmount() > 0) {
            lines.add(GuiText.Scheduled.text(entry.getPendingAmount()));
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
