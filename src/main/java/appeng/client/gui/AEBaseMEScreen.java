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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.SlotME;
import appeng.container.AEBaseContainer;
import appeng.core.AEConfig;
import appeng.core.localization.ButtonToolTips;

public abstract class AEBaseMEScreen<T extends AEBaseContainer> extends AEBaseScreen<T> {

    public AEBaseMEScreen(T container, PlayerInventory playerInventory, Text title) {
        super(container, playerInventory, title);
    }

    @Override
    protected void renderTooltip(MatrixStack matrices, final ItemStack stack, final int x, final int y) {
        final Slot s = this.getSlot(x, y);

        if (s instanceof SlotME && !stack.isEmpty()) {
            final int bigNumber = AEConfig.instance().isUseLargeFonts() ? 999 : 9999;

            IAEItemStack myStack = null;
            final List<Text> currentToolTip = this.getTooltipFromItem(stack);

            try {
                final SlotME theSlotField = (SlotME) s;
                myStack = theSlotField.getAEStack();
            } catch (final Throwable ignore) {
            }

            if (myStack != null) {
                if (myStack.getStackSize() > bigNumber || (myStack.getStackSize() > 1 && stack.isDamaged())) {
                    final String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                            .format(myStack.getStackSize());
                    currentToolTip.add(ButtonToolTips.ItemsStored.text(formattedAmount).formatted(Formatting.GRAY));
                }

                if (myStack.getCountRequestable() > 0) {
                    final String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                            .format(myStack.getCountRequestable());
                    currentToolTip.add(ButtonToolTips.ItemsRequestable.text(formattedAmount));
                }

                this.method_30901(matrices, currentToolTip, x, y);

                return;
            } else if (stack.getCount() > bigNumber) {
                final String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(stack.getCount());
                currentToolTip.add(ButtonToolTips.ItemsStored.text(formattedAmount).formatted(Formatting.GRAY));

                this.method_30901(matrices, currentToolTip, x, y);

                return;
            }
        }

        super.renderTooltip(matrices, stack, x, y);
    }
}