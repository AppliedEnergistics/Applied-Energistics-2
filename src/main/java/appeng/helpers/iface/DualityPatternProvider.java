package appeng.helpers.iface;

import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.GridCraftingPatternChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.MixedItemList;
import appeng.api.util.IConfigManager;
import appeng.core.settings.TickRates;
import appeng.crafting.execution.GenericStackHelper;
import appeng.helpers.ICustomNameObject;
import appeng.me.helpers.MachineSource;
import appeng.util.ConfigManager;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;

/**
 * Shared code between the pattern provider block and part.
 */
public class DualityPatternProvider implements InternalInventoryHost, ICraftingProvider, ICraftingMedium {

    public static final int NUMBER_OF_PATTERN_SLOTS = 9;

    private final IPatternProviderHost host;
    private final IManagedGridNode mainNode;
    private final IActionSource actionSource;
    private final ConfigManager configManager = new ConfigManager();

    // Pattern storing logic
    private final AppEngInternalInventory patternInventory = new AppEngInternalInventory(this, NUMBER_OF_PATTERN_SLOTS);
    private final List<IPatternDetails> patterns = new ArrayList<>();
    private int priority;
    // Pattern sending logic
    private final List<IAEStack> sendList = new ArrayList<>();
    private Direction sendDirection;
    // Stack returning logic
    private final PatternProviderReturnInventory returnInv;

    public DualityPatternProvider(IManagedGridNode mainNode, IPatternProviderHost host) {
        this.host = host;
        this.mainNode = mainNode
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, new Ticker())
                .addService(ICraftingProvider.class, this);
        this.actionSource = new MachineSource(mainNode::getNode);

        this.configManager.registerSetting(Settings.BLOCKING_MODE, YesNo.NO);
        this.configManager.registerSetting(Settings.INTERFACE_TERMINAL, YesNo.YES);

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
    }

    public <T> LazyOptional<T> getCapability(Capability<T> capability) {
        return this.returnInv.getCapability(capability);
    }

    public void writeToNBT(CompoundTag tag) {
        this.configManager.writeToNBT(tag);
        this.patternInventory.writeToNBT(tag, "patterns");
        tag.putInt("priority", this.priority);

        ListTag sendListTag = new ListTag();
        for (var stack : sendList) {
            sendListTag.add(GenericStackHelper.writeGenericStack(stack));
        }
        tag.put("sendList", sendListTag);
        if (sendDirection != null) {
            tag.putByte("sendDirection", (byte) sendDirection.get3DDataValue());
        }

        tag.put("returnInv", this.returnInv.writeToNBT());
    }

    public void readFromNBT(CompoundTag tag) {
        this.configManager.readFromNBT(tag);
        this.patternInventory.readFromNBT(tag, "patterns");
        this.priority = tag.getInt("priority");

        ListTag sendListTag = tag.getList("sendList", Tag.TAG_COMPOUND);
        for (int i = 0; i < sendListTag.size(); ++i) {
            this.addToSendList(GenericStackHelper.readGenericStack(sendListTag.getCompound(i)));
        }
        if (tag.contains("sendDirection")) {
            sendDirection = Direction.from3DDataValue(tag.getByte("sendDirection"));
        }

        this.returnInv.readFromNBT(tag.getList("returnInv", Tag.TAG_COMPOUND));
    }

    public IConfigManager getConfigManager() {
        return this.configManager;
    }

    @Override
    public void saveChanges() {
        this.host.saveChanges();
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot, ItemStack removedStack, ItemStack newStack) {
        this.updatePatterns();
    }

    @Override
    public boolean isRemote() {
        Level level = this.host.getBlockEntity().getLevel();
        return level == null || level.isClientSide();
    }

    public void updatePatterns() {
        patterns.clear();

        for (var stack : this.patternInventory) {
            var details = AEApi.patterns().decodePattern(stack, this.host.getBlockEntity().getLevel());

            if (details != null) {
                patterns.add(details);
            }
        }

        mainNode.ifPresent(grid -> grid.postEvent(new GridCraftingPatternChange(this, mainNode.getNode())));
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        for (var details : this.patterns) {
            craftingTracker.addCraftingOption(this, details, this.priority);
        }
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, MixedItemList[] inputHolder) {
        if (!sendList.isEmpty() || !this.mainNode.isActive() || !this.patterns.contains(patternDetails)) {
            return false;
        }

        var be = host.getBlockEntity();
        var level = be.getLevel();

        for (var direction : host.getTargets()) {
            var adjPos = be.getBlockPos().relative(direction);
            var adjBe = level.getBlockEntity(adjPos);

            if (adjBe instanceof IPatternProviderHost adjHost) {
                if (adjHost.getDuality().sameGrid(this.mainNode.getGrid())) {
                    continue;
                }
            }

            if (adjBe instanceof ICraftingMachine craftingMachine) {
                if (craftingMachine.acceptsPlans()) {
                    if (craftingMachine.pushPattern(patternDetails, inputHolder, direction.getOpposite())) {
                        return true;
                    }
                    continue;
                }
            }

            var adapter = IInterfaceTarget.get(level, adjPos, adjBe, direction.getOpposite(), this.actionSource);
            if (adapter == null)
                continue;

            if (this.isBlocking() && adapter.isBusy()) {
                continue;
            }

            if (this.adapterAcceptsAll(adapter, inputHolder)) {
                for (var inputList : inputHolder) {
                    for (var input : inputList) {
                        this.addToSendList(adapter.injectItems(input, Actionable.MODULATE));
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

    private boolean adapterAcceptsAll(IInterfaceTarget target, MixedItemList[] inputHolder) {
        for (var inputList : inputHolder) {
            for (var input : inputList) {
                var leftover = target.injectItems(input, Actionable.SIMULATE);
                if (IAEStack.getStackSizeOrZero(leftover) == input.getStackSize()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addToSendList(@Nullable IAEStack stack) {
        if (stack != null && stack.getStackSize() != 0) {
            this.sendList.add(stack);

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
        var adapter = IInterfaceTarget.get(level, adjPos, adjBe, sendDirection.getOpposite(), actionSource);

        if (adapter == null) {
            return false;
        }

        boolean didSomething = false;

        for (var it = sendList.listIterator(); it.hasNext();) {
            var stack = it.next();
            var leftover = adapter.injectItems(stack, Actionable.MODULATE);
            if (leftover == null) {
                it.remove();
                didSomething = true;
            } else if (leftover.getStackSize() != stack.getStackSize()) {
                it.set(leftover);
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
        return returnInv.injectIntoNetwork(mainNode.getGrid().getStorageService(), actionSource) | sendStacksOut();
    }

    public InternalInventory getPatternInv() {
        return this.patternInventory;
    }

    public void onMainNodeStateChanged() {
        this.mainNode.ifPresent((grid, node) -> {
            grid.postEvent(new GridCraftingPatternChange(this, node));
            grid.getTickManager().alertDevice(node);
        });
    }

    public void addDrops(List<ItemStack> drops) {
        for (var stack : this.patternInventory) {
            drops.add(stack);
        }

        for (var stack : this.sendList) {
            if (stack.getChannel() == StorageChannels.items()) {
                drops.add(stack.cast(StorageChannels.items()).createItemStack());
            }
        }

        this.returnInv.addDrops(drops);
    }

    public GenericStackInv getReturnInv() {
        return this.returnInv;
    }

    private class Ticker implements IGridTickable {

        @Nonnull
        @Override
        public TickingRequest getTickingRequest(@Nonnull IGridNode node) {
            return new TickingRequest(TickRates.Interface.getMin(), TickRates.Interface.getMax(), !hasWorkToDo(), true);
        }

        @Nonnull
        @Override
        public TickRateModulation tickingRequest(@Nonnull IGridNode node, int ticksSinceLastCall) {
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

    public Component getTermName() {
        final BlockEntity host = this.host.getBlockEntity();
        final Level hostWorld = host.getLevel();

        if (((ICustomNameObject) this.host).hasCustomInventoryName()) {
            return ((ICustomNameObject) this.host).getCustomInventoryName();
        }

        final EnumSet<Direction> possibleDirections = this.host.getTargets();
        for (final Direction direction : possibleDirections) {
            final BlockPos targ = host.getBlockPos().relative(direction);
            final BlockEntity directedBlockEntity = hostWorld.getBlockEntity(targ);

            if (directedBlockEntity == null) {
                continue;
            }

            if (directedBlockEntity instanceof IPatternProviderHost interfaceHost) {
                if (interfaceHost.getDuality().sameGrid(this.mainNode.getGrid())) {
                    continue;
                }
            }

            var adaptor = InternalInventory.wrapExternal(directedBlockEntity, direction.getOpposite());
            if (directedBlockEntity instanceof ICraftingMachine || adaptor != null) {
                if (adaptor != null && !adaptor.mayAllowTransfer()) {
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
                        final ItemStack g = directedBlock.getPickBlock(directedBlockState, hit, hostWorld,
                                directedBlockEntity.getBlockPos(), null);
                        if (!g.isEmpty()) {
                            what = g;
                        }
                    }
                } catch (final Throwable t) {
                    BAD_BLOCKS.add(directedBlock); // nope!
                }

                if (what.getItem() != Items.AIR) {
                    return new TranslatableComponent(what.getDescriptionId());
                }

                final Item item = Item.byBlock(directedBlock);
                if (item == Items.AIR) {
                    return new TranslatableComponent(directedBlock.getDescriptionId());
                }
            }
        }

        return new TextComponent("Nothing");
    }

    public long getSortValue() {
        final BlockEntity te = this.host.getBlockEntity();
        return te.getBlockPos().getZ() << 24 ^ te.getBlockPos().getX() << 8 ^ te.getBlockPos().getY();
    }
}
