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

package appeng.container.implementations;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.container.SlotSemantic;
import appeng.container.guisync.GuiSync;
import appeng.container.interfaces.IProgressProvider;
import appeng.container.slot.OutputSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.tile.misc.InscriberRecipes;
import appeng.tile.misc.InscriberTileEntity;

/**
 * @see appeng.client.gui.implementations.InscriberScreen
 */
public class InscriberContainer extends UpgradeableContainer implements IProgressProvider {

    public static final ContainerType<InscriberContainer> TYPE = ContainerTypeBuilder
            .create(InscriberContainer::new, InscriberTileEntity.class)
            .build("inscriber");

    private final InscriberTileEntity ti;

    private final Slot top;
    private final Slot middle;
    private final Slot bottom;

    @GuiSync(2)
    public int maxProcessingTime = -1;

    @GuiSync(3)
    public int processingTime = -1;

    public InscriberContainer(int id, final PlayerInventory ip, final InscriberTileEntity te) {
        super(TYPE, id, ip, te);
        this.ti = te;

        IItemHandler inv = te.getInternalInventory();

        RestrictedInputSlot top = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.INSCRIBER_PLATE, inv, 0);
        top.setStackLimit(1);
        this.top = this.addSlot(top, SlotSemantic.INSCRIBER_PLATE_TOP);
        RestrictedInputSlot bottom = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.INSCRIBER_PLATE, inv,
                1);
        bottom.setStackLimit(1);
        this.bottom = this.addSlot(bottom, SlotSemantic.INSCRIBER_PLATE_BOTTOM);
        RestrictedInputSlot middle = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.INSCRIBER_INPUT, inv,
                2);
        middle.setStackLimit(1);
        this.middle = this.addSlot(middle, SlotSemantic.MACHINE_INPUT);

        this.addSlot(new OutputSlot(inv, 3, null), SlotSemantic.MACHINE_OUTPUT);
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();
    }

    @Override
    protected boolean supportCapacity() {
        return false;
    }

    @Override
    public int availableUpgrades() {
        return 3;
    }

    @Override
    public void detectAndSendChanges() {
        this.standardDetectAndSendChanges();

        if (isServer()) {
            this.maxProcessingTime = this.ti.getMaxProcessingTime();
            this.processingTime = this.ti.getProcessingTime();
        }
    }

    @Override
    public boolean isValidForSlot(final Slot s, final ItemStack is) {
        final ItemStack top = this.ti.getInternalInventory().getStackInSlot(0);
        final ItemStack bot = this.ti.getInternalInventory().getStackInSlot(1);

        if (s == this.middle) {
            ItemDefinition<?> press = AEItems.NAME_PRESS;
            if (press.isSameAs(top) || press.isSameAs(bot)) {
                return !press.isSameAs(is);
            }

            return InscriberRecipes.findRecipe(ti.getWorld(), is, top, bot, false) != null;
        } else if (s == this.top && !bot.isEmpty() || s == this.bottom && !top.isEmpty()) {
            ItemStack otherSlot;
            if (s == this.top) {
                otherSlot = this.bottom.getStack();
            } else {
                otherSlot = this.top.getStack();
            }

            // name presses
            ItemDefinition<?> namePress = AEItems.NAME_PRESS;
            if (namePress.isSameAs(otherSlot)) {
                return namePress.isSameAs(is);
            }

            // everything else
            // test for a partial recipe match (ignoring the middle slot)
            return InscriberRecipes.isValidOptionalIngredientCombination(ti.getWorld(), is, otherSlot);
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
}
