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

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.AESmithingTablePattern;
import appeng.crafting.pattern.AEStonecuttingPattern;
import appeng.helpers.IPatternTerminalLogicHost;
import appeng.util.ConfigInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.AEItemDefinitionFilter;

public class PatternEncodingLogic implements InternalInventoryHost {

    private final IPatternTerminalLogicHost host;

    private static final int MAX_INPUT_SLOTS = Math.max(AECraftingPattern.CRAFTING_GRID_SLOTS,
            AEProcessingPattern.MAX_INPUT_SLOTS);
    private static final int MAX_OUTPUT_SLOTS = AEProcessingPattern.MAX_OUTPUT_SLOTS;

    private final ConfigInventory encodedInputInv = ConfigInventory.configStacks(MAX_INPUT_SLOTS)
            .changeListener(this::onEncodedInputChanged).allowOverstacking(true).build();
    private final ConfigInventory encodedOutputInv = ConfigInventory.configStacks(MAX_OUTPUT_SLOTS)
            .changeListener(this::onEncodedOutputChanged).allowOverstacking(true).build();

    private final AppEngInternalInventory blankPatternInv = new AppEngInternalInventory(this, 1);
    private final AppEngInternalInventory encodedPatternInv = new AppEngInternalInventory(this, 1);

    private EncodingMode mode = EncodingMode.CRAFTING;
    private boolean substitute = false;
    private boolean substituteFluids = true;
    private boolean isLoading = false;
    @Nullable
    private ResourceKey<Recipe<?>> stonecuttingRecipeId;

    public PatternEncodingLogic(IPatternTerminalLogicHost host) {
        this.host = host;
        this.blankPatternInv.setFilter(new AEItemDefinitionFilter(AEItems.BLANK_PATTERN));
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        // Load the encoded inputs and outputs of a pattern if it changes
        if (inv == this.encodedPatternInv) {
            loadEncodedPattern(encodedPatternInv.getStackInSlot(0));
        }

        saveChanges();
    }

    public void saveChanges() {
        // Do not re-save while we're loading since it could overwrite the NBT with incomplete data
        if (!isLoading) {
            host.markForSave();
        }
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        saveChanges();
    }

    @Override
    public boolean isClientSide() {
        return host.getLevel().isClientSide();
    }

    private void onEncodedInputChanged() {
        fixCraftingRecipes();
        saveChanges();
    }

    private void onEncodedOutputChanged() {
        saveChanges();
    }

    private void loadEncodedPattern(ItemStack pattern) {
        if (pattern.isEmpty()) {
            return;
        }

        var details = PatternDetailsHelper.decodePattern(pattern, host.getLevel());

        if (details instanceof AECraftingPattern craftingPattern) {
            loadCraftingPattern(craftingPattern);
        } else if (details instanceof AEProcessingPattern processingPattern) {
            loadProcessingPattern(processingPattern);
        } else if (details instanceof AESmithingTablePattern smithingTablePattern) {
            loadSmithingTablePattern(smithingTablePattern);
        } else if (details instanceof AEStonecuttingPattern stonecuttingPattern) {
            loadStonecuttingPattern(stonecuttingPattern);
        }

        saveChanges();
    }

    private void loadCraftingPattern(AECraftingPattern pattern) {
        setMode(EncodingMode.CRAFTING);
        this.substitute = pattern.canSubstitute();
        this.substituteFluids = pattern.canSubstituteFluids();

        fillInventoryFromSparseStacks(encodedInputInv, pattern.getSparseInputs());
        fillInventoryFromSparseStacks(encodedOutputInv, pattern.getSparseOutputs());
    }

    private void loadProcessingPattern(AEProcessingPattern pattern) {
        setMode(EncodingMode.PROCESSING);

        fillInventoryFromSparseStacks(encodedInputInv, pattern.getSparseInputs());
        fillInventoryFromSparseStacks(encodedOutputInv, pattern.getSparseOutputs());
    }

    private void loadSmithingTablePattern(AESmithingTablePattern pattern) {
        setMode(EncodingMode.SMITHING_TABLE);
        this.substitute = pattern.canSubstitute();

        encodedInputInv.clear();
        encodedInputInv.setStack(0, new GenericStack(pattern.getTemplate(), 1));
        encodedInputInv.setStack(1, new GenericStack(pattern.getBase(), 1));
        encodedInputInv.setStack(2, new GenericStack(pattern.getAddition(), 1));
        encodedOutputInv.clear();
    }

    private void loadStonecuttingPattern(AEStonecuttingPattern pattern) {
        setMode(EncodingMode.STONECUTTING);
        stonecuttingRecipeId = pattern.getRecipeId();

        this.substitute = pattern.canSubstitute;

        encodedInputInv.clear();
        encodedInputInv.setStack(0, new GenericStack(pattern.getInput(), 1));
        encodedOutputInv.clear();
    }

    private static void fillInventoryFromSparseStacks(ConfigInventory inv, List<GenericStack> stacks) {
        inv.beginBatch();
        try {
            for (int i = 0; i < inv.size(); i++) {
                inv.setStack(i, i < stacks.size() ? stacks.get(i) : null);
            }
        } finally {
            inv.endBatch();
        }
    }

    public EncodingMode getMode() {
        return mode;
    }

    public void setMode(EncodingMode mode) {
        this.mode = mode;
        this.fixCraftingRecipes();
        this.saveChanges();
    }

    public boolean isSubstitution() {
        return this.substitute;
    }

    public void setSubstitution(boolean canSubstitute) {
        this.substitute = canSubstitute;
        this.saveChanges();
    }

    public boolean isFluidSubstitution() {
        return this.substituteFluids;
    }

    public void setFluidSubstitution(boolean canSubstitute) {
        this.substituteFluids = canSubstitute;
        this.saveChanges();
    }

    public @Nullable ResourceKey<Recipe<?>> getStonecuttingRecipeId() {
        return stonecuttingRecipeId;
    }

    public void setStonecuttingRecipeId(ResourceKey<Recipe<?>> stonecuttingRecipeId) {
        this.stonecuttingRecipeId = stonecuttingRecipeId;
        this.saveChanges();
    }

    /**
     * The inventory used to store the inputs for encoding them into a pattern. Does not contain real items.
     * <p/>
     * Used for all {@link #getMode() modes}.
     */
    public ConfigInventory getEncodedInputInv() {
        return encodedInputInv;
    }

    /**
     * The inventory used to store the outputs for encoding them into a pattern. Does not contain real items.
     * <p/>
     * Not used for crafting {@link #getMode() modes}.
     */
    public ConfigInventory getEncodedOutputInv() {
        return encodedOutputInv;
    }

    /**
     * Inventory of size 1, which contains the blank patterns for encoding.
     */
    public InternalInventory getBlankPatternInv() {
        return blankPatternInv;
    }

    /**
     * Inventory of size 1, which will receive the encoded pattern and can be used to place an already-encoded pattern
     * for re-encoding.
     */
    public InternalInventory getEncodedPatternInv() {
        return encodedPatternInv;
    }

    public void readFromNBT(ValueInput data) {
        isLoading = true;
        try {
            try {
                this.mode = EncodingMode.valueOf(data.getStringOr("mode", ""));
            } catch (IllegalArgumentException ignored) {
                this.mode = EncodingMode.CRAFTING;
            }
            this.setSubstitution(data.getBooleanOr("substitute", false));
            this.setFluidSubstitution(data.getBooleanOr("substituteFluids", false));

            var stonecuttingRecipeId = data.read("stonecuttingRecipeId", ResourceLocation.CODEC).orElse(null);
            if (stonecuttingRecipeId != null) {
                this.stonecuttingRecipeId = ResourceKey.create(Registries.RECIPE, stonecuttingRecipeId);
            } else {
                this.stonecuttingRecipeId = null;
            }

            blankPatternInv.readFromNBT(data, "blankPattern");
            encodedPatternInv.readFromNBT(data, "encodedPattern");

            encodedInputInv.readFromChildTag(data, "encodedInputs");
            encodedOutputInv.readFromChildTag(data, "encodedOutputs");
        } finally {
            isLoading = false;
        }
    }

    public void writeToNBT(ValueOutput output) {
        output.putString("mode", this.mode.name());
        output.putBoolean("substitute", this.substitute);
        output.putBoolean("substituteFluids", this.substituteFluids);
        if (this.stonecuttingRecipeId != null) {
            output.putString("stonecuttingRecipeId", this.stonecuttingRecipeId.location().toString());
        }
        blankPatternInv.writeToNBT(output, "blankPattern");
        encodedPatternInv.writeToNBT(output, "encodedPattern");
        encodedInputInv.writeToChildTag(output, "encodedInputs");
        encodedOutputInv.writeToChildTag(output, "encodedOutputs");
    }

    private void fixCraftingRecipes() {
        // Do not do this on the client since the server will always sync the correct state to us anyway
        if (host.getLevel() == null || host.getLevel().isClientSide()) {
            return;
        }

        if (getMode() != EncodingMode.PROCESSING) {
            var craftingGrid = getEncodedInputInv();
            for (int slot = 0; slot < craftingGrid.size(); slot++) {
                var stack = craftingGrid.getStack(slot);
                if (stack == null) {
                    continue;
                }

                // Crafting recipes only support real items, not wrapped fluids or similar things
                if (!AEItemKey.is(stack.what())) {
                    craftingGrid.setStack(slot, null);
                    continue;
                }

                // Clamp item count to 1 for crafting recipes
                if (stack.amount() != 1) {
                    craftingGrid.setStack(slot, new GenericStack(stack.what(), 1));
                }
            }
        }
    }
}
