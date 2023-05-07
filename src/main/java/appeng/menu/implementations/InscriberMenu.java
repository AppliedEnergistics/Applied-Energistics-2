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

package appeng.menu.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.util.IConfigManager;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.blockentity.misc.InscriberRecipes;
import appeng.client.gui.Icon;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.core.localization.Side;
import appeng.core.localization.Tooltips;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.OutputSlot;

/**
 * @see appeng.client.gui.implementations.InscriberScreen
 */
public class InscriberMenu extends UpgradeableMenu<InscriberBlockEntity> implements IProgressProvider {

    public static final MenuType<InscriberMenu> TYPE = MenuTypeBuilder
            .create(InscriberMenu::new, InscriberBlockEntity.class)
            .build("inscriber");

    private final Slot top;
    private final Slot middle;
    private final Slot bottom;

    @GuiSync(2)
    public int maxProcessingTime = -1;

    @GuiSync(3)
    public int processingTime = -1;

    @GuiSync(7)
    public YesNo separateSides = YesNo.NO;
    @GuiSync(8)
    public YesNo autoExport = YesNo.NO;

    public InscriberMenu(int id, Inventory ip, InscriberBlockEntity host) {
        super(TYPE, id, ip, host);

        var inv = host.getInternalInventory();

        var top = new AppEngSlot(inv, 0);
        top.setIcon(Icon.BACKGROUND_PLATE);
        top.setEmptyTooltip(
                () -> separateSides == YesNo.YES ? Tooltips.inputSlot(Side.TOP) : Tooltips.inputSlot(Side.ANY));
        this.top = this.addSlot(top, SlotSemantics.INSCRIBER_PLATE_TOP);

        var bottom = new AppEngSlot(inv, 1);
        bottom.setIcon(Icon.BACKGROUND_PLATE);
        bottom.setEmptyTooltip(
                () -> separateSides == YesNo.YES ? Tooltips.inputSlot(Side.BOTTOM) : Tooltips.inputSlot(Side.ANY));
        this.bottom = this.addSlot(bottom, SlotSemantics.INSCRIBER_PLATE_BOTTOM);

        var middle = new AppEngSlot(inv, 2);
        middle.setIcon(Icon.BACKGROUND_INGOT);
        middle.setEmptyTooltip(
                () -> separateSides == YesNo.YES ? Tooltips.inputSlot(Side.LEFT, Side.RIGHT, Side.BACK, Side.FRONT)
                        : Tooltips.inputSlot(Side.ANY));
        this.middle = this.addSlot(middle, SlotSemantics.MACHINE_INPUT);

        var output = new OutputSlot(inv, 3, null);
        output.setEmptyTooltip(
                () -> separateSides == YesNo.YES ? Tooltips.outputSlot(Side.LEFT, Side.RIGHT, Side.BACK, Side.FRONT)
                        : Tooltips.outputSlot(Side.ANY));
        this.addSlot(output, SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.separateSides = getHost().getConfigManager().getSetting(Settings.INSCRIBER_SEPARATE_SIDES);
        this.autoExport = getHost().getConfigManager().getSetting(Settings.AUTO_EXPORT);
    }

    @Override
    protected void standardDetectAndSendChanges() {
        if (isServerSide()) {
            this.maxProcessingTime = getHost().getMaxProcessingTime();
            this.processingTime = getHost().getProcessingTime();
        }
        super.standardDetectAndSendChanges();
    }

    @Override
    public boolean isValidForSlot(Slot s, ItemStack is) {
        final ItemStack top = getHost().getInternalInventory().getStackInSlot(0);
        final ItemStack bot = getHost().getInternalInventory().getStackInSlot(1);

        if (s == this.middle) {
            ItemDefinition<?> press = AEItems.NAME_PRESS;
            if (press.isSameAs(top) || press.isSameAs(bot)) {
                return !press.isSameAs(is);
            }

            return InscriberRecipes.findRecipe(getHost().getLevel(), is, top, bot, false) != null;
        } else if (s == this.top && !bot.isEmpty() || s == this.bottom && !top.isEmpty()) {
            ItemStack otherSlot;
            if (s == this.top) {
                otherSlot = this.bottom.getItem();
            } else {
                otherSlot = this.top.getItem();
            }

            // name presses
            ItemDefinition<?> namePress = AEItems.NAME_PRESS;
            if (namePress.isSameAs(otherSlot)) {
                return namePress.isSameAs(is);
            }

            // everything else
            // test for a partial recipe match (ignoring the middle slot)
            return InscriberRecipes.isValidOptionalIngredientCombination(getHost().getLevel(), is, otherSlot);
        }
        return true;
    }

    @Override
    public int getCurrentProgress() {
        return this.processingTime;
    }

    @Override
    public int getMaxProgress() {
        return this.maxProcessingTime;
    }

    public YesNo getSeparateSides() {
        return separateSides;
    }

    public YesNo getAutoExport() {
        return autoExport;
    }
}
