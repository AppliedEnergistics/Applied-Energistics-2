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

package appeng.parts.encoding;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.SecurityPermissions;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.GenericStack;
import appeng.core.AppEng;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.IAEPatternDetails;
import appeng.helpers.IPatternTerminalHost;
import appeng.items.parts.PartModels;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractTerminalPart;
import appeng.util.Platform;
import appeng.util.inv.AppEngInternalInventory;

public class PatternEncodingTerminalPart extends AbstractTerminalPart implements IPatternTerminalHost {

    @PartModels
    public static final ResourceLocation MODEL_OFF = new ResourceLocation(AppEng.MOD_ID,
            "part/pattern_encoding_terminal_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = new ResourceLocation(AppEng.MOD_ID,
            "part/pattern_encoding_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    private final AppEngInternalInventory crafting = new AppEngInternalInventory(this, 9);
    private final AppEngInternalInventory output = new AppEngInternalInventory(this, 3);
    private final AppEngInternalInventory pattern = new AppEngInternalInventory(this, 2);

    private EncodingMode mode = EncodingMode.CRAFTING;
    private boolean substitute = false;
    private boolean substituteFluids = true;

    public PatternEncodingTerminalPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
        for (ItemStack is : this.pattern) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);

        try {
            this.mode = EncodingMode.valueOf(data.getString("mode"));
        } catch (IllegalArgumentException ignored) {
            this.mode = EncodingMode.CRAFTING;
        }
        this.setSubstitution(data.getBoolean("substitute"));
        this.setFluidSubstitution(data.getBoolean("substituteFluids"));
        this.pattern.readFromNBT(data, "pattern");
        this.output.readFromNBT(data, "outputList");
        this.crafting.readFromNBT(data, "craftingGrid");
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putString("mode", this.mode.name());
        data.putBoolean("substitute", this.substitute);
        data.putBoolean("substituteFluids", this.substituteFluids);
        this.pattern.writeToNBT(data, "pattern");
        this.output.writeToNBT(data, "outputList");
        this.crafting.writeToNBT(data, "craftingGrid");
    }

    @Override
    public MenuType<?> getMenuType(Player p) {
        if (Platform.checkPermissions(p, this, SecurityPermissions.CRAFT, false, false)) {
            return PatternEncodingTermMenu.TYPE;
        }
        return MEStorageMenu.TYPE;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        if (inv == this.pattern && slot == 1) {
            var is = this.pattern.getStackInSlot(1);
            var details = PatternDetailsHelper.decodePattern(is,
                    this.getHost().getBlockEntity().getLevel());
            if (details instanceof AECraftingPattern) {
                setMode(EncodingMode.CRAFTING);
            } else if (details instanceof AEProcessingPattern) {
                setMode(EncodingMode.PROCESSING);
            }
            if (details instanceof IAEPatternDetails aeDetails) {
                this.setSubstitution(aeDetails.canSubstitute());
                this.setFluidSubstitution(aeDetails.canSubstituteFluids());

                for (int x = 0; x < this.crafting.size() && x < aeDetails.getSparseInputs().length; x++) {
                    this.crafting.setItemDirect(x, GenericStack.wrapInItemStack(aeDetails.getSparseInputs()[x]));
                }

                for (int x = 0; x < this.output.size() && x < aeDetails.getSparseOutputs().length; x++) {
                    this.output.setItemDirect(x, GenericStack.wrapInItemStack(aeDetails.getSparseOutputs()[x]));
                }
            }
        } else if (inv == this.crafting) {
            this.fixCraftingRecipes();
        }

        this.getHost().markForSave();
    }

    @Override
    public EncodingMode getMode() {
        return mode;
    }

    @Override
    public void setMode(EncodingMode mode) {
        this.mode = mode;
        this.fixCraftingRecipes();
    }

    public boolean isSubstitution() {
        return this.substitute;
    }

    public void setSubstitution(boolean canSubstitute) {
        this.substitute = canSubstitute;
    }

    public boolean isFluidSubstitution() {
        return this.substituteFluids;
    }

    public void setFluidSubstitution(boolean canSubstitute) {
        this.substituteFluids = canSubstitute;
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
