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

import appeng.container.me.items.ItemTerminalContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.parts.IPartModel;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.me.items.PatternTermContainer;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;

public class PatternTerminalPart extends AbstractTerminalPart {

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
    public void readFromNBT(final CompoundNBT data) {
        super.readFromNBT(data);
        this.setCraftingRecipe(data.getBoolean("craftingMode"));
        this.setSubstitution(data.getBoolean("substitute"));
        this.pattern.readFromNBT(data, "pattern");
        this.output.readFromNBT(data, "outputList");
        this.crafting.readFromNBT(data, "craftingGrid");
    }

    @Override
    public void writeToNBT(final CompoundNBT data) {
        super.writeToNBT(data);
        data.putBoolean("craftingMode", this.craftingMode);
        data.putBoolean("substitute", this.substitute);
        this.pattern.writeToNBT(data, "pattern");
        this.output.writeToNBT(data, "outputList");
        this.crafting.writeToNBT(data, "craftingGrid");
    }

    @Override
    public ContainerType<?> getContainerType(final PlayerEntity p) {
        if (Platform.checkPermissions(p, this, SecurityPermissions.CRAFT, false)) {
            return PatternTermContainer.TYPE;
        }
        return ItemTerminalContainer.TYPE;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        if (inv == this.pattern && slot == 1) {
            final ItemStack is = this.pattern.getStackInSlot(1);
            final ICraftingPatternDetails details = Api.instance().crafting().decodePattern(is,
                    this.getHost().getTile().getWorld());
            if (details != null) {
                this.setCraftingRecipe(details.isCraftable());
                this.setSubstitution(details.canSubstitute());

                for (int x = 0; x < this.crafting.getSlots() && x < details.getSparseInputs().length; x++) {
                    final IAEItemStack item = details.getSparseInputs()[x];
                    this.crafting.setStackInSlot(x, item == null ? ItemStack.EMPTY : item.createItemStack());
                }

                for (int x = 0; x < this.output.getSlots() && x < details.getSparseOutputs().length; x++) {
                    final IAEItemStack item = details.getSparseOutputs()[x];
                    this.output.setStackInSlot(x, item == null ? ItemStack.EMPTY : item.createItemStack());
                }
            }
        } else if (inv == this.crafting) {
            this.fixCraftingRecipes();
        }

        this.getHost().markForSave();
    }

    private void fixCraftingRecipes() {
        if (this.craftingMode) {
            for (int x = 0; x < this.crafting.getSlots(); x++) {
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

    @Override
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("crafting")) {
            return this.crafting;
        }

        if (name.equals("output")) {
            return this.output;
        }

        if (name.equals("pattern")) {
            return this.pattern;
        }

        return super.getInventoryByName(name);
    }

    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }
}
