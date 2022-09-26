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

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.me.common.ClientDisplaySlot;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.client.gui.widgets.TabButton;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import appeng.menu.me.items.PatternEncodingTermMenu;

/**
 * Allows precisely setting the amount to use for a processing pattern slot.
 * <p/>
 * Note that this is a sub-screen of {@link PatternEncodingTermScreen}
 */
public class SetProcessingPatternAmountScreen<C extends PatternEncodingTermMenu>
        extends AESubScreen<C, PatternEncodingTermScreen<C>> {

    private final NumberEntryWidget amount;

    private final GenericStack currentStack;

    private final Consumer<GenericStack> setter;

    public SetProcessingPatternAmountScreen(PatternEncodingTermScreen<C> parentScreen,
            GenericStack currentStack,
            Consumer<GenericStack> setter) {
        super(parentScreen, "/screens/set_processing_pattern_amount.json");

        this.currentStack = currentStack;
        this.setter = setter;

        widgets.addButton("save", GuiText.Set.text(), this::confirm);

        var icon = getMenu().getHost().getMainMenuIcon();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        var button = new TabButton(icon, icon.getHoverName(), itemRenderer, btn -> {
            returnToParent();
        });
        widgets.add("back", button);

        this.amount = widgets.addNumberEntryWidget("amountToStock", NumberEntryType.of(currentStack.what()));
        this.amount.setLongValue(currentStack.amount());
        this.amount.setMaxValue(getMaxAmount());
        this.amount.setTextFieldStyle(style.getWidget("amountToStockInput"));
        this.amount.setMinValue(0);
        this.amount.setHideValidationIcon(true);
        this.amount.setOnConfirm(this::confirm);

        addClientSideSlot(new ClientDisplaySlot(currentStack), SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    protected void init() {
        super.init();

        // The screen JSON includes the toolbox, but we don't actually have a need for it here
        setSlotsHidden(SlotSemantics.TOOLBOX, true);
    }

    private void confirm() {
        this.amount.getLongValue().ifPresent(newAmount -> {
            newAmount = Mth.clamp(newAmount, 0, getMaxAmount());

            if (newAmount <= 0) {
                setter.accept(null);
            } else {
                setter.accept(new GenericStack(currentStack.what(), newAmount));
            }
            returnToParent();
        });
    }

    private long getMaxAmount() {
        return 999999 * (long) currentStack.what().getAmountPerUnit();
    }
}
