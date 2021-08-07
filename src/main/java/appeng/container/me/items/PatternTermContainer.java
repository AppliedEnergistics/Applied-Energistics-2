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

package appeng.container.me.items;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.crafting.ICraftingHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.container.SlotSemantic;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerTypeBuilder;
import appeng.container.slot.FakeCraftingMatrixSlot;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalFakeSlot;
import appeng.container.slot.PatternOutputsSlot;
import appeng.container.slot.PatternTermSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.core.Api;
import appeng.core.definitions.AEItems;
import appeng.core.sync.packets.PatternSlotPacket;
import appeng.helpers.IContainerCraftingPacket;
import appeng.items.storage.ViewCellItem;
import appeng.me.helpers.MachineSource;
import appeng.parts.reporting.PatternTerminalPart;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.WrapperCursorItemHandler;
import appeng.util.item.AEItemStack;

/**
 * @see appeng.client.gui.me.items.PatternTermScreen
 */
public class PatternTermContainer extends ItemTerminalContainer
        implements IOptionalSlotHost, IContainerCraftingPacket {

    public static MenuType<PatternTermContainer> TYPE = ContainerTypeBuilder
            .create(PatternTermContainer::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("patternterm");

    private final PatternTerminalPart patternTerminal;
    private final IItemHandler craftingGridInv;
    private final FakeCraftingMatrixSlot[] craftingGridSlots = new FakeCraftingMatrixSlot[9];
    private final OptionalFakeSlot[] processingOutputSlots = new OptionalFakeSlot[3];
    private final PatternTermSlot craftOutputSlot;
    private final RestrictedInputSlot blankPatternSlot;
    private final RestrictedInputSlot encodedPatternSlot;
    private final ICraftingHelper craftingHelper = Api.INSTANCE.crafting();

    private CraftingRecipe currentRecipe;
    private boolean currentRecipeCraftingMode;

    @GuiSync(97)
    public boolean craftingMode = true;
    @GuiSync(96)
    public boolean substitute = false;

    public PatternTermContainer(int id, final Inventory ip, final ITerminalHost monitorable) {
        super(TYPE, id, ip, monitorable, false);
        this.patternTerminal = (PatternTerminalPart) monitorable;

        final IItemHandler patternInv = this.getPatternTerminal().getInventoryByName("pattern");
        final IItemHandler output = this.getPatternTerminal().getInventoryByName("output");

        // Create the 3x3 crafting input grid, which is used for both processing and crafting mode
        this.craftingGridInv = this.getPatternTerminal().getInventoryByName("crafting");
        for (int i = 0; i < 9; i++) {
            this.addSlot(this.craftingGridSlots[i] = new FakeCraftingMatrixSlot(this.craftingGridInv, i),
                    SlotSemantic.CRAFTING_GRID);
        }

        // Create the output slot used for crafting mode patterns
        this.addSlot(this.craftOutputSlot = new PatternTermSlot(ip.player, this.getActionSource(), this.powerSource,
                monitorable, this.craftingGridInv, patternInv, this, 2, this), SlotSemantic.CRAFTING_RESULT);
        this.craftOutputSlot.setIcon(null);

        // Create slots for the outputs of processing-mode patterns
        for (int i = 0; i < 3; i++) {
            this.addSlot(this.processingOutputSlots[i] = new PatternOutputsSlot(output, this, i, 1),
                    SlotSemantic.PROCESSING_RESULT);
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

        this.createPlayerInventorySlots(ip);
    }

    @Override
    public void setItem(int slotID, int stateId, ItemStack stack) {
        super.setItem(slotID, stateId, stack);
        this.getAndUpdateOutput();
    }

    private ItemStack getAndUpdateOutput() {
        final Level level = this.getPlayerInventory().player.level;
        final CraftingContainer ic = new CraftingContainer(this, 3, 3);

        for (int x = 0; x < ic.getContainerSize(); x++) {
            ic.setItem(x, this.craftingGridInv.getStackInSlot(x));
        }

        if (this.currentRecipe == null || !this.currentRecipe.matches(ic, level)) {
            this.currentRecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, ic, level).orElse(null);
            this.currentRecipeCraftingMode = this.craftingMode;
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

    public void encode() {
        ItemStack output = this.encodedPatternSlot.getItem();

        final ItemStack[] in = this.getInputs();
        final ItemStack[] out = this.getOutputs();

        // if there is no input, this would be silly.
        if (in == null || out == null || isCraftingMode() && currentRecipe == null) {
            return;
        }

        // first check the output slots, should either be null, or a pattern
        if (!output.isEmpty() && !craftingHelper.isEncodedPattern(output)) {
            return;
        } // if nothing is there we should snag a new pattern.
        else if (output.isEmpty()) {
            output = this.blankPatternSlot.getItem();
            if (output.isEmpty() || !isPattern(output)) {
                return; // no blanks.
            }

            // remove one, and clear the input slot.
            output.setCount(output.getCount() - 1);
            if (output.getCount() == 0) {
                this.blankPatternSlot.set(ItemStack.EMPTY);
            }

            // let the crafting helper create a new encoded pattern
            output = null;
        }

        if (this.isCraftingMode()) {
            output = craftingHelper.encodeCraftingPattern(output, this.currentRecipe, in, out[0], isSubstitute());
        } else {
            output = craftingHelper.encodeProcessingPattern(output, in, out);
        }
        this.encodedPatternSlot.set(output);

    }

    private ItemStack[] getInputs() {
        final ItemStack[] input = new ItemStack[9];
        boolean hasValue = false;

        for (int x = 0; x < this.craftingGridSlots.length; x++) {
            input[x] = this.craftingGridSlots[x].getItem();
            if (!input[x].isEmpty()) {
                hasValue = true;
            }
        }

        if (hasValue) {
            return input;
        }

        return null;
    }

    private ItemStack[] getOutputs() {
        if (this.isCraftingMode()) {
            final ItemStack out = this.getAndUpdateOutput();

            if (!out.isEmpty() && out.getCount() > 0) {
                return new ItemStack[] { out };
            }
        } else {
            boolean hasValue = false;
            final ItemStack[] list = new ItemStack[3];

            for (int i = 0; i < this.processingOutputSlots.length; i++) {
                final ItemStack out = this.processingOutputSlots[i].getItem();
                list[i] = out;
                if (!out.isEmpty()) {
                    hasValue = true;
                }
            }
            if (hasValue) {
                return list;
            }
        }

        return null;
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

    public void craftOrGetItem(final PatternSlotPacket packetPatternSlot) {
        if (packetPatternSlot.slotItem != null && this.monitor != null /*
                                                                        * TODO should this check powered / powerSource?
                                                                        */) {
            final IAEItemStack out = packetPatternSlot.slotItem.copy();
            InventoryAdaptor inv = new AdaptorItemHandler(new WrapperCursorItemHandler(this));
            final InventoryAdaptor playerInv = InventoryAdaptor.getAdaptor(this.getPlayerInventory().player);

            if (packetPatternSlot.shift) {
                inv = playerInv;
            }

            if (!inv.simulateAdd(out.createItemStack()).isEmpty()) {
                return;
            }

            final IAEItemStack extracted = Platform.poweredExtraction(this.powerSource, this.monitor,
                    out, this.getActionSource());
            final Player p = this.getPlayerInventory().player;

            if (extracted != null) {
                inv.addItems(extracted.createItemStack());
                this.broadcastChanges();
                return;
            }

            final CraftingContainer ic = new CraftingContainer(new ContainerNull(), 3, 3);
            final CraftingContainer real = new CraftingContainer(new ContainerNull(), 3, 3);

            for (int x = 0; x < 9; x++) {
                ic.setItem(x, packetPatternSlot.pattern[x] == null ? ItemStack.EMPTY
                        : packetPatternSlot.pattern[x].createItemStack());
            }

            final Recipe<CraftingContainer> r = p.level.getRecipeManager()
                    .getRecipeFor(RecipeType.CRAFTING, ic, p.level)
                    .orElse(null);

            if (r == null) {
                return;
            }

            final IMEMonitor<IAEItemStack> storage = this.getPatternTerminal()
                    .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
            final IItemList<IAEItemStack> all = storage.getStorageList();

            final ItemStack is = r.assemble(ic);

            for (int x = 0; x < ic.getContainerSize(); x++) {
                if (!ic.getItem(x).isEmpty()) {
                    final ItemStack pulled = Platform.extractItemsByRecipe(this.powerSource,
                            this.getActionSource(), storage, p.level, r, is, ic, ic.getItem(x), x, all,
                            Actionable.MODULATE, ViewCellItem.createFilter(this.getViewCells()));
                    real.setItem(x, pulled);
                }
            }

            final Recipe<CraftingContainer> rr = p.level.getRecipeManager()
                    .getRecipeFor(RecipeType.CRAFTING, real, p.level).orElse(null);

            if (rr == r && Platform.itemComparisons().isSameItem(rr.assemble(real), is)) {
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
                        this.monitor.injectItems(AEItemStack.fromItemStack(failed), Actionable.MODULATE,
                                new MachineSource(this.getPatternTerminal()));
                    }
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
        }
    }

    @Override
    public void onServerDataSync() {
        super.onServerDataSync();

        if (this.currentRecipeCraftingMode != this.craftingMode) {
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
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("player")) {
            return new PlayerInvWrapper(this.getPlayerInventory());
        }
        return this.getPatternTerminal().getInventoryByName(name);
    }

    @Override
    public boolean useRealItems() {
        return false;
    }

    public boolean isCraftingMode() {
        return this.craftingMode;
    }

    private void setCraftingMode(final boolean craftingMode) {
        this.craftingMode = craftingMode;
    }

    public PatternTerminalPart getPatternTerminal() {
        return this.patternTerminal;
    }

    private boolean isSubstitute() {
        return this.substitute;
    }

    public void setSubstitute(final boolean substitute) {
        this.substitute = substitute;
    }

    public FakeCraftingMatrixSlot[] getCraftingGridSlots() {
        return craftingGridSlots;
    }

    public OptionalFakeSlot[] getProcessingOutputSlots() {
        return processingOutputSlots;
    }

    public PatternTermSlot getCraftOutputSlot() {
        return craftOutputSlot;
    }

}
