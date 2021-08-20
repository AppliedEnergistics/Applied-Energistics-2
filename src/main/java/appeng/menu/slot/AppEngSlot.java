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

package appeng.menu.slot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;
import appeng.client.gui.Icon;
import appeng.core.AELog;
import appeng.menu.AEBaseMenu;

public class AppEngSlot extends Slot {
    private static final Container EMPTY_INVENTORY = new SimpleContainer(0);
    private final InternalInventory inventory;
    private final int invSlot;

    private boolean isDraggable = true;
    private AEBaseMenu menu = null;

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

    public AppEngSlot(final InternalInventory inv, final int invSlot) {
        super(EMPTY_INVENTORY, invSlot, 0, 0);
        this.inventory = inv;
        this.invSlot = invSlot;
    }

    public Slot setNotDraggable() {
        this.setDraggable(false);
        return this;
    }

    public String getTooltip() {
        return null;
    }

    public void clearStack() {
        this.inventory.setItemDirect(this.invSlot, ItemStack.EMPTY);
    }

    @Override
    public boolean mayPlace(@Nonnull final ItemStack stack) {
        if (this.isSlotEnabled()) {
            return this.inventory.isItemValid(this.invSlot, stack);
        }
        return false;
    }

    @Override
    @Nonnull
    public ItemStack getItem() {
        if (!this.isSlotEnabled()) {
            return ItemStack.EMPTY;
        }

        if (this.slot >= this.inventory.size()) {
            return ItemStack.EMPTY;
        }

        // Some slots may want to display a different stack in the GUI, which we solve by
        // returning getDisplayStack() during rendering, which a slot can override.
        if (this.rendering) {
            this.rendering = false;
            return this.getDisplayStack();
        }

        return this.inventory.getStackInSlot(this.invSlot);
    }

    @Override
    public void set(final ItemStack stack) {
        if (this.isSlotEnabled()) {
            this.inventory.setItemDirect(this.invSlot, stack);
            this.setChanged();
        }
    }

    private void notifyContainerSlotChanged() {
        if (this.getMenu() != null) {
            this.getMenu().onSlotChange(this);
        }
    }

    public InternalInventory getInventory() {
        return this.inventory;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.validState = null;
        notifyContainerSlotChanged();
    }

    @Override
    public int getMaxStackSize() {
        return this.inventory.getSlotLimit(this.invSlot);
    }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) {
        return Math.min(this.getMaxStackSize(), stack.getMaxStackSize());
    }

    @Override
    public boolean mayPickup(final Player player) {
        if (this.isSlotEnabled()) {
            return !this.inventory.extractItem(this.invSlot, 1, true).isEmpty();
        }
        return false;
    }

    @Override
    @Nonnull
    public ItemStack remove(int amount) {
        return this.inventory.extractItem(this.invSlot, amount, false);
    }

    public boolean isSameInventory(Slot other) {
        return other instanceof AppEngSlot && ((AppEngSlot) other).inventory == this.inventory;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean isActive() {
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
        return this.inventory.getStackInSlot(this.invSlot);
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

    protected AEBaseMenu getMenu() {
        return this.menu;
    }

    public void setMenu(final AEBaseMenu menu) {
        this.menu = menu;
    }

    protected boolean isRemote() {
        return menu == null || menu.isRemote();
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
