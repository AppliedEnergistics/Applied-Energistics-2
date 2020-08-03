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

import javax.annotation.Nonnull;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.SingleItemSlot;

import appeng.container.AEBaseContainer;

public class AppEngSlot extends Slot {
    private static final Inventory EMPTY_INVENTORY = new SimpleInventory(0);
    private final FixedItemInv itemHandler;
    private final SingleItemSlot backingSlot;
    private final int invSlot;

    private final int defX;
    private final int defY;
    private boolean isDraggable = true;
    private boolean isPlayerSide = false;
    private AEBaseContainer myContainer = null;
    private int IIcon = -1;
    private CalculatedValidity isValid;
    private boolean isDisplay = false;

    public AppEngSlot(final FixedItemInv inv, final int invSlot, final int x, final int y) {
        super(EMPTY_INVENTORY, invSlot, x, y);
        this.itemHandler = inv;
        this.invSlot = invSlot;
        this.backingSlot = inv.getSlot(invSlot);

        this.defX = x;
        this.defY = y;
        this.setIsValid(CalculatedValidity.NotAvailable);
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
        backingSlot.set(ItemStack.EMPTY);
    }

    @Override
    public boolean canInsert(@Nonnull final ItemStack stack) {
        if (this.isSlotEnabled()) {
            return this.backingSlot.isValid(stack);
        }
        return false;
    }

    @Override
    @Nonnull
    public ItemStack getStack() {
        if (!this.isSlotEnabled()) {
            return ItemStack.EMPTY;
        }

        if (this.invSlot >= this.itemHandler.getSlotCount()) {
            return ItemStack.EMPTY;
        }

        if (this.isDisplay()) {
            this.setDisplay(false);
            return this.getDisplayStack();
        }

        return this.backingSlot.get();
    }

    @Override
    public void setStack(final ItemStack stack) {
        if (this.isSlotEnabled()) {
            this.backingSlot.set(stack);
            this.markDirty();
        }
    }

    private void notifyContainerSlotChanged() {
        if (this.getContainer() != null) {
            this.getContainer().onSlotChange(this);
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.setIsValid(CalculatedValidity.NotAvailable);
        notifyContainerSlotChanged();
    }

    @Override
    public int getMaxStackAmount() {
        return this.backingSlot.getMaxAmount(ItemStack.EMPTY);
    }

    @Override
    public int getMaxStackAmount(@Nonnull ItemStack stack) {
        return Math.min(this.getMaxStackAmount(), stack.getMaxCount());
    }

    @Override
    public boolean canTakeItems(final PlayerEntity player) {
        if (this.isSlotEnabled()) {
            return this.backingSlot.couldExtractAnything();
        }
        return false;
    }

    @Override
    @Nonnull
    public ItemStack takeStack(int amount) {
        return this.backingSlot.extract(amount);
    }

    @Override
    public boolean hasStack() {
        return !backingSlot.get().isEmpty();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean doDrawHoveringEffect() {
        return this.isSlotEnabled();
    }

    public boolean isSlotEnabled() {
        return true;
    }

    public ItemStack getDisplayStack() {
        return this.backingSlot.get();
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

    public CalculatedValidity getIsValid() {
        return this.isValid;
    }

    public void setIsValid(final CalculatedValidity isValid) {
        this.isValid = isValid;
    }

    protected AEBaseContainer getContainer() {
        return this.myContainer;
    }

    public void setContainer(final AEBaseContainer myContainer) {
        this.myContainer = myContainer;
    }

    public enum CalculatedValidity {
        NotAvailable, Valid, Invalid
    }
}
