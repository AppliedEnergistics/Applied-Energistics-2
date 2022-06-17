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

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.Icon;
import appeng.core.AELog;
import appeng.menu.AEBaseMenu;

public class AppEngSlot extends Slot {
    private static final Container EMPTY_INVENTORY = new SimpleContainer(0);
    private final InternalInventory inventory;
    private final int invSlot;
    private boolean hideAmount;
    /**
     * Tooltip for this slot if the slot is empty.
     */
    @Nullable
    private List<Component> emptyTooltip;

    private boolean isDraggable = true;
    private AEBaseMenu menu = null;
    private boolean active = true;

    /**
     * Shows an icon from the icon sprite-sheet in the background of this slot.
     */
    @Nullable
    private Icon icon;
    /**
     * Caches if the item stack currently contained in this slot is "valid" or not for UI purposes.
     */
    private Boolean validState = null;

    public AppEngSlot(InternalInventory inv, int invSlot) {
        super(EMPTY_INVENTORY, invSlot, 0, 0);
        this.inventory = inv;
        this.invSlot = invSlot;
    }

    public Slot setNotDraggable() {
        this.setDraggable(false);
        return this;
    }

    public void clearStack() {
        this.inventory.setItemDirect(this.invSlot, ItemStack.EMPTY);
    }

    /**
     * @return Null if default tooltip should be shown. Empty to suppress tooltip entirely.
     */
    @Nullable
    public List<Component> getCustomTooltip(Function<ItemStack, List<Component>> getItemTooltip,
            ItemStack carriedItem) {
        if (getDisplayStack().isEmpty() && emptyTooltip != null) {
            return emptyTooltip;
        }
        return null;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (containsWrapperItem()) {
            return false;
        }
        if (this.isSlotEnabled()) {
            return this.inventory.isItemValid(this.invSlot, stack);
        }
        return false;
    }

    @Override
    public ItemStack getItem() {
        if (!this.isSlotEnabled()) {
            return ItemStack.EMPTY;
        }

        if (this.slot >= this.inventory.size()) {
            return ItemStack.EMPTY;
        }

        return this.inventory.getStackInSlot(this.invSlot);
    }

    @Override
    public void set(ItemStack stack) {
        if (this.isSlotEnabled()) {
            this.inventory.setItemDirect(this.invSlot, stack);
            this.setChanged();
        }
    }

    @Override
    public void initialize(ItemStack stack) {
        this.inventory.setItemDirect(this.invSlot, stack);
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
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(this.getMaxStackSize(), stack.getMaxStackSize());
    }

    @Override
    public boolean mayPickup(Player player) {
        if (containsWrapperItem()) {
            return false;
        }
        if (this.isSlotEnabled()) {
            return !this.inventory.extractItem(this.invSlot, 1, true).isEmpty();
        }
        return false;
    }

    @Override
    public ItemStack remove(int amount) {
        if (containsWrapperItem()) {
            return ItemStack.EMPTY;
        }

        return this.inventory.extractItem(this.invSlot, amount, false);
    }

    private boolean containsWrapperItem() {
        return GenericStack.isWrapped(getItem());
    }

    public boolean isSameInventory(Slot other) {
        return other instanceof AppEngSlot && ((AppEngSlot) other).inventory == this.inventory;
    }

    public InternalInventory getSlotInv() {
        return inventory.getSlotInv(invSlot);
    }

    @Override
    public boolean isActive() {
        return this.isSlotEnabled() && active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSlotEnabled() {
        return true;
    }

    /**
     * This method can be overridden in a subclass to show a specific item stack in the UI when this slot is being
     * rendered.
     */
    public ItemStack getDisplayStack() {
        var is = getItem();
        if (hideAmount) {
            var unwrapped = GenericStack.unwrapItemStack(is);
            if (unwrapped != null) {
                return GenericStack.wrapInItemStack(unwrapped.what(), 0);
            } else {
                is = is.copy();
                is.setCount(1);
            }
        }
        return is;
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

    public boolean isDraggable() {
        return this.isDraggable;
    }

    private void setDraggable(boolean isDraggable) {
        this.isDraggable = isDraggable;
    }

    protected AEBaseMenu getMenu() {
        return this.menu;
    }

    public void setMenu(AEBaseMenu menu) {
        this.menu = menu;
    }

    protected boolean isRemote() {
        return menu == null || menu.isClientSide();
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

    public boolean isHideAmount() {
        return hideAmount;
    }

    public void setHideAmount(boolean hideAmount) {
        this.hideAmount = hideAmount;
    }

    @Nullable
    public List<Component> getEmptyTooltip() {
        return emptyTooltip;
    }

    public void setEmptyTooltip(@Nullable List<Component> emptyTooltip) {
        this.emptyTooltip = emptyTooltip;
    }
}
