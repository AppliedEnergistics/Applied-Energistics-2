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

package appeng.client.gui;


import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.SlotME;
import appeng.core.AEConfig;
import appeng.core.localization.ButtonToolTips;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;


public abstract class AEBaseMEGui extends AEBaseGui {

    public AEBaseMEGui(final Container container) {
        super(container);
    }

    @Override
    protected void renderToolTip(final ItemStack stack, final int x, final int y) {
        final Slot s = this.getSlot(x, y);

        if (s instanceof SlotME && !stack.isEmpty()) {
            final int bigNumber = AEConfig.instance().useTerminalUseLargeFont() ? 999 : 9999;

            IAEItemStack myStack = null;
            final List<String> currentToolTip = this.getItemToolTip(stack);

            try {
                final SlotME theSlotField = (SlotME) s;
                myStack = theSlotField.getAEStack();
            } catch (final Throwable ignore) {
            }

            if (myStack != null) {
                if (myStack.getStackSize() > bigNumber || (myStack.getStackSize() > 1 && stack.isItemDamaged())) {
                    final String local = ButtonToolTips.ItemsStored.getLocal();
                    final String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(myStack.getStackSize());
                    final String format = String.format(local, formattedAmount);

                    currentToolTip.add(TextFormatting.GRAY + format);
                }

                if (myStack.getCountRequestable() > 0) {
                    final String local = ButtonToolTips.ItemsRequestable.getLocal();
                    final String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(myStack.getCountRequestable());
                    final String format = String.format(local, formattedAmount);

                    currentToolTip.add(format);
                }

                this.drawHoveringText(currentToolTip, x, y, this.fontRenderer);

                return;
            } else if (stack.getCount() > bigNumber) {
                final String local = ButtonToolTips.ItemsStored.getLocal();
                final String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(stack.getCount());
                final String format = String.format(local, formattedAmount);

                currentToolTip.add(TextFormatting.GRAY + format);

                this.drawHoveringText(currentToolTip, x, y, this.fontRenderer);

                return;
            }
        }

        super.renderToolTip(stack, x, y);
    }
}