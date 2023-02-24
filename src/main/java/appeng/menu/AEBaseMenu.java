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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPart;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.core.AELog;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.GuiDataSyncPacket;
import appeng.helpers.InventoryAction;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.me.helpers.PlayerSource;
import appeng.menu.guisync.DataSynchronization;
import appeng.menu.locator.MenuLocator;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.CraftingMatrixSlot;
import appeng.menu.slot.CraftingTermSlot;
import appeng.menu.slot.DisabledSlot;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.InaccessibleSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.ConfigMenuInventory;
import appeng.util.Platform;

public abstract class AEBaseMenu extends AbstractContainerMenu {
    private static final int MAX_STRING_LENGTH = 32767;
    private static final String HIDE_SLOT = "HideSlot";

    private final IActionSource mySrc;
    @Nullable
    private final BlockEntity blockEntity;
    @Nullable
    private final IPart part;
    @Nullable
    protected final ItemMenuHost itemMenuHost;
    private final DataSynchronization dataSync = new DataSynchronization(this);
    private final Inventory playerInventory;
    private final Set<Integer> lockedPlayerInventorySlots = new HashSet<>();
    private final Map<Slot, SlotSemantic> semanticBySlot = new HashMap<>();
    private final ArrayListMultimap<SlotSemantic, Slot> slotsBySemantic = ArrayListMultimap.create();
    private final Map<String, ClientAction<?>> clientActions = new HashMap<>();
    private boolean menuValid = true;
    private MenuLocator locator;
    private int ticksSinceCheck = 900;
    // Slots that are only present on the client-side
    private final Set<Slot> clientSideSlot = new HashSet<>();
    /**
     * Indicates that the menu was created after returning from a {@link ISubMenu}. Previous screen state stored on the
     * client should be restored.
     */
    private boolean returnedFromSubScreen;

    public AEBaseMenu(MenuType<?> menuType, int id, Inventory playerInventory,
            Object host) {
        super(menuType, id);
        this.playerInventory = playerInventory;
        this.blockEntity = host instanceof BlockEntity ? (BlockEntity) host : null;
        this.part = host instanceof IPart ? (IPart) host : null;
        this.itemMenuHost = host instanceof ItemMenuHost ? (ItemMenuHost) host : null;

        if (host != null && this.blockEntity == null && this.part == null && this.itemMenuHost == null) {
            throw new IllegalArgumentException("Must have a valid host, instead " + host + " in " + playerInventory);
        }

        if (itemMenuHost != null && itemMenuHost.getSlot() != null) {
            lockPlayerInventorySlot(itemMenuHost.getSlot());
        }

        this.mySrc = new PlayerSource(getPlayer(), this.getActionHost());
        registerClientAction(HIDE_SLOT, String.class, this::hideSlot);
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

    public IActionSource getActionSource() {
        return this.mySrc;
    }

    public void verifyPermissions(SecurityPermissions security, boolean requirePower) {
        if (isClientSide()) {
            return;
        }

        this.ticksSinceCheck++;
        if (this.ticksSinceCheck < 20) {
            return;
        }

        this.ticksSinceCheck = 0;
        this.setValidMenu(this.isValidMenu() && this.hasAccess(security, requirePower));
    }

    protected final boolean hasAccess(SecurityPermissions perm, boolean requirePower) {
        if (!isActionHost() && !requirePower) {
            return true; // Hosts that are not grid connected always give access
        }

        var host = this.getActionHost();
        if (host != null) {
            return Platform.checkPermissions(getPlayer(), host, perm, requirePower, false);
        } else {
            return false;
        }
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

        for (int i = 0; i < playerInventory.items.size(); i++) {
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
        if (itemMenuHost != null && !itemMenuHost.onBroadcastChanges(this)) {
            setValidMenu(false);
            return;
        }

        if (isServerSide()) {
            if (this.blockEntity != null
                    && this.blockEntity.getLevel().getBlockEntity(this.blockEntity.getBlockPos()) != this.blockEntity) {
                this.setValidMenu(false);
            }

            if (dataSync.hasChanges()) {
                sendPacketToClient(new GuiDataSyncPacket(containerId, dataSync::writeUpdate));
            }
        }

        super.broadcastChanges();
    }

    /**
     * Check if a given slot is considered to be "on the player side" for the purposes of shift-clicking items back and
     * forth between the opened menu and the player's inventory.
     */
    private boolean isPlayerSideSlot(Slot slot) {
        if (slot.container == playerInventory) {
            return true;
        }

        SlotSemantic slotSemantic = semanticBySlot.get(slot);
        return slotSemantic == SlotSemantics.PLAYER_INVENTORY
                || slotSemantic == SlotSemantics.PLAYER_HOTBAR
                || slotSemantic == SlotSemantics.TOOLBOX
                // The crafting grid in the crafting terminal also shift-clicks into the network
                || slotSemantic == SlotSemantics.CRAFTING_GRID;
    }

    @org.jetbrains.annotations.Nullable
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

        final Slot clickSlot = this.slots.get(idx);
        boolean playerSide = isPlayerSideSlot(clickSlot);

        if (clickSlot instanceof DisabledSlot || clickSlot instanceof InaccessibleSlot) {
            return ItemStack.EMPTY;
        }
        if (clickSlot.hasItem()) {
            ItemStack tis = clickSlot.getItem();

            if (tis.isEmpty()) {
                return ItemStack.EMPTY;
            }

            final List<Slot> selectedSlots = new ArrayList<>();

            // Gather a list of valid destinations.
            if (playerSide) {
                tis = this.transferStackToMenu(tis);

                if (!tis.isEmpty()) {
                    // target slots in the menu...
                    for (Slot cs : this.slots) {
                        if (!isPlayerSideSlot(cs) && !(cs instanceof FakeSlot) && !(cs instanceof CraftingMatrixSlot)
                                && cs.mayPlace(tis)) {
                            selectedSlots.add(cs);
                        }
                    }
                }
            } else {
                tis = tis.copy();

                // target slots in the menu...
                for (Slot cs : this.slots) {
                    if (isPlayerSideSlot(cs) && !(cs instanceof FakeSlot) && !(cs instanceof CraftingMatrixSlot)
                            && cs.mayPlace(tis)) {
                        selectedSlots.add(cs);
                    }
                }
            }

            // Handle Fake Slot Shift clicking.
            if (selectedSlots.isEmpty() && playerSide && !tis.isEmpty()) {
                // target slots in the menu...
                for (Slot cs : this.slots) {
                    if (cs instanceof FakeSlot && !isPlayerSideSlot(cs)) {
                        var destination = cs.getItem();
                        if (ItemStack.isSameItemSameTags(destination, tis)) {
                            break; // Item is already in the filter
                        } else if (destination.isEmpty()) {
                            cs.set(tis.copy());
                            // ???
                            this.broadcastChanges();
                            break;
                        }
                    }
                }
            }

            if (!tis.isEmpty()) {
                // find slots to stack the item into
                for (Slot d : selectedSlots) {
                    if (d.mayPlace(tis) && d.hasItem() && x(clickSlot, tis, d)) {
                        return ItemStack.EMPTY;
                    }
                }

                // FIXME figure out whats the difference between this and the one above ?!
                // any match..
                for (Slot d : selectedSlots) {
                    if (d.mayPlace(tis)) {
                        if (d.hasItem()) {
                            if (x(clickSlot, tis, d)) {
                                return ItemStack.EMPTY;
                            }
                        } else {
                            int maxSize = tis.getMaxStackSize();
                            if (maxSize > d.getMaxStackSize()) {
                                maxSize = d.getMaxStackSize();
                            }

                            final ItemStack tmp = tis.copy();
                            if (tmp.getCount() > maxSize) {
                                tmp.setCount(maxSize);
                            }

                            tis.setCount(tis.getCount() - tmp.getCount());
                            d.set(tmp);

                            if (tis.getCount() <= 0) {
                                clickSlot.set(ItemStack.EMPTY);
                                d.setChanged();

                                this.broadcastChanges();
                                return ItemStack.EMPTY;
                            } else {
                                this.broadcastChanges();
                            }
                        }
                    }
                }
            }

            clickSlot.set(!tis.isEmpty() ? tis : ItemStack.EMPTY);
        }

        // ???
        this.broadcastChanges();
        return ItemStack.EMPTY;
    }

    private boolean x(Slot clickSlot, ItemStack tis, Slot d) {
        final ItemStack t = d.getItem().copy();

        if (ItemStack.isSameItemSameTags(t, tis)) {
            int maxSize = t.getMaxStackSize();
            if (maxSize > d.getMaxStackSize()) {
                maxSize = d.getMaxStackSize();
            }

            int placeable = maxSize - t.getCount();
            if (placeable > 0) {
                if (tis.getCount() < placeable) {
                    placeable = tis.getCount();
                }

                t.setCount(t.getCount() + placeable);
                tis.setCount(tis.getCount() - placeable);

                d.set(t);

                if (tis.getCount() <= 0) {
                    clickSlot.set(ItemStack.EMPTY);
                    d.setChanged();

                    // ???
                    this.broadcastChanges();
                    // ???
                    this.broadcastChanges();
                    return true;
                } else {
                    // ???
                    this.broadcastChanges();
                }
            }
        }
        return false;
    }

    @Override
    public boolean stillValid(Player PlayerEntity) {
        if (this.isValidMenu()) {
            if (this.blockEntity instanceof Container) {
                return ((Container) this.blockEntity).stillValid(PlayerEntity);
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

        if (s instanceof FakeSlot) {
            s.set(item);
        }
    }

    public void doAction(ServerPlayer player, InventoryAction action, int slot, long id) {
        if (slot < 0 || slot >= this.slots.size()) {
            return;
        }
        var s = this.getSlot(slot);

        if (s instanceof CraftingTermSlot) {
            switch (action) {
                case CRAFT_SHIFT:
                case CRAFT_ITEM:
                case CRAFT_STACK:
                    ((CraftingTermSlot) s).doClick(action, player);
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
            var realInvSlot = appEngSlot.slot;

            if (action == InventoryAction.FILL_ITEM) {
                var what = realInv.getKey(realInvSlot);
                handleFillingHeldItem(
                        (amount, mode) -> realInv.extract(realInvSlot, what, amount, mode),
                        what);
            } else if (action == InventoryAction.EMPTY_ITEM) {
                handleEmptyHeldItem((what, amount, mode) -> realInv.insert(realInvSlot, what, amount, mode));
            }
        }

        if (action == InventoryAction.MOVE_REGION) {
            final List<Slot> from = new ArrayList<>();

            for (Slot j : this.slots) {
                if (j != null && j.getClass() == s.getClass() && !(j instanceof CraftingTermSlot)) {
                    from.add(j);
                }
            }

            for (Slot fr : from) {
                this.quickMoveStack(player, fr.index);
            }
        }

    }

    protected interface FillingSource {
        long extract(long amount, Actionable mode);
    }

    protected final void handleFillingHeldItem(FillingSource source, AEKey what) {
        var ctx = ContainerItemStrategies.findCarriedContextForKey(what, getPlayer(), this);
        if (ctx == null) {
            return;
        }

        // Check how much we can store in the item
        long amountAllowed = ctx.insert(what, Long.MAX_VALUE, Actionable.SIMULATE);
        if (amountAllowed == 0) {
            return; // Nothing.
        }

        // Check if we can pull out of the system
        var canPull = source.extract(amountAllowed, Actionable.SIMULATE);
        if (canPull <= 0) {
            return;
        }

        // How much could fit into the carried container
        long canFill = ctx.insert(what, canPull, Actionable.MODULATE);
        if (canFill == 0) {
            return;
        }

        // Now actually pull out of the system
        var extracted = source.extract(canFill, Actionable.MODULATE);
        if (extracted <= 0) {
            // Something went wrong
            AELog.error("Unable to pull fluid out of the ME system even though the simulation said yes ");
            return;
        }

        ctx.playFillSound(getPlayer(), what);
    }

    protected interface EmptyingSink {
        long insert(AEKey what, long amount, Actionable mode);
    }

    protected final void handleEmptyHeldItem(EmptyingSink sink) {
        var ctx = ContainerItemStrategies.findCarriedContext(null, getPlayer(), this);
        if (ctx == null) {
            return;
        }

        // See how much we can drain from the item
        var content = ctx.getExtractableContent();
        if (content == null) {
            return;
        }

        var what = content.what();
        var amount = content.amount();

        // Check if we can push into the system
        var canInsert = sink.insert(what, amount, Actionable.SIMULATE);
        if (canInsert <= 0) {
            return;
        }

        // Actually drain
        var extracted = ctx.extract(what, canInsert, Actionable.MODULATE);
        if (extracted != canInsert) {
            AELog.error(
                    "Fluid item [%s] reported a different possible amount to drain than it actually provided.",
                    getCarried());
            return;
        }

        if (sink.insert(what, extracted, Actionable.MODULATE) != extracted) {
            AELog.error("Failed to insert previously simulated %s into ME system", what);
            return;
        }

        ctx.playEmptySound(getPlayer(), what);
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
     * @return Returns the remainder.
     */
    protected ItemStack transferStackToMenu(ItemStack input) {
        return input;
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

    /**
     * Can be overridden in subclasses to be notified of GUI data updates sent by the server.
     */
    @OverridingMethodsMustInvokeSuper
    public void onServerDataSync() {
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

    public MenuLocator getLocator() {
        return this.locator;
    }

    public void setLocator(MenuLocator locator) {
        this.locator = locator;
    }

    /**
     * Returns whether this menu instance lives on the client.
     */
    public boolean isClientSide() {
        return getPlayer().getCommandSenderWorld().isClientSide();
    }

    /**
     * Returns whether this menu instance lives on the server.
     */
    protected boolean isServerSide() {
        return !isClientSide();
    }

    protected final void sendPacketToClient(BasePacket packet) {
        if (getPlayer() instanceof ServerPlayer serverPlayer) {
            NetworkHandler.instance().sendTo(packet, serverPlayer);
        }
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();

        if (dataSync.hasFields()) {
            sendPacketToClient(new GuiDataSyncPacket(containerId, dataSync::writeFull));
        }
    }

    /**
     * Receives data from the server for synchronizing fields of this class.
     */
    public final void receiveServerSyncData(GuiDataSyncPacket packet) {
        this.dataSync.readUpdate(packet.getData());
        this.onServerDataSync();
    }

    /**
     * Receives a menu action from the client.
     */
    public final void receiveClientAction(GuiDataSyncPacket packet) {
        FriendlyByteBuf data = packet.getData();
        String name = data.readUtf(256);

        ClientAction<?> action = clientActions.get(name);
        if (action == null) {
            throw new IllegalArgumentException("Unknown client action: '" + name + "'");
        }

        action.handle(data);
    }

    /**
     * Registers a handler for a client-initiated GUI action. Keep in mind in your handler that the client can send
     * arbitrary data. The given argument class will be serialized as JSON across the wire, so it must be GSON
     * serializable.
     */
    protected final <T> void registerClientAction(String name, Class<T> argClass, Consumer<T> handler) {
        if (clientActions.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate client action registered: " + name);
        }

        clientActions.put(name, new ClientAction<>(name, argClass, handler));
    }

    /**
     * Convenience function for registering a client action with no argument.
     *
     * @see #registerClientAction(String, Class, Consumer)
     */
    protected final void registerClientAction(String name, Runnable callback) {
        registerClientAction(name, Void.class, arg -> callback.run());
    }

    /**
     * Trigger a client action on the server.
     */
    protected final <T> void sendClientAction(String action, T arg) {
        ClientAction<?> clientAction = clientActions.get(action);
        if (clientAction == null) {
            throw new IllegalArgumentException("Trying to send unregistered client action: " + action);
        }

        // Serialize the argument to JSON for sending it in the packet
        String jsonPayload;
        if (clientAction.argClass == Void.class) {
            if (arg != null) {
                throw new IllegalArgumentException(
                        "Client action " + action + " requires no argument, but it was given");
            }
            jsonPayload = null;
        } else {
            if (arg == null) {
                throw new IllegalArgumentException(
                        "Client action " + action + " requires an argument, but none was given");
            }
            if (clientAction.argClass != arg.getClass()) {
                throw new IllegalArgumentException(
                        "Trying to send client action " + action + " with wrong argument type " + arg.getClass()
                                + ", expected: " + clientAction.argClass);
            }
            jsonPayload = clientAction.gson.toJson(arg);
        }

        // We do not allow for longer strings than 32kb
        if (jsonPayload != null && jsonPayload.length() > MAX_STRING_LENGTH) {
            throw new IllegalArgumentException(
                    "Cannot send client action " + action + " because serialized argument is longer than "
                            + MAX_STRING_LENGTH + " (" + jsonPayload.length() + ")");
        }

        NetworkHandler.instance().sendToServer(new GuiDataSyncPacket(containerId, writer -> {
            writer.writeUtf(clientAction.name);
            if (jsonPayload != null) {
                writer.writeUtf(jsonPayload);
            }
        }));
    }

    /**
     * Sends a previously registered client action to the server.
     */
    protected final void sendClientAction(String action) {
        sendClientAction(action, (Void) null);
    }

    /**
     * Registration for an action that a client can initiate.
     */
    private static class ClientAction<T> {
        private final Gson gson = new GsonBuilder().create();
        private final String name;
        private final Class<T> argClass;
        private final Consumer<T> handler;

        public ClientAction(String name, Class<T> argClass, Consumer<T> handler) {
            this.name = name;
            this.argClass = argClass;
            this.handler = handler;
        }

        public void handle(FriendlyByteBuf buffer) {
            T arg = null;
            if (argClass != Void.class) {
                String payload = buffer.readUtf();
                AELog.debug("Handling client action '%s' with payload %s", name, payload);
                arg = gson.fromJson(payload, argClass);
            } else {
                AELog.debug("Handling client action '%s'", name);
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
}
