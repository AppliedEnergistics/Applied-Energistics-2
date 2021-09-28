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

package appeng.parts.reporting;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.crafting.IPatternDetails;
import appeng.api.inventories.InternalInventory;
import appeng.api.parts.IPartModel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEStack;
import appeng.core.AppEng;
import appeng.crafting.pattern.IAEPatternDetails;
import appeng.items.misc.FluidDummyItem;
import appeng.items.parts.PartModels;
import appeng.menu.me.items.ItemTerminalMenu;
import appeng.menu.me.items.PatternTermMenu;
import appeng.parts.PartModel;
import appeng.util.Platform;
import appeng.util.inv.AppEngInternalInventory;

public class PatternTerminalPart extends AbstractTerminalPart {

    /**
     * Identifies the sub-inventory used by the pattern terminal to encode the inputs of crafting or processing
     * patterns.
     */
    public static final ResourceLocation INV_CRAFTING = new ResourceLocation(
            "appliedenergistics2:pattern_terminal_crafting");

    /**
     * Identifies the sub-inventory used by the pattern terminal to encode the outputs of crafting or processing
     * patterns.
     */
    public static final ResourceLocation INV_OUTPUT = new ResourceLocation(
            "appliedenergistics2:pattern_terminal_output");

    @PartModels
    public static final ResourceLocation MODEL_OFF = new ResourceLocation(AppEng.MOD_ID, "part/pattern_terminal_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = new ResourceLocation(AppEng.MOD_ID, "part/pattern_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    private final AppEngInternalInventory crafting = new AppEngInternalInventory(this, 9);
    private final AppEngInternalInventory output = new AppEngInternalInventory(this, 3);
    private final AppEngInternalInventory pattern = new AppEngInternalInventory(this, 2);

    private boolean craftingMode = true;
    private boolean substitute = false;

    public PatternTerminalPart(final ItemStack is) {
        super(is);
    }

    @Override
    public void getDrops(final List<ItemStack> drops, final boolean wrenched) {
        for (final ItemStack is : this.pattern) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.setCraftingRecipe(data.getBoolean("craftingMode"));
        this.setSubstitution(data.getBoolean("substitute"));
        this.pattern.readFromNBT(data, "pattern");
        this.output.readFromNBT(data, "outputList");
        this.crafting.readFromNBT(data, "craftingGrid");
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        data.putBoolean("craftingMode", this.craftingMode);
        data.putBoolean("substitute", this.substitute);
        this.pattern.writeToNBT(data, "pattern");
        this.output.writeToNBT(data, "outputList");
        this.crafting.writeToNBT(data, "craftingGrid");
    }

    @Override
    public MenuType<?> getMenuType(final Player p) {
        if (Platform.checkPermissions(p, this, SecurityPermissions.CRAFT, false)) {
            return PatternTermMenu.TYPE;
        }
        return ItemTerminalMenu.TYPE;
    }

    @Override
    public void onChangeInventory(final InternalInventory inv, final int slot,
            final ItemStack removedStack, final ItemStack newStack) {
        if (inv == this.pattern && slot == 1) {
            final ItemStack is = this.pattern.getStackInSlot(1);
            final IPatternDetails details = AEApi.patterns().decodePattern(is,
                    this.getHost().getBlockEntity().getLevel());
            if (details instanceof IAEPatternDetails aeDetails) {
                this.setCraftingRecipe(aeDetails.isCraftable());
                this.setSubstitution(aeDetails.canSubstitute());

                for (int x = 0; x < this.crafting.size() && x < aeDetails.getSparseInputs().length; x++) {
                    this.crafting.setItemDirect(x, getDisplayStack(aeDetails.getSparseInputs()[x]));
                }

                for (int x = 0; x < this.output.size() && x < aeDetails.getSparseOutputs().length; x++) {
                    this.output.setItemDirect(x, getDisplayStack(aeDetails.getSparseOutputs()[x]));
                }
            }
        } else if (inv == this.crafting) {
            this.fixCraftingRecipes();
        }

        this.getHost().markForSave();
    }

    private ItemStack getDisplayStack(@Nullable IAEStack aeStack) {
        if (aeStack == null) {
            return ItemStack.EMPTY;
        } else if (aeStack.getChannel() == StorageChannels.items()) {
            return aeStack.cast(StorageChannels.items()).createItemStack();
        } else if (aeStack.getChannel() == StorageChannels.fluids()) {
            return FluidDummyItem.fromFluidStack(aeStack.cast(StorageChannels.fluids()).getFluidStack(), true);
        } else {
            throw new IllegalArgumentException("Only item and fluid stacks are supported");
        }
    }

    private void fixCraftingRecipes() {
        if (this.craftingMode) {
            for (int x = 0; x < this.crafting.size(); x++) {
                final ItemStack is = this.crafting.getStackInSlot(x);
                if (!is.isEmpty()) {
                    is.setCount(1);
                }
            }
        }
    }

    public boolean isCraftingRecipe() {
        return this.craftingMode;
    }

    public void setCraftingRecipe(final boolean craftingMode) {
        this.craftingMode = craftingMode;
        this.fixCraftingRecipes();
    }

    public boolean isSubstitution() {
        return this.substitute;
    }

    public void setSubstitution(final boolean canSubstitute) {
        this.substitute = canSubstitute;
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(INV_CRAFTING)) {
            return this.crafting;
        } else if (id.equals(INV_OUTPUT)) {
            return this.output;
        } else if (id.equals(PATTERNS)) {
            return this.pattern;
        } else {
            return super.getSubInventory(id);
        }
    }

    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }
}
