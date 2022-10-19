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

package appeng.container.slot;


import appeng.container.AEBaseContainer;
import appeng.util.helpers.ItemHandlerUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;


public class AppEngSlot extends Slot {
    private static final IInventory emptyInventory = new InventoryBasic("[Null]", true, 0);
    private final IItemHandler itemHandler;
    private final int index;

    private final int defX;
    private final int defY;
    private boolean isDraggable = true;
    private boolean isPlayerSide = false;
    private AEBaseContainer myContainer = null;
    private int IIcon = -1;
    private hasCalculatedValidness isValid;
    private boolean isDisplay = false;
    private boolean returnAsSingleStack;

    public AppEngSlot(final IItemHandler inv, final int idx, final int x, final int y) {
        super(emptyInventory, idx, x, y);
        this.itemHandler = inv;
        this.index = idx;

        this.defX = x;
        this.defY = y;
        this.setIsValid(hasCalculatedValidness.NotAvailable);
    }

    public Slot setNotDraggable() {
        this.setDraggable(false);
        return this;
    }

    public Slot setPlayerSide() {
        this.isPlayerSide = true;
        return this;
    }

    public String getTooltip() {
        return null;
    }

    public void clearStack() {
        ItemHandlerUtil.setStackInSlot(this.itemHandler, this.index, ItemStack.EMPTY);
    }

    @Override
    public boolean isItemValid(@Nonnull final ItemStack par1ItemStack) {
        if (this.isSlotEnabled()) {
            return this.itemHandler.isItemValid(this.index, par1ItemStack);
        }
        return false;
    }

    @Override
    @Nonnull
    public ItemStack getStack() {
        if (!this.isSlotEnabled()) {
            return ItemStack.EMPTY;
        }

        if (this.itemHandler.getSlots() <= this.getSlotIndex()) {
            return ItemStack.EMPTY;
        }


        if (this.isDisplay()) {
            this.setDisplay(false);
            if (this.returnAsSingleStack()) {
                setReturnAsSingleStack(false);
                ItemStack ret = this.getDisplayStack().copy();
                ret.setCount(1);
                return ret;
            }
            return this.getDisplayStack();
        }

        return this.itemHandler.getStackInSlot(this.index);
    }

    private boolean returnAsSingleStack() {
        return this.returnAsSingleStack;
    }

    public void setReturnAsSingleStack(boolean returnAsSingleStack) {
        this.returnAsSingleStack = returnAsSingleStack;
    }

    @Override
    public void putStack(final ItemStack stack) {
        if (this.isSlotEnabled()) {
            ItemHandlerUtil.setStackInSlot(this.itemHandler, this.index, stack);

            if (this.getContainer() != null) {
                this.getContainer().onSlotChange(this);
            }
        }
    }

    public IItemHandler getItemHandler() {
        return this.itemHandler;
    }

    @Override
    public void onSlotChanged() {
        this.setIsValid(hasCalculatedValidness.NotAvailable);
        if (this.isSlotEnabled()) {
            ItemHandlerUtil.setStackInSlot(this.itemHandler, this.index, this.getStack().copy());

            if (this.getContainer() != null) {
                this.getContainer().onSlotChange(this);
            }
        }
        super.onSlotChanged();
    }

    @Override
    public int getSlotStackLimit() {
        return this.itemHandler.getSlotLimit(this.index);
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        return Math.min(this.getSlotStackLimit(), stack.getMaxStackSize());
    }

    @Override
    public boolean canTakeStack(final EntityPlayer par1EntityPlayer) {
        if (this.isSlotEnabled()) {
            return !this.itemHandler.extractItem(this.index, Integer.MAX_VALUE, true).isEmpty();
        }
        return false;
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int amount) {
        return this.itemHandler.extractItem(this.index, amount, false);
    }

    @Override
    public boolean isSameInventory(Slot other) {
        return other instanceof AppEngSlot && ((AppEngSlot) other).itemHandler == this.itemHandler;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isEnabled() {
        return this.isSlotEnabled();
    }

    public boolean isSlotEnabled() {
        return true;
    }

    public ItemStack getDisplayStack() {
        return this.itemHandler.getStackInSlot(this.index);
    }

    public float getOpacityOfIcon() {
        return 0.4f;
    }

    public boolean renderIconWithItem() {
        return false;
    }

    public int getIcon() {
        return this.getIIcon();
    }

    public boolean isPlayerSide() {
        return this.isPlayerSide;
    }

    public boolean shouldDisplay() {
        return this.isSlotEnabled();
    }

    public int getX() {
        return this.defX;
    }

    public int getY() {
        return this.defY;
    }

    private int getIIcon() {
        return this.IIcon;
    }

    public void setIIcon(final int iIcon) {
        this.IIcon = iIcon;
    }

    private boolean isDisplay() {
        return this.isDisplay;
    }

    public void setDisplay(final boolean isDisplay) {
        this.isDisplay = isDisplay;
    }

    public boolean isDraggable() {
        return this.isDraggable;
    }

    private void setDraggable(final boolean isDraggable) {
        this.isDraggable = isDraggable;
    }

    void setPlayerSide(final boolean isPlayerSide) {
        this.isPlayerSide = isPlayerSide;
    }

    public hasCalculatedValidness getIsValid() {
        return this.isValid;
    }

    public void setIsValid(final hasCalculatedValidness isValid) {
        this.isValid = isValid;
    }

    protected AEBaseContainer getContainer() {
        return this.myContainer;
    }

    public void setContainer(final AEBaseContainer myContainer) {
        this.myContainer = myContainer;
    }

    public enum hasCalculatedValidness {
        NotAvailable, Valid, Invalid
    }
}
