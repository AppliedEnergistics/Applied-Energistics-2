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

package appeng.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.connection.ConnectionType;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.config.Actionable;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPart;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.clientbound.GuiDataSyncPacket;
import appeng.core.network.serverbound.GuiActionPacket;
import appeng.helpers.InventoryAction;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.me.helpers.PlayerSource;
import appeng.menu.guisync.ClientActionKey;
import appeng.menu.guisync.DataSynchronization;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.CraftingMatrixSlot;
import appeng.menu.slot.CraftingTermSlot;
import appeng.menu.slot.DisabledSlot;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.parts.AEBasePart;
import appeng.util.ConfigMenuInventory;

public abstract class AEBaseMenu extends AbstractContainerMenu {
    private static final Logger LOG = LoggerFactory.getLogger(AEBaseMenu.class);

    private static final int MAX_CLIENT_ACTION_PAYLOAD = 32767;
    private static final int MAX_CONTAINER_TRANSFER_ITERATIONS = 256;
    private static final ClientActionKey<String> HIDE_SLOT = new ClientActionKey<>("HideSlot");

    private final IActionSource mySrc;
    @Nullable
    private final BlockEntity blockEntity;
    @Nullable
    private final IPart part;
    @Nullable
    protected final ItemMenuHost<?> itemMenuHost;
    private final DataSynchronization dataSync = new DataSynchronization(this);
    private final Inventory playerInventory;
    private final Set<Integer> lockedPlayerInventorySlots = new HashSet<>();
    private final Map<Slot, SlotSemantic> semanticBySlot = new HashMap<>();
    private final ArrayListMultimap<SlotSemantic, Slot> slotsBySemantic = ArrayListMultimap.create();
    private final Map<String, ClientAction<?>> clientActions = new HashMap<>();
    private boolean menuValid = true;
    private MenuHostLocator locator;
    // Slots that are only present on the client-side
    private final Set<Slot> clientSideSlot = new HashSet<>();
    /**
     * Indicates that the menu was created after returning from a {@link ISubMenu}. Previous screen state amount on the
     * client should be restored.
     */
    private boolean returnedFromSubScreen;

    public AEBaseMenu(MenuType<?> menuType, int id, Inventory playerInventory,
            Object host) {
        super(menuType, id);
        this.playerInventory = playerInventory;
        this.blockEntity = host instanceof BlockEntity ? (BlockEntity) host : null;
        this.part = host instanceof IPart ? (IPart) host : null;
        this.itemMenuHost = host instanceof ItemMenuHost<?> ? (ItemMenuHost<?>) host : null;

        if (host != null && this.blockEntity == null && this.part == null && this.itemMenuHost == null) {
            throw new IllegalArgumentException("Must have a valid host, instead " + host + " in " + playerInventory);
        }

        if (itemMenuHost != null && itemMenuHost.getPlayerInventorySlot() != null) {
            lockPlayerInventorySlot(itemMenuHost.getPlayerInventorySlot());
        }

        this.mySrc = new PlayerSource(getPlayer(), this.getActionHost());
        registerClientAction(HIDE_SLOT, ByteBufCodecs.STRING_UTF8, this::hideSlot);
    }

    protected final IActionHost getActionHost() {
        if (this.itemMenuHost instanceof IActionHost) {
            return (IActionHost) this.itemMenuHost;
        }

        if (this.blockEntity instanceof IActionHost) {
            return (IActionHost) this.blockEntity;
        }

        if (this.part instanceof IActionHost) {
            return (IActionHost) this.part;
        }

        return null;
    }

    protected final boolean isActionHost() {
        return this.itemMenuHost instanceof IActionHost
                || this.blockEntity instanceof IActionHost
                || this.part instanceof IActionHost;
    }

    /**
     * Convenience method to get the player owning this menu.
     */

    public Player getPlayer() {
        return getPlayerInventory().player;
    }

    protected final RegistryAccess registryAccess() {
        return getPlayer().registryAccess();
    }

    public IActionSource getActionSource() {
        return this.mySrc;
    }

    public Inventory getPlayerInventory() {
        return this.playerInventory;
    }

    public void lockPlayerInventorySlot(int invSlot) {
        Preconditions.checkArgument(invSlot >= 0 && invSlot < playerInventory.getContainerSize(),
                "cannot lock player inventory slot: %s", invSlot);
        this.lockedPlayerInventorySlots.add(invSlot);
    }

    public final boolean isPlayerInventorySlotLocked(int invSlot) {
        return this.lockedPlayerInventorySlots.contains(invSlot);
    }

    public Object getTarget() {
        if (this.blockEntity != null) {
            return this.blockEntity;
        }
        if (this.part != null) {
            return this.part;
        }
        return this.itemMenuHost;
    }

    public BlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    protected final void createPlayerInventorySlots(Inventory playerInventory) {
        Preconditions.checkState(
                getSlots(SlotSemantics.PLAYER_INVENTORY).isEmpty(),
                "Player inventory was already created");

        for (int i = 0; i < playerInventory.getNonEquipmentItems().size(); i++) {
            Slot slot;
            if (this.lockedPlayerInventorySlots.contains(i)) {
                slot = new DisabledSlot(playerInventory, i);
            } else {
                slot = new Slot(playerInventory, i, 0, 0);
            }
            var s = i < Inventory.getSelectionSize()
                    ? SlotSemantics.PLAYER_HOTBAR
                    : SlotSemantics.PLAYER_INVENTORY;
            addSlot(slot, s);
        }
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
        // Do not allow swapping with off-hand if the off-hand slot is locked
        if (clickType == ContainerInput.SWAP && isPlayerInventorySlotLocked(Inventory.SLOT_OFFHAND)) {
            return;
        }

        super.clicked(slotId, button, clickType, player);
    }

    protected Slot addSlot(Slot slot, SlotSemantic semantic) {
        slot = this.addSlot(slot);

        Preconditions.checkState(!semanticBySlot.containsKey(slot));
        semanticBySlot.put(slot, semantic);
        slotsBySemantic.put(semantic, slot);
        return slot;
    }

    /**
     * Allows screens to add client-side only slots.
     * <p/>
     * For sub-screens that need their slots to be removed when returning to the parent, use the
     * <code>addClientSideSlot</code> method in {@link appeng.client.gui.implementations.AESubScreen} instead.
     */
    public Slot addClientSideSlot(Slot slot, SlotSemantic semantic) {
        Preconditions.checkState(isClientSide(), "Can only add client-side slots on the client");
        if (!clientSideSlot.add(slot)) {
            throw new IllegalStateException("Client-side slot already exists");
        }

        // We're only doing this on the client-side.
        // The synchronization related fields are not used
        slot.index = slots.size();
        slots.add(slot);

        if (semantic != null) {
            semanticBySlot.put(slot, semantic);
            slotsBySemantic.put(semantic, slot);
        }
        return slot;
    }

    public void removeClientSideSlot(Slot slot) {
        if (slots.get(slot.index) != slot) {
            throw new IllegalStateException("Trying to remove slot which isn't currently in the menu");
        }
        if (!clientSideSlot.remove(slot)) {
            throw new IllegalStateException("Trying to remove slot which isn't a client-side slot");
        }

        slots.remove(slot.index);
        semanticBySlot.remove(slot);
        slotsBySemantic.values().remove(slot);

        // Update the slot index for any subsequent slots
        for (int i = slot.index; i < slots.size(); i++) {
            slots.get(i).index = i;
        }
    }

    public boolean isClientSideSlot(Slot slot) {
        return clientSideSlot.contains(slot);
    }

    public List<Slot> getSlots(SlotSemantic semantic) {
        return slotsBySemantic.get(semantic);
    }

    @Override
    protected Slot addSlot(Slot newSlot) {
        if (newSlot instanceof AppEngSlot s) {
            s.setMenu(this);
        }
        return super.addSlot(newSlot);
    }

    @Override
    public void initializeContents(int stateId, List<ItemStack> items, ItemStack carried) {
        for (int i = 0; i < items.size(); ++i) {
            var slot = this.getSlot(i);
            if (slot instanceof AppEngSlot aeSlot) {
                aeSlot.initialize(items.get(i));
            } else {
                slot.set(items.get(i));
            }
        }

        this.setCarried(carried);
        this.stateId = stateId;
    }

    @Override
    public void broadcastChanges() {
        if (!isValidMenu()) {
            return;
        }

        if (itemMenuHost != null) {
            if (!itemMenuHost.isValid()) {
                setValidMenu(false);
                return;
            }
            itemMenuHost.tick();
        }

        if (isServerSide()) {
            if (this.blockEntity != null
                    && this.blockEntity.getLevel().getBlockEntity(this.blockEntity.getBlockPos()) != this.blockEntity) {
                this.setValidMenu(false);
            }
            if (this.part instanceof AEBasePart basePart) {
                var host = basePart.getHost();
                if (host == null || !host.isInWorld() || host.getPart(basePart.getSide()) != basePart) {
                    setValidMenu(false);
                }
            }

            if (dataSync.hasChanges()) {
                sendPacketToClient(new GuiDataSyncPacket(containerId, dataSync::writeUpdate, registryAccess()));
            }
        }

        super.broadcastChanges();
    }

    /**
     * Get a slots priority for quick moving items (higher priority slots are tried first)
     */
    protected int getQuickMovePriority(Slot slot) {
        var semantic = getSlotSemantic(slot);
        if (semantic == null) {
            return 0;
        }
        return semantic.quickMovePriority();
    }

    /**
     * Check if a given slot is considered to be "on the player side" for the purposes of shift-clicking items back and
     * forth between the opened menu and the player's inventory.
     */
    public boolean isPlayerSideSlot(Slot slot) {
        if (slot.container == playerInventory) {
            return true;
        }

        var slotSemantic = semanticBySlot.get(slot);
        return slotSemantic != null && slotSemantic.playerSide();
    }

    @Nullable
    public SlotSemantic getSlotSemantic(Slot s) {
        return semanticBySlot.get(s);
    }

    public void hideSlot(String semantic) {
        if (isClientSide()) {
            sendClientAction(HIDE_SLOT, semantic);
        }
        var slotSemantic = SlotSemantics.get(semantic);
        if (slotSemantic == null)
            return;
        if (canSlotsBeHidden(slotSemantic)) {
            for (Slot s : getSlots(slotSemantic)) {
                if (s instanceof AppEngSlot slot) {
                    slot.setSlotEnabled(false);
                }
            }
        }
    }

    /**
     * Return true if the client is allowed to hide slots with this semantic, false otherwise. You should return false
     * unless you have checked that hiding the slots doesn't risk crashing the game or causing other severe issues.
     */
    protected boolean canSlotsBeHidden(SlotSemantic semantic) {
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int idx) {
        if (isClientSide()) {
            return ItemStack.EMPTY;
        }

        var clickSlot = this.slots.get(idx);

        // Vanilla will check this before calling this method, but we do use it in other contexts as well (move region)
        if (!clickSlot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }

        var stackToMove = clickSlot.getItem();
        if (stackToMove.isEmpty()) {
            return ItemStack.EMPTY;
        }

        boolean fromPlayerSide = isPlayerSideSlot(clickSlot);

        // Allow moving items from player-side slots into some "remote" inventory that is not slot-based
        // This is used to move items into the network inventory
        if (fromPlayerSide) {
            // With a Storage Bus on a Just Dire Things player accessor, the stack to move might be modified during the
            // transfer. So keep track of how much was transferred and only subtract it at the end.
            int transferred = transferStackToMenu(stackToMove.copy());
            if (transferred > 0) {
                clickSlot.remove(transferred);
            }
        }
        stackToMove = clickSlot.getItem();
        if (stackToMove.isEmpty()) {
            return ItemStack.EMPTY;
        }

        var originalStackToMove = stackToMove.copy();

        stackToMove = quickMoveToOtherSlots(stackToMove, isPlayerSideSlot(clickSlot));

        // While we did modify stackToMove in-place, this causes the container to be notified of the change
        if (!ItemStack.matches(originalStackToMove, stackToMove)) {
            clickSlot.setByPlayer(stackToMove.isEmpty() ? ItemStack.EMPTY : stackToMove);
        }

        return ItemStack.EMPTY;
    }

    private ItemStack quickMoveToOtherSlots(ItemStack stackToMove, boolean fromPlayerSide) {
        var destinationSlots = getQuickMoveDestinationSlots(stackToMove, fromPlayerSide);

        // If no actual targets were available, allow moving into filter slots too
        if (destinationSlots.isEmpty() && fromPlayerSide) {
            for (Slot cs : this.slots) {
                if (cs instanceof FakeSlot && !isPlayerSideSlot(cs)) {
                    var destination = cs.getItem();
                    if (ItemStack.isSameItemSameComponents(destination, stackToMove)) {
                        break; // Item is already in the filter
                    } else if (destination.isEmpty()) {
                        cs.set(stackToMove.copy());
                        // ???
                        this.broadcastChanges();
                        break;
                    }
                }
            }
            return stackToMove; // Since destinationSlots was empty, nothing else to do
        }

        // Try stacking the item into filled slots first
        for (var dest : destinationSlots) {
            if (dest.hasItem() && (stackToMove = dest.safeInsert(stackToMove)).isEmpty()) {
                return stackToMove;
            }
        }

        // Now try placing it in empty slots, if it's not already fully consumed
        for (var dest : destinationSlots) {
            if (!dest.hasItem() && (stackToMove = dest.safeInsert(stackToMove)).isEmpty()) {
                return stackToMove;
            }
        }

        return stackToMove;
    }

    protected List<Slot> getQuickMoveDestinationSlots(ItemStack stackToMove, boolean fromPlayerSide) {
        // Find potential destination slots
        var destinationSlots = new ArrayList<Slot>();
        for (var candidateSlot : this.slots) {
            if (isValidQuickMoveDestination(candidateSlot, stackToMove, fromPlayerSide)) {
                destinationSlots.add(candidateSlot);
            }
        }

        // Order slots by the priority of their semantic
        destinationSlots.sort(Comparator.comparingInt(this::getQuickMovePriority).reversed());
        return destinationSlots;
    }

    /**
     * Check if a given candidate slot is a valid destination for {@link #quickMoveStack}.
     */
    protected boolean isValidQuickMoveDestination(Slot candidateSlot, ItemStack stackToMove,
            boolean fromPlayerSide) {
        return isPlayerSideSlot(candidateSlot) != fromPlayerSide
                && !(candidateSlot instanceof FakeSlot)
                && !(candidateSlot instanceof CraftingMatrixSlot)
                && candidateSlot.mayPlace(stackToMove);
    }

    protected int getPlaceableAmount(Slot s, AEItemKey what) {
        if (!s.mayPlace(what.toStack())) {
            return 0;
        }

        var currentItem = s.getItem();
        if (currentItem.isEmpty()) {
            return s.getMaxStackSize(what.getReadOnlyStack());
        } else if (what.matches(currentItem)) {
            return Math.max(0, s.getMaxStackSize(currentItem) - currentItem.getCount());
        } else {
            return 0;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.isValidMenu()) {
            if (this.blockEntity instanceof Container) {
                return ((Container) this.blockEntity).stillValid(player);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canDragTo(Slot s) {
        if (s instanceof AppEngSlot) {
            return ((AppEngSlot) s).isDraggable();
        } else {
            return super.canDragTo(s);
        }
    }

    /**
     * Sets a filter slot based on a <b>non-existent</b> item sent by the client.
     */
    public void setFilter(int slotIndex, ItemStack item) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return;
        }
        final Slot s = this.getSlot(slotIndex);
        if (!(s instanceof AppEngSlot appEngSlot)) {
            return;
        }
        if (!appEngSlot.isSlotEnabled()) {
            return;
        }

        if (s instanceof FakeSlot fakeSlot && fakeSlot.canSetFilterTo(item)) {
            s.set(item);
        }
    }

    public void doAction(ServerPlayer player, InventoryAction action, int slot, long id) {
        if (slot < 0 || slot >= this.slots.size()) {
            return;
        }
        var s = this.getSlot(slot);

        if (s instanceof CraftingTermSlot craftingTermSlot) {
            switch (action) {
                case CRAFT_SHIFT:
                case CRAFT_ALL:
                case CRAFT_ITEM:
                case CRAFT_STACK:
                    craftingTermSlot.doClick(action, player);
                default:
            }
        }

        if (s instanceof FakeSlot fakeSlot) {
            handleFakeSlotAction(fakeSlot, action);
            return; // No further interaction allowed with fake slots
        }

        // We support filling and emptying for slots backed by generic inventories
        if (s instanceof AppEngSlot appEngSlot
                && appEngSlot.getInventory() instanceof ConfigMenuInventory configInv
                && configInv.getDelegate().getMode() == GenericStackInv.Mode.STORAGE) {
            var realInv = configInv.getDelegate();
            var realInvSlot = appEngSlot.getSlotIndex();

            if (action == InventoryAction.FILL_ITEM || action == InventoryAction.FILL_ENTIRE_ITEM) {
                var what = realInv.getKey(realInvSlot);
                handleFillingHeldItem(
                        (amount, mode) -> realInv.extract(realInvSlot, what, amount, mode),
                        what, action == InventoryAction.FILL_ENTIRE_ITEM);
            } else if (action == InventoryAction.EMPTY_ITEM || action == InventoryAction.EMPTY_ENTIRE_ITEM) {
                handleEmptyHeldItem((what, amount, mode) -> realInv.insert(realInvSlot, what, amount, mode),
                        action == InventoryAction.EMPTY_ENTIRE_ITEM);
            }
        }

        if (action == InventoryAction.MOVE_REGION) {
            var slotSemantic = getSlotSemantic(s);
            if (slotSemantic != null) {
                // Avoid potential concurrent modification i.e. if an upgrade is moved into the machine
                List<Slot> slotsToMove = List.copyOf(getSlots(slotSemantic));
                for (var slotToMove : slotsToMove) {
                    quickMoveStack(player, slotToMove.index);
                }
            } else {
                quickMoveStack(player, s.index);
            }
        }

    }

    protected interface FillingSource {
        long extract(long amount, Actionable mode);
    }

    protected final void handleFillingHeldItem(FillingSource source, AEKey what, boolean fillAll) {
        var ctx = ContainerItemStrategies.findCarriedContextForKey(what, getPlayer(), this);
        if (ctx == null) {
            return;
        }

        long amount = fillAll ? Long.MAX_VALUE : what.getAmountPerUnit();
        boolean filled = false;
        int maxIterations = fillAll ? MAX_CONTAINER_TRANSFER_ITERATIONS : 1;

        while (maxIterations > 0) {
            // Check if we can pull out of the system
            var canPull = source.extract(amount, Actionable.SIMULATE);
            if (canPull <= 0) {
                break;
            }

            // Check how much we can store in the item
            long amountAllowed = ctx.insert(what, canPull, Actionable.SIMULATE);
            if (amountAllowed == 0) {
                break; // Nothing.
            }

            // Now actually pull out of the system
            var extracted = source.extract(amountAllowed, Actionable.MODULATE);
            if (extracted <= 0) {
                // Something went wrong
                LOG.error("Unable to pull fluid out of the ME system even though the simulation said yes ");
                break;
            }

            // Actually store possible amount in the held item
            long inserted = ctx.insert(what, extracted, Actionable.MODULATE);
            if (inserted == 0) {
                break;
            }

            filled = true;
            maxIterations--;
        }

        if (filled) {
            ctx.playFillSound(getPlayer(), what);
        }
    }

    protected interface EmptyingSink {
        long insert(AEKey what, long amount, Actionable mode);
    }

    protected final void handleEmptyHeldItem(EmptyingSink sink, boolean emptyAll) {
        var ctx = ContainerItemStrategies.findCarriedContext(null, getPlayer(), this);
        if (ctx == null) {
            return;
        }

        // See how much we can drain from the item
        var content = ctx.getExtractableContent();
        if (content == null || content.amount() == 0) {
            return;
        }

        var what = content.what();
        long amount = emptyAll ? Long.MAX_VALUE : what.getAmountPerUnit();
        int maxIterations = emptyAll ? MAX_CONTAINER_TRANSFER_ITERATIONS : 1;
        boolean emptied = false;

        while (maxIterations > 0) {
            // Check if we can pull out of the container
            var canExtract = ctx.extract(what, amount, Actionable.SIMULATE);
            if (canExtract <= 0) {
                break;
            }

            // Check if we can push into the system
            var amountAllowed = sink.insert(what, canExtract, Actionable.SIMULATE);
            if (amountAllowed <= 0) {
                break;
            }

            // Actually drain
            var extracted = ctx.extract(what, amountAllowed, Actionable.MODULATE);
            if (extracted != amountAllowed) {
                LOG.error(
                        "Fluid item [{}] reported a different possible amount to drain than it actually provided.",
                        getCarried());
                break;
            }

            // Actually push into the system
            if (sink.insert(what, extracted, Actionable.MODULATE) != extracted) {
                LOG.error("Failed to insert previously simulated {} into ME system", what);
                break;
            }

            emptied = true;
            maxIterations--;
        }

        if (emptied) {
            ctx.playEmptySound(getPlayer(), what);
        }
    }

    private void handleFakeSlotAction(FakeSlot fakeSlot, InventoryAction action) {
        var hand = getCarried();

        switch (action) {
            case PICKUP_OR_SET_DOWN:
                fakeSlot.increase(hand);
                break;
            case PLACE_SINGLE:
                if (!hand.isEmpty()) {
                    var is = hand.copy();
                    is.setCount(1);
                    fakeSlot.increase(is);
                }

                break;
            case SPLIT_OR_PLACE_SINGLE:
                var is = fakeSlot.getItem();
                if (!is.isEmpty()) {
                    fakeSlot.decrease(hand);
                } else if (!hand.isEmpty()) {
                    is = hand.copy();
                    is.setCount(1);
                    fakeSlot.set(is);
                }

                break;
            case EMPTY_ITEM: {
                var emptyingAction = ContainerItemStrategies.getEmptyingAction(hand);
                if (emptyingAction != null) {
                    fakeSlot.set(GenericStack.wrapInItemStack(emptyingAction.what(), emptyingAction.maxAmount()));
                }
            }
                break;
            default:
                break;
        }
    }

    /**
     * @return Returns how many items were transfered.
     */
    protected int transferStackToMenu(ItemStack input) {
        return 0;
    }

    public void swapSlotContents(int slotA, int slotB) {
        final Slot a = this.getSlot(slotA);
        final Slot b = this.getSlot(slotB);

        // NPE protection...
        if (a == null || b == null) {
            return;
        }

        final ItemStack isA = a.getItem();
        final ItemStack isB = b.getItem();

        // something to do?
        if (isA.isEmpty() && isB.isEmpty()) {
            return;
        }

        // can take?

        if (!isA.isEmpty() && !a.mayPickup(this.getPlayerInventory().player)) {
            return;
        }

        if (!isB.isEmpty() && !b.mayPickup(this.getPlayerInventory().player)) {
            return;
        }

        // swap valid?

        if (!isB.isEmpty() && !a.mayPlace(isB)) {
            return;
        }

        if (!isA.isEmpty() && !b.mayPlace(isA)) {
            return;
        }

        ItemStack testA = isB.isEmpty() ? ItemStack.EMPTY : isB.copy();
        ItemStack testB = isA.isEmpty() ? ItemStack.EMPTY : isA.copy();

        // can put some back?
        if (!testA.isEmpty() && testA.getCount() > a.getMaxStackSize()) {
            if (!testB.isEmpty()) {
                return;
            }

            final int totalA = testA.getCount();
            testA.setCount(a.getMaxStackSize());
            testB = testA.copy();

            testB.setCount(totalA - testA.getCount());
        }

        if (!testB.isEmpty() && testB.getCount() > b.getMaxStackSize()) {
            if (!testA.isEmpty()) {
                return;
            }

            final int totalB = testB.getCount();
            testB.setCount(b.getMaxStackSize());
            testA = testB.copy();

            testA.setCount(totalB - testA.getCount());
        }

        a.set(testA);
        b.set(testB);
    }

    @Nullable
    protected final ServerLevel getServerLevel() {
        return getPlayer().level() instanceof ServerLevel serverLevel ? serverLevel : null;
    }

    /**
     * Can be overridden in subclasses to be notified of GUI data updates sent by the server.
     */
    @MustBeInvokedByOverriders
    public void onServerDataSync(ShortSet updatedFields) {
    }

    public void onSlotChange(Slot s) {

    }

    public boolean isValidForSlot(Slot s, ItemStack i) {
        return true;
    }

    public boolean isValidMenu() {
        return this.menuValid;
    }

    public void setValidMenu(boolean isContainerValid) {
        this.menuValid = isContainerValid;
    }

    public MenuHostLocator getLocator() {
        return this.locator;
    }

    public void setLocator(MenuHostLocator locator) {
        this.locator = locator;
    }

    /**
     * Returns whether this menu instance lives on the client.
     */
    public boolean isClientSide() {
        return getPlayer().level().isClientSide();
    }

    /**
     * Returns whether this menu instance lives on the server.
     */
    protected boolean isServerSide() {
        return !isClientSide();
    }

    protected final void sendPacketToClient(ClientboundPacket packet) {
        if (getPlayer() instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(packet);
        }
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();

        if (dataSync.hasFields()) {
            sendPacketToClient(new GuiDataSyncPacket(containerId, dataSync::writeFull, registryAccess()));
        }
    }

    /**
     * Receives data from the server for synchronizing fields of this class.
     */
    public final void receiveServerSyncData(RegistryFriendlyByteBuf data) {
        ShortSet updatedFields = new ShortOpenHashSet();
        this.dataSync.readUpdate(data, updatedFields);
        this.onServerDataSync(updatedFields);
    }

    /**
     * Receives a menu action from the client.
     */
    public final void receiveClientAction(String actionName, byte[] argumentPayload) {
        ClientAction<?> action = clientActions.get(actionName);
        if (action == null) {
            throw new IllegalArgumentException("Unknown client action: '" + actionName + "'");
        }

        action.handle(argumentPayload, registryAccess());
    }

    /**
     * Registers a handler for a client-initiated GUI action. Keep in mind in your handler that the client can send
     * arbitrary data. The given argument class will be serialized as JSON across the wire, so it must be GSON
     * serializable.
     */
    protected final <T> void registerClientAction(ClientActionKey<T> key,
            StreamCodec<? super RegistryFriendlyByteBuf, T> argCodec, Consumer<T> handler) {
        if (clientActions.containsKey(key.name())) {
            throw new IllegalArgumentException("Duplicate client action registered: " + key);
        }

        clientActions.put(key.name(), new ClientAction<>(key, argCodec, handler));
    }

    /**
     * Convenience function for registering a client action with no argument.
     *
     * @see #registerClientAction(ClientActionKey, StreamCodec, Consumer)
     */
    protected final void registerClientAction(ClientActionKey<Void> key, Runnable callback) {
        registerClientAction(key, null, arg -> callback.run());
    }

    /**
     * Trigger a client action on the server.
     */
    protected final <T> void sendClientAction(ClientActionKey<T> action, T arg) {
        @SuppressWarnings("unchecked")
        ClientAction<T> clientAction = (ClientAction<T>) clientActions.get(action.name());
        if (clientAction == null) {
            throw new IllegalArgumentException("Trying to send unregistered client action: " + action);
        }

        // Serialize the argument to JSON for sending it in the packet
        byte[] argumentPayload;
        if (clientAction.argCodec == null) {
            if (arg != null) {
                throw new IllegalArgumentException(
                        "Client action " + action + " requires no argument, but it was given");
            }
            argumentPayload = new byte[0];
        } else {
            if (arg == null) {
                throw new IllegalArgumentException(
                        "Client action " + action + " requires an argument, but none was given");
            }
            var buffer = new RegistryFriendlyByteBuf(
                    Unpooled.buffer(),
                    registryAccess(),
                    ConnectionType.NEOFORGE);
            clientAction.argCodec.encode(buffer, arg);
            argumentPayload = new byte[buffer.readableBytes()];
            buffer.readBytes(argumentPayload);
        }

        ServerboundPacket message = new GuiActionPacket(containerId, clientAction.key().name(), argumentPayload);
        ClientPacketDistributor.sendToServer(message);
    }

    /**
     * Sends a previously registered client action to the server.
     */
    protected final void sendClientAction(ClientActionKey<Void> action) {
        sendClientAction(action, null);
    }

    /**
     * Registration for an action that a client can initiate.
     */
    private record ClientAction<T>(
            ClientActionKey<T> key,
            @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> argCodec,
            Consumer<T> handler) {
        public void handle(byte[] payload, RegistryAccess registryAccess) {
            T arg = null;
            LOG.debug("Handling client action '{}' with payload {}", key, HexFormat.of().formatHex(payload));
            if (argCodec != null) {
                var buffer = new RegistryFriendlyByteBuf(
                        Unpooled.wrappedBuffer(payload),
                        registryAccess,
                        ConnectionType.NEOFORGE);
                arg = argCodec.decode(buffer);
            } else {
                if (payload.length > 0) {
                    LOG.warn("Client action {} should not have an argmuent, but received payload: {}", arg,
                            HexFormat.of().formatHex(payload));
                }
            }

            this.handler.accept(arg);
        }
    }

    protected final void setupUpgrades(IUpgradeInventory upgrades) {
        for (int i = 0; i < upgrades.size(); i++) {
            var slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, i);
            slot.setNotDraggable();
            this.addSlot(slot, SlotSemantics.UPGRADE);
        }
    }

    public boolean isReturnedFromSubScreen() {
        return returnedFromSubScreen;
    }

    public void setReturnedFromSubScreen(boolean returnedFromSubScreen) {
        this.returnedFromSubScreen = returnedFromSubScreen;
    }

    protected final <T> void syncField(int id,
            Supplier<T> getter,
            Consumer<T> setter,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {

    }
}
