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

package appeng.menu.me.items;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.StorageHelper;
import appeng.core.definitions.AEItems;
import appeng.core.sync.packets.PatternSlotPacket;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.helpers.FluidContainerHelper;
import appeng.helpers.IMenuCraftingPacket;
import appeng.helpers.IPatternTerminalHost;
import appeng.items.storage.ViewCellItem;
import appeng.me.helpers.MachineSource;
import appeng.menu.NullMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEMonitorableMenu;
import appeng.menu.slot.FakeCraftingMatrixSlot;
import appeng.menu.slot.IOptionalSlotHost;
import appeng.menu.slot.OptionalFakeSlot;
import appeng.menu.slot.PatternOutputsSlot;
import appeng.menu.slot.PatternTermSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.Platform;
import appeng.util.inv.CarriedItemInventory;
import appeng.util.inv.PlayerInternalInventory;

/**
 * Can only be used with a host that implements {@link IPatternTerminalHost}.
 *
 * @see appeng.client.gui.me.items.PatternTermScreen
 */
public class PatternTermMenu extends MEMonitorableMenu implements IOptionalSlotHost, IMenuCraftingPacket {

    private static final String ACTION_SET_CRAFT_MODE = "setCraftMode";
    private static final String ACTION_ENCODE = "encode";
    private static final String ACTION_CLEAR = "clear";
    private static final String ACTION_SET_SUBSTITUTION = "setSubstitution";
    private static final String ACTION_SET_FLUID_SUBSTITUTION = "setFluidSubstitution";
    private static final String ACTION_CONVERT_ITEMS_TO_FLUIDS = "convertItemsToFluids";

    public static MenuType<PatternTermMenu> TYPE = MenuTypeBuilder
            .create(PatternTermMenu::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("patternterm");

    private final IPatternTerminalHost patternTerminal;
    private final InternalInventory craftingGridInv;
    private final FakeCraftingMatrixSlot[] craftingGridSlots = new FakeCraftingMatrixSlot[9];
    private final OptionalFakeSlot[] processingOutputSlots = new OptionalFakeSlot[3];
    private final PatternTermSlot craftOutputSlot;
    private final RestrictedInputSlot blankPatternSlot;
    private final RestrictedInputSlot encodedPatternSlot;

    private CraftingRecipe currentRecipe;
    private boolean currentRecipeCraftingMode;

    @GuiSync(97)
    public boolean craftingMode = true;
    @GuiSync(96)
    public boolean substitute = false;
    @GuiSync(95)
    public boolean substituteFluids = true;
    /**
     * Whether fluids can be substituted or not depends on the recipe. This set contains the slots of the crafting
     * matrix that support such substitution.
     */
    public IntSet slotsSupportingFluidSubstitution = new IntArraySet();

    public PatternTermMenu(int id, final Inventory ip, final ITerminalHost monitorable) {
        this(TYPE, id, ip, monitorable, true);
    }

    public PatternTermMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host,
            boolean bindInventory) {
        super(menuType, id, ip, host, bindInventory);
        this.patternTerminal = (IPatternTerminalHost) host;

        var patternInv = this.getPatternTerminal().getSubInventory(ISegmentedInventory.PATTERNS);
        var output = this.getPatternTerminal().getSubInventory(IPatternTerminalHost.INV_OUTPUT);

        // Create the 3x3 crafting input grid, which is used for both processing and crafting mode
        this.craftingGridInv = this.getPatternTerminal().getSubInventory(IPatternTerminalHost.INV_CRAFTING);
        for (int i = 0; i < 9; i++) {
            this.addSlot(this.craftingGridSlots[i] = new FakeCraftingMatrixSlot(this.craftingGridInv, i),
                    SlotSemantic.CRAFTING_GRID);
        }

        // Create the output slot used for crafting mode patterns
        this.addSlot(this.craftOutputSlot = new PatternTermSlot(ip.player, this.getActionSource(), this.powerSource,
                host.getInventory(), this.craftingGridInv, patternInv, this, 2, this), SlotSemantic.CRAFTING_RESULT);
        this.craftOutputSlot.setIcon(null);

        // Create slots for the outputs of processing-mode patterns. Unrolled as each as a different semantic
        this.addSlot(this.processingOutputSlots[0] = new PatternOutputsSlot(output, this, 0, 1),
                SlotSemantic.PROCESSING_PRIMARY_RESULT);
        this.addSlot(this.processingOutputSlots[1] = new PatternOutputsSlot(output, this, 1, 1),
                SlotSemantic.PROCESSING_FIRST_OPTIONAL_RESULT);
        this.addSlot(this.processingOutputSlots[2] = new PatternOutputsSlot(output, this, 2, 1),
                SlotSemantic.PROCESSING_SECOND_OPTIONAL_RESULT);

        for (int i = 0; i < 3; i++) {
            this.processingOutputSlots[i].setRenderDisabled(false);
            this.processingOutputSlots[i].setIcon(null);
        }

        this.addSlot(this.blankPatternSlot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.BLANK_PATTERN,
                patternInv, 0), SlotSemantic.BLANK_PATTERN);
        this.addSlot(
                this.encodedPatternSlot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ENCODED_PATTERN,
                        patternInv, 1),
                SlotSemantic.ENCODED_PATTERN);

        this.encodedPatternSlot.setStackLimit(1);

        registerClientAction(ACTION_ENCODE, this::encode);
        registerClientAction(ACTION_CLEAR, this::clear);
        registerClientAction(ACTION_SET_CRAFT_MODE, Boolean.class, getPatternTerminal()::setCraftingRecipe);
        registerClientAction(ACTION_SET_SUBSTITUTION, Boolean.class, getPatternTerminal()::setSubstitution);
        registerClientAction(ACTION_SET_FLUID_SUBSTITUTION, Boolean.class, getPatternTerminal()::setFluidSubstitution);
        registerClientAction(ACTION_CONVERT_ITEMS_TO_FLUIDS, this::convertItemsToFluids);
    }

    @Override
    public void setItem(int slotID, int stateId, ItemStack stack) {
        super.setItem(slotID, stateId, stack);
        this.getAndUpdateOutput();
    }

    private ItemStack getAndUpdateOutput() {
        var level = this.getPlayerInventory().player.level;
        var ic = new CraftingContainer(this, 3, 3);

        boolean invalidIngredients = false;
        for (int x = 0; x < ic.getContainerSize(); x++) {
            var stack = unwrapCraftingIngredient(this.craftingGridInv.getStackInSlot(x));
            if (stack != null) {
                ic.setItem(x, stack);
            } else {
                invalidIngredients = true;
            }
        }

        if (this.currentRecipe == null || !this.currentRecipe.matches(ic, level)) {
            if (invalidIngredients) {
                this.currentRecipe = null;
            } else {
                this.currentRecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, ic, level).orElse(null);
            }
            this.currentRecipeCraftingMode = isCraftingMode();
            checkFluidSubstitutionSupport();
        }

        final ItemStack is;

        if (this.currentRecipe == null) {
            is = ItemStack.EMPTY;
        } else {
            is = this.currentRecipe.assemble(ic);
        }

        this.craftOutputSlot.setDisplayedCraftingOutput(is);
        return is;
    }

    private void checkFluidSubstitutionSupport() {
        this.slotsSupportingFluidSubstitution.clear();

        if (this.currentRecipe == null) {
            return; // No recipe -> no substitution
        }

        var encodedPattern = encodePattern();
        if (encodedPattern != null) {
            var decodedPattern = PatternDetailsHelper.decodePattern(encodedPattern,
                    this.getPlayerInventory().player.level);
            if (decodedPattern instanceof AECraftingPattern craftingPattern) {
                for (int i = 0; i < craftingPattern.getSparseInputs().length; i++) {
                    if (craftingPattern.getValidFluid(i) != null) {
                        slotsSupportingFluidSubstitution.add(i);
                    }
                }
            }
        }
    }

    public void encode() {
        if (isClient()) {
            sendClientAction(ACTION_ENCODE);
            return;
        }

        ItemStack encodedPattern = encodePattern();
        if (encodedPattern != null) {
            var encodeOutput = this.encodedPatternSlot.getItem();

            // first check the output slots, should either be null, or a pattern (encoded or otherwise)
            if (!encodeOutput.isEmpty()
                    && !PatternDetailsHelper.isEncodedPattern(encodeOutput)
                    && !AEItems.BLANK_PATTERN.isSameAs(encodeOutput)) {
                return;
            } // if nothing is there we should snag a new pattern.
            else if (encodeOutput.isEmpty()) {
                var blankPattern = this.blankPatternSlot.getItem();
                if (!isPattern(blankPattern)) {
                    return; // no blanks.
                }

                // remove one, and clear the input slot.
                blankPattern.shrink(1);
                if (blankPattern.getCount() <= 0) {
                    this.blankPatternSlot.set(ItemStack.EMPTY);
                }
            }

            this.encodedPatternSlot.set(encodedPattern);
        } else {
            clearPattern();
        }
    }

    /**
     * Clears the pattern in the encoded pattern slot.
     */
    private void clearPattern() {
        var encodedPattern = this.encodedPatternSlot.getItem();
        if (PatternDetailsHelper.isEncodedPattern(encodedPattern)) {
            this.encodedPatternSlot.set(
                    AEItems.BLANK_PATTERN.stack(encodedPattern.getCount()));
        }
    }

    @Nullable
    private ItemStack encodePattern() {
        if (this.isCraftingMode()) {
            return encodeCraftingPattern();
        } else {
            return encodeProcessingPattern();
        }
    }

    @Nullable
    private ItemStack encodeCraftingPattern() {
        var ingredients = new ItemStack[this.craftingGridSlots.length];
        boolean valid = false;
        for (int x = 0; x < this.craftingGridSlots.length; x++) {
            ingredients[x] = unwrapCraftingIngredient(this.craftingGridSlots[x].getItem());
            if (ingredients[x] == null) {
                return null; // Invalid item
            } else if (!ingredients[x].isEmpty()) {
                // At least one input must be set, but it doesn't matter which one
                valid = true;
            }
        }
        if (!valid) {
            return null;
        }

        var result = this.getAndUpdateOutput();
        if (result.isEmpty() || currentRecipe == null) {
            return null;
        }

        return PatternDetailsHelper.encodeCraftingPattern(this.currentRecipe, ingredients, result, isSubstitute(),
                isSubstituteFluids());
    }

    @Nullable
    private ItemStack encodeProcessingPattern() {
        var inputs = new GenericStack[this.craftingGridSlots.length];
        boolean valid = false;
        for (int x = 0; x < this.craftingGridSlots.length; x++) {
            inputs[x] = GenericStack.fromItemStack(this.craftingGridSlots[x].getItem());
            if (inputs[x] != null) {
                // At least one input must be set, but it doesn't matter which one
                valid = true;
            }
        }
        if (!valid) {
            return null;
        }

        var outputs = new GenericStack[3];
        for (int i = 0; i < this.processingOutputSlots.length; i++) {
            outputs[i] = GenericStack.fromItemStack(this.processingOutputSlots[i].getItem());
        }
        if (outputs[0] == null) {
            // The first output slot is required
            return null;
        }

        return PatternDetailsHelper.encodeProcessingPattern(inputs, outputs);
    }

    @Nullable
    private ItemStack unwrapCraftingIngredient(ItemStack ingredient) {
        var unwrapped = GenericStack.unwrapItemStack(ingredient);
        if (unwrapped != null) {
            if (unwrapped.what() instanceof AEItemKey itemKey) {
                return itemKey.toStack(1);
            } else {
                return null; // There's something in this slot that's not an item
            }
        } else {
            return ingredient;
        }
    }

    private boolean isPattern(final ItemStack output) {
        if (output.isEmpty()) {
            return false;
        }

        return AEItems.BLANK_PATTERN.isSameAs(output);
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        if (idx == 1) {
            return isServer() ? !this.getPatternTerminal().isCraftingRecipe() : !this.isCraftingMode();
        } else if (idx == 2) {
            return isServer() ? this.getPatternTerminal().isCraftingRecipe() : this.isCraftingMode();
        } else {
            return false;
        }
    }

    /**
     * Triggered by clicking on the pattern terminals crafting result slot. Will either grab the item from the network
     * inventory or craft it.
     */
    public void craftOrGetItem(PatternSlotPacket packetPatternSlot) {
        var what = packetPatternSlot.what;
        if (what.isEmpty() || this.monitor == null || !isPowered()) {
            return;
        }

        InternalInventory inv = new CarriedItemInventory(this);
        var playerInv = new PlayerInternalInventory(getPlayerInventory());

        if (packetPatternSlot.intoPlayerInv) {
            inv = playerInv;
        }

        // If the target inv hold at least 1 of the crafted/extracted item, don't bother
        if (!inv.simulateAdd(what).isEmpty()) {
            return;
        }

        // Clamp the amount to a reasonable amount
        var itemKey = AEItemKey.of(what);
        var amount = Mth.clamp(what.getCount(), 1, what.getItem().getMaxStackSize());

        var extracted = StorageHelper.poweredExtraction(this.powerSource, this.monitor,
                itemKey, amount, this.getActionSource());
        var p = this.getPlayerInventory().player;

        if (extracted > 0) {
            what.setCount(amount);
            inv.addItems(what);
            this.broadcastChanges();
            return;
        }

        ///
        // Item was not available in network inventory, let's try to grab ingredients for crafting it ad-hoc
        ///

        var ic = new CraftingContainer(new NullMenu(), 3, 3);
        var real = new CraftingContainer(new NullMenu(), 3, 3);

        for (int x = 0; x < 9; x++) {
            ic.setItem(x, packetPatternSlot.pattern[x]);
        }

        var r = p.level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, ic, p.level)
                .orElse(null);

        if (r == null) {
            return;
        }

        var storage = this.getPatternTerminal().getInventory();
        var all = storage.getAvailableStacks();

        final ItemStack is = r.assemble(ic);

        var partitionFilter = ViewCellItem.createItemFilter(this.getViewCells());
        for (int x = 0; x < ic.getContainerSize(); x++) {
            if (!ic.getItem(x).isEmpty()) {
                var pulled = Platform.extractItemsByRecipe(this.powerSource,
                        this.getActionSource(), storage, p.level, r, is, ic, ic.getItem(x), x, all,
                        Actionable.MODULATE, partitionFilter);
                real.setItem(x, pulled);
            }
        }

        var rr = p.level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, real, p.level).orElse(null);

        if (rr == r && ItemStack.isSameItemSameTags(rr.assemble(real), is)) {
            final ResultContainer craftingResult = new ResultContainer();
            craftingResult.setRecipeUsed(rr);

            final ResultSlot sc = new ResultSlot(p, real, craftingResult, 0, 0, 0);
            sc.onTake(p, is);

            for (int x = 0; x < real.getContainerSize(); x++) {
                final ItemStack failed = playerInv.addItems(real.getItem(x));

                if (!failed.isEmpty()) {
                    p.drop(failed, false);
                }
            }

            inv.addItems(is);
            this.broadcastChanges();
        } else {
            for (int x = 0; x < real.getContainerSize(); x++) {
                final ItemStack failed = real.getItem(x);
                if (!failed.isEmpty()) {
                    this.monitor.insert(AEItemKey.of(failed), failed.getCount(), Actionable.MODULATE,
                            new MachineSource(this.getPatternTerminal()));
                }
            }
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (isServer()) {
            if (this.isCraftingMode() != this.getPatternTerminal().isCraftingRecipe()) {
                this.setCraftingMode(this.getPatternTerminal().isCraftingRecipe());
            }

            this.substitute = this.patternTerminal.isSubstitution();
            this.substituteFluids = this.patternTerminal.isFluidSubstitution();
        }
    }

    @Override
    public void onServerDataSync() {
        super.onServerDataSync();

        for (var slot : craftingGridSlots) {
            slot.setHideAmount(craftingMode);
        }

        if (this.currentRecipeCraftingMode != this.isCraftingMode()) {
            this.getAndUpdateOutput();
        }
    }

    @Override
    public void onSlotChange(final Slot s) {
        if (s == this.encodedPatternSlot && isServer()) {
            this.broadcastChanges();
        }

        if (s == this.craftOutputSlot && isClient()) {
            this.getAndUpdateOutput();
        }
    }

    public void clear() {
        if (isClient()) {
            sendClientAction(ACTION_CLEAR);
            return;
        }

        for (final Slot s : this.craftingGridSlots) {
            s.set(ItemStack.EMPTY);
        }

        for (final Slot s : this.processingOutputSlots) {
            s.set(ItemStack.EMPTY);
        }

        this.broadcastChanges();
        this.getAndUpdateOutput();
    }

    @Override
    public InternalInventory getCraftingMatrix() {
        return craftingGridInv;
    }

    @Override
    public boolean useRealItems() {
        return false;
    }

    public boolean isCraftingMode() {
        return this.craftingMode;
    }

    public void setCraftingMode(boolean craftingMode) {
        if (isClient()) {
            sendClientAction(ACTION_SET_CRAFT_MODE, craftingMode);
        } else {
            this.craftingMode = craftingMode;
        }
    }

    public IPatternTerminalHost getPatternTerminal() {
        return this.patternTerminal;
    }

    public boolean isSubstitute() {
        return this.substitute;
    }

    public void setSubstitute(final boolean substitute) {
        if (isClient()) {
            sendClientAction(ACTION_SET_SUBSTITUTION, substitute);
        } else {
            this.substitute = substitute;
        }
    }

    public boolean isSubstituteFluids() {
        return this.substituteFluids;
    }

    public void setSubstituteFluids(boolean substituteFluids) {
        if (isClient()) {
            sendClientAction(ACTION_SET_FLUID_SUBSTITUTION, substituteFluids);
        } else {
            this.substituteFluids = substituteFluids;
        }
    }

    public void convertItemsToFluids() {
        if (isClient()) {
            sendClientAction(ACTION_CONVERT_ITEMS_TO_FLUIDS);
            return;
        }
        if (!getPatternTerminal().isCraftingRecipe()) {
            for (var slot : this.craftingGridSlots) {
                convertItemToFluid(slot);
            }
            for (var slot : this.processingOutputSlots) {
                convertItemToFluid(slot);
            }
        }
    }

    /**
     * @return True, if any slot can be converted from item->fluid.
     */
    public boolean canConvertItemsToFluids() {
        if (isCraftingMode()) {
            return false;
        }

        for (var slot : this.craftingGridSlots) {
            if (canConvertItemToFluid(slot)) {
                return true;
            }
        }
        for (var slot : this.processingOutputSlots) {
            if (canConvertItemToFluid(slot)) {
                return true;
            }
        }

        return false;
    }

    private static void convertItemToFluid(Slot slot) {
        var fluidStack = FluidContainerHelper.getContainedStack(slot.getItem());
        if (fluidStack != null) {
            slot.set(GenericStack.wrapInItemStack(fluidStack));
        }
    }

    public boolean canConvertItemToFluid(Slot slot) {
        return FluidContainerHelper.getContainedStack(slot.getItem()) != null;
    }

    public void setProcessingResult(ItemStack resultItem) {
        for (int i = 0; i < processingOutputSlots.length; i++) {
            if (i == 0) {
                processingOutputSlots[i].set(resultItem);
            } else {
                processingOutputSlots[i].set(ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected ItemStack transferStackToMenu(ItemStack input) {
        // try refilling the blank pattern slot
        if (blankPatternSlot.mayPlace(input)) {
            input = blankPatternSlot.safeInsert(input);
            if (input.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        // try refilling the encoded pattern slot
        if (encodedPatternSlot.mayPlace(input)) {
            input = encodedPatternSlot.safeInsert(input);
            if (input.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return super.transferStackToMenu(input);
    }

    public FakeCraftingMatrixSlot[] getCraftingGridSlots() {
        return craftingGridSlots;
    }

    public OptionalFakeSlot[] getProcessingOutputSlots() {
        return processingOutputSlots;
    }
}
