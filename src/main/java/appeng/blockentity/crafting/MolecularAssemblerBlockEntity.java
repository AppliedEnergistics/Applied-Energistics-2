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

package appeng.blockentity.crafting;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.client.render.crafting.AssemblerAnimationStatus;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.network.TargetPoint;
import appeng.core.sync.packets.AssemblerAnimationPacket;
import appeng.crafting.CraftingEvent;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.CraftingPatternItem;
import appeng.menu.AutoCraftingMenu;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

public class MolecularAssemblerBlockEntity extends AENetworkInvBlockEntity
        implements IUpgradeableObject, IGridTickable, ICraftingMachine, IPowerChannelState {

    /**
     * Identifies the sub-inventory used by molecular assemblers to store the input items for the crafting process.
     */
    public static final ResourceLocation INV_MAIN = AppEng.makeId("molecular_assembler");

    private final CraftingContainer craftingInv;
    private final AppEngInternalInventory gridInv = new AppEngInternalInventory(this, 9 + 1, 1);
    private final AppEngInternalInventory patternInv = new AppEngInternalInventory(this, 1, 1);
    private final InternalInventory gridInvExt = new FilteredInternalInventory(this.gridInv, new CraftingGridFilter());
    private final InternalInventory internalInv = new CombinedInternalInventory(this.gridInv, this.patternInv);
    private final IUpgradeInventory upgrades;
    private boolean isPowered = false;
    private Direction pushDirection = null;
    private ItemStack myPattern = ItemStack.EMPTY;
    private AECraftingPattern myPlan = null;
    private double progress = 0;
    private boolean isAwake = false;
    private boolean forcePlan = false;
    private boolean reboot = true;

    @Environment(EnvType.CLIENT)
    private AssemblerAnimationStatus animationStatus;

    public MolecularAssemblerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);

        this.getMainNode()
                .setIdlePowerUsage(0.0)
                .addService(IGridTickable.class, this);
        this.upgrades = UpgradeInventories.forMachine(AEBlocks.MOLECULAR_ASSEMBLER, getUpgradeSlots(),
                this::saveChanges);
        this.craftingInv = new CraftingContainer(new AutoCraftingMenu(), 3, 3);

    }

    private int getUpgradeSlots() {
        return 5;
    }

    @Override
    public Optional<Component> getDisplayName() {
        if (hasCustomInventoryName()) {
            return Optional.of(getCustomInventoryName());
        } else {
            return Optional.of(getItemFromBlockEntity().getDescription());
        }
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] table,
            Direction where) {
        if (this.myPattern.isEmpty()) {
            boolean isEmpty = this.gridInv.isEmpty() && this.patternInv.isEmpty();

            // Only accept our own crafting patterns!
            if (isEmpty && patternDetails instanceof AECraftingPattern pattern) {
                // We only support fluid and item stacks

                this.forcePlan = true;
                this.myPlan = pattern;
                this.pushDirection = where;

                this.fillGrid(table, pattern);

                this.updateSleepiness();
                this.saveChanges();
                return true;
            }
        }
        return false;
    }

    private void fillGrid(KeyCounter[] table, AECraftingPattern adapter) {
        for (int sparseIndex = 0; sparseIndex < 9; ++sparseIndex) {
            int inputId = adapter.getCompressedIndexFromSparse(sparseIndex);
            if (inputId != -1) {
                var list = table[inputId];

                // Try substituting with a fluid, if allowed and available
                var validFluid = myPlan.getValidFluid(sparseIndex);
                if (validFluid != null) {
                    var validFluidKey = validFluid.what();
                    var amount = list.get(validFluidKey);
                    int requiredAmount = (int) validFluid.amount();
                    if (amount >= requiredAmount) {
                        this.gridInv.setItemDirect(sparseIndex,
                                GenericStack.wrapInItemStack(validFluidKey, requiredAmount));
                        list.remove(validFluidKey, requiredAmount);
                        continue;
                    }
                }

                // Try falling back to whatever is available
                for (var entry : list) {
                    if (entry.getLongValue() > 0 && entry.getKey() instanceof AEItemKey itemKey) {
                        this.gridInv.setItemDirect(sparseIndex, itemKey.toStack());
                        list.remove(itemKey, 1);
                        break;
                    }
                }
            }
        }

        // Sanity check
        for (KeyCounter list : table) {
            list.removeZeros();
            if (!list.isEmpty()) {
                throw new RuntimeException("Could not fill grid with some items, including " + list.iterator().next());
            }
        }
    }

    private void updateSleepiness() {
        final boolean wasEnabled = this.isAwake;
        this.isAwake = this.myPlan != null && this.hasMats() || this.canPush();
        if (wasEnabled != this.isAwake) {
            getMainNode().ifPresent((grid, node) -> {
                if (this.isAwake) {
                    grid.getTickManager().wakeDevice(node);
                } else {
                    grid.getTickManager().sleepDevice(node);
                }
            });
        }
    }

    private boolean canPush() {
        return !this.gridInv.getStackInSlot(9).isEmpty();
    }

    private boolean hasMats() {
        if (this.myPlan == null) {
            return false;
        }

        for (int x = 0; x < this.craftingInv.getContainerSize(); x++) {
            this.craftingInv.setItem(x, this.gridInv.getStackInSlot(x));
        }

        return !this.myPlan.getOutput(this.craftingInv, this.getLevel()).isEmpty();
    }

    @Override
    public boolean acceptsPlans() {
        return this.patternInv.isEmpty();
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        final boolean c = super.readFromStream(data);
        final boolean oldPower = this.isPowered;
        this.isPowered = data.readBoolean();
        return this.isPowered != oldPower || c;
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.isPowered);
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        if (this.forcePlan) {
            // If the plan is null it means the pattern previously loaded from NBT hasn't been decoded yet
            var pattern = myPlan != null ? myPlan.getDefinition().toStack() : myPattern;
            if (!pattern.isEmpty()) {
                var compound = new CompoundTag();
                pattern.save(compound);
                data.put("myPlan", compound);
                data.putInt("pushDirection", this.pushDirection.ordinal());
            }
        }

        this.upgrades.writeToNBT(data, "upgrades");
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);

        // Reset current state back to defaults
        this.forcePlan = false;
        this.myPattern = ItemStack.EMPTY;
        this.myPlan = null;

        if (data.contains("myPlan")) {
            var pattern = ItemStack.of(data.getCompound("myPlan"));
            if (!pattern.isEmpty()) {
                this.forcePlan = true;
                this.myPattern = pattern;
                this.pushDirection = Direction.values()[data.getInt("pushDirection")];
            }
        }

        this.upgrades.readFromNBT(data, "upgrades");
        this.recalculatePlan();
    }

    private void recalculatePlan() {
        this.reboot = true;

        if (this.forcePlan) {
            // If we're in forced mode, and myPattern is not empty, but the plan is null,
            // this indicates that we received an encoded pattern from NBT data, but
            // didn't have a chance to decode it yet
            if (getLevel() != null && myPlan == null) {
                if (!myPattern.isEmpty() && myPattern.getItem() instanceof CraftingPatternItem patternItem) {
                    this.myPlan = patternItem.decode(myPattern, getLevel(), false);
                }

                // Reset myPattern, so it will accept another job once this one finishes
                this.myPattern = ItemStack.EMPTY;

                // If the plan is still null, reset back to non-forced mode
                if (myPlan == null) {
                    AELog.warn("Unable to restore auto-crafting pattern after load: %s", myPattern.getTag());
                    this.forcePlan = false;
                }
            }

            return;
        }

        final ItemStack is = this.patternInv.getStackInSlot(0);

        if (!is.isEmpty() && is.getItem() instanceof CraftingPatternItem patternItem) {
            if (!ItemStack.isSame(is, this.myPattern)) {
                final Level level = this.getLevel();
                var details = patternItem.decode(is, level, false);

                if (details != null) {
                    this.progress = 0;
                    this.myPattern = is;
                    this.myPlan = details;
                }
            }
        } else {
            this.progress = 0;
            this.forcePlan = false;
            this.myPlan = null;
            this.myPattern = ItemStack.EMPTY;
            this.pushDirection = null;
        }

        this.updateSleepiness();
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.UPGRADES)) {
            return this.upgrades;
        } else if (id.equals(INV_MAIN)) {
            return this.internalInv;
        }

        return super.getSubInventory(id);
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.internalInv;
    }

    @Override
    public InternalInventory getExposedInventoryForSide(Direction side) {
        return this.gridInvExt;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        if (inv == this.gridInv || inv == this.patternInv) {
            this.recalculatePlan();
        }
    }

    public int getCraftingProgress() {
        return (int) this.progress;
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);

        for (var upgrade : upgrades) {
            drops.add(upgrade);
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        this.recalculatePlan();
        this.updateSleepiness();
        return new TickingRequest(1, 1, !this.isAwake, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!this.gridInv.getStackInSlot(9).isEmpty()) {
            this.pushOut(this.gridInv.getStackInSlot(9));

            // did it eject?
            if (this.gridInv.getStackInSlot(9).isEmpty()) {
                this.saveChanges();
            }

            this.ejectHeldItems();
            this.updateSleepiness();
            this.progress = 0;
            return this.isAwake ? TickRateModulation.IDLE : TickRateModulation.SLEEP;
        }

        if (this.myPlan == null) {
            this.updateSleepiness();
            return TickRateModulation.SLEEP;
        }

        if (this.reboot) {
            ticksSinceLastCall = 1;
        }

        if (!this.isAwake) {
            return TickRateModulation.SLEEP;
        }

        this.reboot = false;
        int speed = 10;
        switch (this.upgrades.getInstalledUpgrades(AEItems.SPEED_CARD)) {
            case 0 -> this.progress += this.userPower(ticksSinceLastCall, speed = 10, 1.0);
            case 1 -> this.progress += this.userPower(ticksSinceLastCall, speed = 13, 1.3);
            case 2 -> this.progress += this.userPower(ticksSinceLastCall, speed = 17, 1.7);
            case 3 -> this.progress += this.userPower(ticksSinceLastCall, speed = 20, 2.0);
            case 4 -> this.progress += this.userPower(ticksSinceLastCall, speed = 25, 2.5);
            case 5 -> this.progress += this.userPower(ticksSinceLastCall, speed = 50, 5.0);
        }

        if (this.progress >= 100) {
            for (int x = 0; x < this.craftingInv.getContainerSize(); x++) {
                this.craftingInv.setItem(x, this.gridInv.getStackInSlot(x));
            }

            this.progress = 0;
            final ItemStack output = this.myPlan.getOutput(this.craftingInv, this.getLevel());
            if (!output.isEmpty()) {
                CraftingEvent.fireAutoCraftingEvent(getLevel(), this.myPlan, output, this.craftingInv);

                // pushOut might reset the plan back to null, so get the remaining items before
                var craftingRemainders = this.myPlan.getRemainingItems(this.craftingInv);

                this.pushOut(output.copy());

                for (int x = 0; x < this.craftingInv.getContainerSize(); x++) {
                    this.gridInv.setItemDirect(x, craftingRemainders.get(x));
                }

                if (this.patternInv.isEmpty()) {
                    this.forcePlan = false;
                    this.myPlan = null;
                    this.pushDirection = null;
                }

                this.ejectHeldItems();

                var item = AEItemKey.of(output);
                if (item != null) {
                    final TargetPoint where = new TargetPoint(this.worldPosition.getX(), this.worldPosition.getY(),
                            this.worldPosition.getZ(), 32,
                            this.level);
                    NetworkHandler.instance()
                            .sendToAllAround(new AssemblerAnimationPacket(this.worldPosition, (byte) speed, item),
                                    where);
                }

                this.saveChanges();
                this.updateSleepiness();
                return this.isAwake ? TickRateModulation.IDLE : TickRateModulation.SLEEP;
            }
        }

        return TickRateModulation.FASTER;
    }

    private void ejectHeldItems() {
        if (this.gridInv.getStackInSlot(9).isEmpty()) {
            for (int x = 0; x < 9; x++) {
                final ItemStack is = this.gridInv.getStackInSlot(x);
                if (!is.isEmpty()
                        && (this.myPlan == null || !this.myPlan.isItemValid(x, AEItemKey.of(is), this.level))) {
                    this.gridInv.setItemDirect(9, is);
                    this.gridInv.setItemDirect(x, ItemStack.EMPTY);
                    this.saveChanges();
                    return;
                }
            }
        }
    }

    private int userPower(int ticksPassed, int bonusValue, double acceleratorTax) {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            return (int) (grid.getEnergyService().extractAEPower(ticksPassed * bonusValue * acceleratorTax,
                    Actionable.MODULATE, PowerMultiplier.CONFIG) / acceleratorTax);
        } else {
            return 0;
        }
    }

    private void pushOut(ItemStack output) {
        if (this.pushDirection == null) {
            for (Direction d : Direction.values()) {
                output = this.pushTo(output, d);
            }
        } else {
            output = this.pushTo(output, this.pushDirection);
        }

        if (output.isEmpty() && this.forcePlan) {
            this.forcePlan = false;
            this.recalculatePlan();
        }

        this.gridInv.setItemDirect(9, output);
    }

    private ItemStack pushTo(ItemStack output, Direction d) {
        if (output.isEmpty()) {
            return output;
        }

        final BlockEntity te = this.getLevel().getBlockEntity(this.worldPosition.relative(d));

        if (te == null) {
            return output;
        }

        var adaptor = InternalInventory.wrapExternal(te, d.getOpposite());
        if (adaptor == null) {
            return output;
        }

        final int size = output.getCount();
        output = adaptor.addItems(output);
        final int newSize = output.isEmpty() ? 0 : output.getCount();

        if (size != newSize) {
            this.saveChanges();
        }

        return output;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            boolean newState = false;

            var grid = getMainNode().getGrid();
            if (grid != null) {
                newState = this.getMainNode().isActive() && grid.getEnergyService().extractAEPower(1,
                        Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.0001;
            }

            if (newState != this.isPowered) {
                this.isPowered = newState;
                this.markForUpdate();
            }
        }
    }

    @Override
    public boolean isPowered() {
        return this.isPowered;
    }

    @Override
    public boolean isActive() {
        return this.isPowered;
    }

    @Environment(EnvType.CLIENT)
    public void setAnimationStatus(@Nullable AssemblerAnimationStatus status) {
        this.animationStatus = status;
    }

    @Environment(EnvType.CLIENT)
    @Nullable
    public AssemblerAnimationStatus getAnimationStatus() {
        return this.animationStatus;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    @Nullable
    public AECraftingPattern getCurrentPattern() {
        if (isClientSide()) {
            var patternItem = patternInv.getStackInSlot(0);
            var pattern = PatternDetailsHelper.decodePattern(patternItem, level);
            if (pattern instanceof AECraftingPattern craftingPattern) {
                return craftingPattern;
            }
            return null;
        } else {
            return myPlan;
        }
    }

    private class CraftingGridFilter implements IAEItemFilter {
        private boolean hasPattern() {
            return MolecularAssemblerBlockEntity.this.myPlan != null
                    && !MolecularAssemblerBlockEntity.this.patternInv.isEmpty();
        }

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return slot == 9;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            if (slot >= 9) {
                return false;
            }

            if (this.hasPattern()) {
                return MolecularAssemblerBlockEntity.this.myPlan.isItemValid(slot, AEItemKey.of(stack),
                        MolecularAssemblerBlockEntity.this.getLevel());
            }
            return false;
        }
    }
}
