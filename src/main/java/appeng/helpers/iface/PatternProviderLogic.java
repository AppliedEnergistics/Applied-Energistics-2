/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.helpers.iface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.util.IConfigManager;
import appeng.core.settings.TickRates;
import appeng.helpers.ICustomNameObject;
import appeng.me.helpers.MachineSource;
import appeng.util.ConfigManager;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;

/**
 * Shared code between the pattern provider block and part.
 */
public class PatternProviderLogic implements InternalInventoryHost, ICraftingProvider {

    public static final int NUMBER_OF_PATTERN_SLOTS = 9;

    private final PatternProviderLogicHost host;
    private final IManagedGridNode mainNode;
    private final IActionSource actionSource;
    private final ConfigManager configManager = new ConfigManager(this::saveChanges);
    private int priority;

    // Pattern storing logic
    private final AppEngInternalInventory patternInventory = new AppEngInternalInventory(this, NUMBER_OF_PATTERN_SLOTS);
    private final List<IPatternDetails> patterns = new ArrayList<>();
    /**
     * Keeps track of the inputs of all the patterns. When blocking mode is enabled, if any of these is contained in the
     * target, the pattern won't be pushed. Always contains keys with the secondary component dropped.
     */
    private final Set<AEKey> patternInputs = new HashSet<>();
    // Pattern sending logic
    private final List<GenericStack> sendList = new ArrayList<>();
    private Direction sendDirection;
    // Stack returning logic
    private final PatternProviderReturnInventory returnInv;

    public PatternProviderLogic(IManagedGridNode mainNode, PatternProviderLogicHost host) {
        this.host = host;
        this.mainNode = mainNode
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, new Ticker())
                .addService(ICraftingProvider.class, this);
        this.actionSource = new MachineSource(mainNode::getNode);

        this.configManager.registerSetting(Settings.BLOCKING_MODE, YesNo.NO);
        this.configManager.registerSetting(Settings.PATTERN_ACCESS_TERMINAL, YesNo.YES);

        this.returnInv = new PatternProviderReturnInventory(() -> {
            this.mainNode.ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
        });
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        this.host.saveChanges();

        ICraftingProvider.requestUpdate(mainNode);
    }

    public void writeToNBT(CompoundTag tag) {
        this.configManager.writeToNBT(tag);
        this.patternInventory.writeToNBT(tag, "patterns");
        tag.putInt("priority", this.priority);

        ListTag sendListTag = new ListTag();
        for (var toSend : sendList) {
            sendListTag.add(GenericStack.writeTag(toSend));
        }
        tag.put("sendList", sendListTag);
        if (sendDirection != null) {
            tag.putByte("sendDirection", (byte) sendDirection.get3DDataValue());
        }

        tag.put("returnInv", this.returnInv.writeToTag());
    }

    public void readFromNBT(CompoundTag tag) {
        this.configManager.readFromNBT(tag);
        this.patternInventory.readFromNBT(tag, "patterns");
        this.priority = tag.getInt("priority");

        ListTag sendListTag = tag.getList("sendList", Tag.TAG_COMPOUND);
        for (int i = 0; i < sendListTag.size(); ++i) {
            var stack = GenericStack.readTag(sendListTag.getCompound(i));
            if (stack != null) {
                this.addToSendList(stack.what(), stack.amount());
            }
        }
        if (tag.contains("sendDirection")) {
            sendDirection = Direction.from3DDataValue(tag.getByte("sendDirection"));
        }

        this.returnInv.readFromTag(tag.getList("returnInv", Tag.TAG_COMPOUND));
    }

    public IConfigManager getConfigManager() {
        return this.configManager;
    }

    @Override
    public void saveChanges() {
        this.host.saveChanges();
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        this.saveChanges();
        this.updatePatterns();
    }

    @Override
    public boolean isClientSide() {
        Level level = this.host.getBlockEntity().getLevel();
        return level == null || level.isClientSide();
    }

    public void updatePatterns() {
        patterns.clear();
        patternInputs.clear();

        for (var stack : this.patternInventory) {
            var details = PatternDetailsHelper.decodePattern(stack, this.host.getBlockEntity().getLevel());

            if (details != null) {
                patterns.add(details);

                for (var iinput : details.getInputs()) {
                    for (var inputCandidate : iinput.getPossibleInputs()) {
                        patternInputs.add(inputCandidate.what().dropSecondary());
                    }
                }
            }
        }

        ICraftingProvider.requestUpdate(mainNode);
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return this.patterns;
    }

    @Override
    public int getPatternPriority() {
        return this.priority;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!sendList.isEmpty() || !this.mainNode.isActive() || !this.patterns.contains(patternDetails)) {
            return false;
        }

        var be = host.getBlockEntity();
        var level = be.getLevel();

        for (var direction : host.getTargets()) {
            var adjPos = be.getBlockPos().relative(direction);
            var adjBe = level.getBlockEntity(adjPos);
            var adjBeSide = direction.getOpposite();

            if (adjBe instanceof PatternProviderLogicHost adjHost) {
                if (adjHost.getLogic().sameGrid(this.mainNode.getGrid())) {
                    continue;
                }
            }

            var craftingMachine = ICraftingMachine.of(adjBe, adjBeSide);
            if (craftingMachine != null && craftingMachine.acceptsPlans()) {
                if (craftingMachine.pushPattern(patternDetails, inputHolder, adjBeSide)) {
                    return true;
                }
                continue;
            }

            var adapter = PatternProviderTarget.get(level, adjPos, adjBe, adjBeSide, this.actionSource);
            if (adapter == null)
                continue;

            if (this.isBlocking() && adapter.containsPatternInput(this.patternInputs)) {
                continue;
            }

            if (this.adapterAcceptsAll(adapter, inputHolder)) {
                for (var inputList : inputHolder) {
                    for (var input : inputList) {
                        var what = input.getKey();
                        long amount = input.getLongValue();
                        var inserted = adapter.insert(what, amount, Actionable.MODULATE);
                        if (inserted < amount) {
                            this.addToSendList(what, amount - inserted);
                        }
                    }
                }
                this.sendDirection = direction;
                this.sendStacksOut();
                return true;
            }
        }

        return false;
    }

    private boolean sameGrid(@Nullable IGrid grid) {
        return grid != null && grid == this.mainNode.getGrid();
    }

    public boolean isBlocking() {
        return this.configManager.getSetting(Settings.BLOCKING_MODE) == YesNo.YES;
    }

    private boolean adapterAcceptsAll(PatternProviderTarget target, KeyCounter[] inputHolder) {
        for (var inputList : inputHolder) {
            for (var input : inputList) {
                var inserted = target.insert(input.getKey(), input.getLongValue(), Actionable.SIMULATE);
                if (inserted == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addToSendList(AEKey what, long amount) {
        if (amount > 0) {
            this.sendList.add(new GenericStack(what, amount));

            this.mainNode.ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
        }
    }

    private boolean sendStacksOut() {
        if (sendDirection == null) {
            if (!sendList.isEmpty()) {
                throw new IllegalStateException("Invalid pattern provider state, this is a bug.");
            }
            return false;
        }

        var be = this.host.getBlockEntity();
        var level = be.getLevel();
        var adjPos = be.getBlockPos().relative(sendDirection);
        var adjBe = level.getBlockEntity(adjPos);
        var adapter = PatternProviderTarget.get(level, adjPos, adjBe, sendDirection.getOpposite(), actionSource);

        if (adapter == null) {
            return false;
        }

        boolean didSomething = false;

        for (var it = sendList.listIterator(); it.hasNext();) {
            var stack = it.next();
            var what = stack.what();
            long amount = stack.amount();

            var inserted = adapter.insert(what, amount, Actionable.MODULATE);
            if (inserted >= amount) {
                it.remove();
                didSomething = true;
            } else if (inserted > 0) {
                it.set(new GenericStack(what, amount - inserted));
                didSomething = true;
            }
        }

        if (sendList.isEmpty()) {
            sendDirection = null;
        }

        return didSomething;
    }

    @Override
    public boolean isBusy() {
        return !sendList.isEmpty();
    }

    private boolean hasWorkToDo() {
        return !sendList.isEmpty() || !returnInv.isEmpty();
    }

    private boolean doWork() {
        // Note: bitwise OR to avoid short-circuiting.
        return returnInv.injectIntoNetwork(mainNode.getGrid().getStorageService().getInventory(), actionSource)
                | sendStacksOut();
    }

    public InternalInventory getPatternInv() {
        return this.patternInventory;
    }

    public void onMainNodeStateChanged() {
        ICraftingProvider.requestUpdate(this.mainNode);

        if (this.mainNode.hasGridBooted()) {
            this.mainNode.ifPresent((grid, node) -> {
                grid.getTickManager().alertDevice(node);
            });
        }
    }

    public void addDrops(List<ItemStack> drops) {
        for (var stack : this.patternInventory) {
            drops.add(stack);
        }

        for (var stack : this.sendList) {
            stack.what().addDrops(stack.amount(), drops, this.host.getBlockEntity().getLevel(),
                    this.host.getBlockEntity().getBlockPos());
        }

        this.returnInv.addDrops(drops, this.host.getBlockEntity().getLevel(), this.host.getBlockEntity().getBlockPos());
    }

    public PatternProviderReturnInventory getReturnInv() {
        return this.returnInv;
    }

    private class Ticker implements IGridTickable {

        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(TickRates.Interface, !hasWorkToDo(), true);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            if (!mainNode.isActive()) {
                return TickRateModulation.SLEEP;
            }
            boolean couldDoWork = doWork();
            return hasWorkToDo() ? couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER
                    : TickRateModulation.SLEEP;
        }
    }

    // TODO: get rid of this awful code
    private static final Collection<Block> BAD_BLOCKS = new HashSet<>(100);

    /**
     * @return Gets the name used to show this pattern provider in the
     *         {@link appeng.menu.implementations.PatternAccessTermMenu}.
     */
    public Component getTermName() {
        final BlockEntity host = this.host.getBlockEntity();
        final Level hostWorld = host.getLevel();

        if (((ICustomNameObject) this.host).hasCustomInventoryName()) {
            return ((ICustomNameObject) this.host).getCustomInventoryName();
        }

        for (var direction : this.host.getTargets()) {
            final BlockPos targ = host.getBlockPos().relative(direction);
            final BlockEntity directedBlockEntity = hostWorld.getBlockEntity(targ);

            if (directedBlockEntity == null) {
                continue;
            }

            if (directedBlockEntity instanceof PatternProviderLogicHost interfaceHost) {
                if (interfaceHost.getLogic().sameGrid(this.mainNode.getGrid())) {
                    continue;
                }
            }

            var craftingMachine = ICraftingMachine.of(directedBlockEntity, direction.getOpposite());
            if (craftingMachine != null) {
                var displayName = craftingMachine.getDisplayName();
                if (displayName.isPresent()) {
                    return displayName.get();
                }
            }

            var adaptor = InternalInventory.wrapExternal(directedBlockEntity, direction.getOpposite());
            if (adaptor != null) {
                if (!adaptor.mayAllowTransfer()) {
                    continue;
                }

                final BlockState directedBlockState = hostWorld.getBlockState(targ);
                final Block directedBlock = directedBlockState.getBlock();
                ItemStack what = new ItemStack(directedBlock, 1);
                try {
                    Vec3 from = new Vec3(host.getBlockPos().getX() + 0.5, host.getBlockPos().getY() + 0.5,
                            host.getBlockPos().getZ() + 0.5);
                    from = from.add(direction.getStepX() * 0.501, direction.getStepY() * 0.501,
                            direction.getStepZ() * 0.501);
                    final Vec3 to = from.add(direction.getStepX(), direction.getStepY(),
                            direction.getStepZ());
                    final BlockHitResult hit = null;// hostWorld.rayTraceBlocks( from, to ); //FIXME:
                    // https://github.com/MinecraftForge/MinecraftForge/pull/6708
                    if (hit != null && !BAD_BLOCKS.contains(directedBlock)
                            && hit.getBlockPos().equals(directedBlockEntity.getBlockPos())) {
                        final ItemStack g = directedBlock.getCloneItemStack(directedBlockState, hit, hostWorld,
                                directedBlockEntity.getBlockPos(), null);
                        if (!g.isEmpty()) {
                            what = g;
                        }
                    }
                } catch (Throwable t) {
                    BAD_BLOCKS.add(directedBlock); // nope!
                }

                if (what.getItem() != Items.AIR) {
                    return Component.translatable(what.getDescriptionId());
                }

                final Item item = Item.byBlock(directedBlock);
                if (item == Items.AIR) {
                    return Component.translatable(directedBlock.getDescriptionId());
                }
            }
        }

        return Component.literal("Nothing");
    }

    public long getSortValue() {
        final BlockEntity te = this.host.getBlockEntity();
        return te.getBlockPos().getZ() << 24 ^ te.getBlockPos().getX() << 8 ^ te.getBlockPos().getY();
    }

    public <T> LazyOptional<T> getCapability(Capability<T> capability) {
        return this.returnInv.getCapability(capability);
    }

    @Nullable
    public IGrid getGrid() {
        return mainNode.getGrid();
    }
}
