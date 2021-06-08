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
import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.SingleItemSlot;

import appeng.client.gui.Icon;
import appeng.container.AEBaseContainer;
import appeng.core.AELog;
import appeng.util.helpers.ItemHandlerUtil;

public class AppEngSlot extends Slot {
    private static final IInventory EMPTY_INVENTORY = new Inventory(0);
    private final FixedItemInv itemHandler;
    private final SingleItemSlot backingSlot;
    private final int invSlot;

    private boolean isDraggable = true;
    private AEBaseContainer container = null;

    /**
     * Shows an icon from the icon sprite-sheet in the background of this slot.
     */
    @Nullable
    private Icon icon;
    /**
     * Caches if the item stack currently contained in this slot is "valid" or not for UI purposes.
     */
    private Boolean validState = null;
    private boolean rendering = false;

    public AppEngSlot(final FixedItemInv inv, final int invSlot) {
        super(EMPTY_INVENTORY, invSlot, 0, 0);
        this.itemHandler = inv;
        this.invSlot = invSlot;
        this.backingSlot = inv.getSlot(invSlot);
    }

    public Slot setNotDraggable() {
        this.setDraggable(false);
        return this;
    }

    public String getTooltip() {
        return null;
    }

    public void clearStack() {
        backingSlot.set(ItemStack.EMPTY);
    }

    @Override
    public boolean isItemValid(@Nonnull final ItemStack stack) {
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

        // Some slots may want to display a different stack in the GUI, which we solve by
        // returning getDisplayStack() during rendering, which a slot can override.
        if (this.rendering) {
            this.rendering = false;
            return this.getDisplayStack();
        }

        return this.backingSlot.get();
    }

    @Override
    public void putStack(final ItemStack stack) {
        if (this.isSlotEnabled()) {
            this.backingSlot.set(stack);
            this.onSlotChanged();
        }
    }

    private void notifyContainerSlotChanged() {
        if (this.getContainer() != null) {
            this.getContainer().onSlotChange(this);
        }
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();
        this.validState = null;
        notifyContainerSlotChanged();
    }

    @Override
    public int getSlotStackLimit() {
        return this.backingSlot.getMaxAmount(ItemStack.EMPTY);
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        return Math.min(this.getSlotStackLimit(), stack.getMaxStackSize());
    }

    @Override
    public boolean canTakeStack(final PlayerEntity player) {
        if (this.isSlotEnabled()) {
            return this.backingSlot.couldExtractAnything();
        }
        return false;
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int amount) {
        return this.backingSlot.extract(amount);
    }

    @Override
    public boolean getHasStack() {
        return !backingSlot.get().isEmpty();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean isEnabled() {
        return this.isSlotEnabled();
    }

    public boolean isSlotEnabled() {
        return true;
    }

    /**
     * This method can be overridden in a subclass to show a specific item stack in the UI when this slot is being
     * rendered.
     */
    public ItemStack getDisplayStack() {
        return this.backingSlot.get();
    }

    public float getOpacityOfIcon() {
        return 0.4f;
    }

    public boolean renderIconWithItem() {
        return false;
    }

    public Icon getIcon() {
        return this.icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    /**
     * Indicate that this slot is currently being rendered, which is used to provide a slot with the ability to return a
     * different stack for rendering purposes by overriding {@link #getDisplayStack()}.
     */
    public void setRendering(final boolean rendering) {
        this.rendering = rendering;
    }

    public boolean isDraggable() {
        return this.isDraggable;
    }

    private void setDraggable(final boolean isDraggable) {
        this.isDraggable = isDraggable;
    }

    protected AEBaseContainer getContainer() {
        return this.container;
    }

    public void setContainer(final AEBaseContainer myContainer) {
        this.container = myContainer;
    }

    protected boolean isRemote() {
        return container == null || container.isRemote();
    }

    /**
     * @return True if the slot is in a valid state
     */
    public final boolean isValid() {
        // Lazily compute whether the currently held item is valid or not
        if (validState == null) {
            try {
                validState = getCurrentValidationState();
            } catch (Exception e) {
                validState = false;
                AELog.warn("Failed to update validation state for slot %s: %s", this, e);
            }
        }
        return validState;
    }

    /**
     * Override in subclasses to customize the validation state of a slot, which is used to draw a red backdrop for
     * invalid slots. Please note that the return value is cached.
     *
     * @return True if the current state of the slot is valid, false otherwise.
     * @see #resetCachedValidation()
     */
    protected boolean getCurrentValidationState() {
        return true;
    }

    /**
     * Call to cause {@link #isValid()} to recompute the state next time it is called.
     */
    public void resetCachedValidation() {
        this.validState = null;
    }

}
