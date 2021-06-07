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

package appeng.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.IPart;
import appeng.container.guisync.DataSynchronization;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.CraftingMatrixSlot;
import appeng.container.slot.CraftingTermSlot;
import appeng.container.slot.DisabledSlot;
import appeng.container.slot.FakeSlot;
import appeng.container.slot.InaccessibleSlot;
import appeng.core.AELog;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.GuiDataSyncPacket;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.me.helpers.PlayerSource;
import appeng.util.Platform;

public abstract class AEBaseContainer extends Container {
    private final IActionSource mySrc;
    private final TileEntity tileEntity;
    private final IPart part;
    private final IGuiItemObject guiItem;
    private final DataSynchronization dataSync = new DataSynchronization(this);
    private final PlayerInventory playerInventory;
    private final Set<Integer> lockedPlayerInventorySlots = new HashSet<>();
    private final Map<Slot, SlotSemantic> semanticBySlot = new HashMap<>();
    private final ArrayListMultimap<SlotSemantic, Slot> slotsBySemantic = ArrayListMultimap.create();
    private final Map<String, ClientAction<?>> clientActions = new HashMap<>();
    private boolean isContainerValid = true;
    private ContainerLocator locator;
    private int ticksSinceCheck = 900;

    public AEBaseContainer(ContainerType<?> containerType, int id, final PlayerInventory playerInventory,
            final Object host) {
        super(containerType, id);
        this.playerInventory = playerInventory;
        this.tileEntity = host instanceof TileEntity ? (TileEntity) host : null;
        this.part = host instanceof IPart ? (IPart) host : null;
        this.guiItem = host instanceof IGuiItemObject ? (IGuiItemObject) host : null;

        if (host != null && this.tileEntity == null && this.part == null && this.guiItem == null) {
            throw new IllegalArgumentException("Must have a valid host, instead " + host + " in " + playerInventory);
        }

        this.mySrc = new PlayerSource(playerInventory.player, this.getActionHost());
    }

    protected IActionHost getActionHost() {
        if (this.guiItem instanceof IActionHost) {
            return (IActionHost) this.guiItem;
        }

        if (this.tileEntity instanceof IActionHost) {
            return (IActionHost) this.tileEntity;
        }

        if (this.part instanceof IActionHost) {
            return (IActionHost) this.part;
        }

        return null;
    }

    public boolean isRemote() {
        return this.playerInventory.player.getEntityWorld().isRemote();
    }

    public IActionSource getActionSource() {
        return this.mySrc;
    }

    public void verifyPermissions(final SecurityPermissions security, final boolean requirePower) {
        if (isRemote()) {
            return;
        }

        this.ticksSinceCheck++;
        if (this.ticksSinceCheck < 20) {
            return;
        }

        this.ticksSinceCheck = 0;
        this.setValidContainer(this.isValidContainer() && this.hasAccess(security, requirePower));
    }

    protected boolean hasAccess(final SecurityPermissions perm, final boolean requirePower) {
        final IActionHost host = this.getActionHost();

        if (host != null) {
            final IGridNode gn = host.getActionableNode();
            if (gn != null) {
                final IGrid g = gn.getGrid();
                if (g != null) {
                    if (requirePower) {
                        final IEnergyGrid eg = g.getCache(IEnergyGrid.class);
                        if (!eg.isNetworkPowered()) {
                            return false;
                        }
                    }

                    final ISecurityGrid sg = g.getCache(ISecurityGrid.class);
                    if (sg.hasPermission(this.getPlayerInventory().player, perm)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public PlayerInventory getPlayerInventory() {
        return this.playerInventory;
    }

    public void lockPlayerInventorySlot(final int invSlot) {
        Preconditions.checkArgument(invSlot >= 0 && invSlot < playerInventory.mainInventory.size(),
                "cannot lock player inventory slot: %s", invSlot);
        this.lockedPlayerInventorySlots.add(invSlot);
    }

    public Object getTarget() {
        if (this.tileEntity != null) {
            return this.tileEntity;
        }
        if (this.part != null) {
            return this.part;
        }
        return this.guiItem;
    }

    public TileEntity getTileEntity() {
        return this.tileEntity;
    }

    protected final void createPlayerInventorySlots(PlayerInventory playerInventory) {
        Preconditions.checkState(
                getSlots(SlotSemantic.PLAYER_INVENTORY).isEmpty(),
                "Player inventory was already created");

        IItemHandler ih = new PlayerInvWrapper(playerInventory);

        for (int i = 0; i < playerInventory.mainInventory.size(); i++) {
            Slot slot;
            if (this.lockedPlayerInventorySlots.contains(i)) {
                slot = new DisabledSlot(ih, i);
            } else {
                slot = new Slot(playerInventory, i, 0, 0);
            }
            SlotSemantic s = i < PlayerInventory.getHotbarSize()
                    ? SlotSemantic.PLAYER_HOTBAR
                    : SlotSemantic.PLAYER_INVENTORY;
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

    public List<Slot> getSlots(SlotSemantic semantic) {
        return slotsBySemantic.get(semantic);
    }

    @Override
    protected Slot addSlot(final Slot newSlot) {
        if (newSlot instanceof AppEngSlot) {
            final AppEngSlot s = (AppEngSlot) newSlot;
            s.setContainer(this);
        }
        return super.addSlot(newSlot);
    }

    @Override
    public void detectAndSendChanges() {
        if (isServer()) {
            if (this.tileEntity != null
                    && this.tileEntity.getWorld().getTileEntity(this.tileEntity.getPos()) != this.tileEntity) {
                this.setValidContainer(false);
            }

            if (dataSync.hasChanges()) {
                sendPacketToClient(new GuiDataSyncPacket(windowId, dataSync::writeUpdate));
            }
        }

        super.detectAndSendChanges();
    }

    /**
     * Check if a given slot is considered to be "on the player side" for the purposes of shift-clicking items back and
     * forth between the opened container and the player's inventory.
     */
    private boolean isPlayerSideSlot(Slot slot) {
        if (slot.inventory == playerInventory) {
            return true;
        }

        SlotSemantic slotSemantic = semanticBySlot.get(slot);
        return slotSemantic == SlotSemantic.PLAYER_INVENTORY
                || slotSemantic == SlotSemantic.PLAYER_HOTBAR
                // The crafting grid in the crafting terminal also shift-clicks into the network
                || slotSemantic == SlotSemantic.CRAFTING_GRID;
    }

    @Override
    public ItemStack transferStackInSlot(final PlayerEntity p, final int idx) {
        if (isRemote()) {
            return ItemStack.EMPTY;
        }

        final Slot clickSlot = this.inventorySlots.get(idx);
        boolean playerSide = isPlayerSideSlot(clickSlot);

        if (clickSlot instanceof DisabledSlot || clickSlot instanceof InaccessibleSlot) {
            return ItemStack.EMPTY;
        }
        if (clickSlot.getHasStack()) {
            ItemStack tis = clickSlot.getStack();

            if (tis.isEmpty()) {
                return ItemStack.EMPTY;
            }

            final List<Slot> selectedSlots = new ArrayList<>();

            // Gather a list of valid destinations.
            if (playerSide) {
                tis = this.transferStackToContainer(tis);

                if (!tis.isEmpty()) {
                    // target slots in the container...
                    for (final Slot cs : this.inventorySlots) {
                        if (!isPlayerSideSlot(cs) && !(cs instanceof FakeSlot) && !(cs instanceof CraftingMatrixSlot)
                                && cs.isItemValid(tis)) {
                            selectedSlots.add(cs);
                        }
                    }
                }
            } else {
                tis = tis.copy();

                // target slots in the container...
                for (final Slot cs : this.inventorySlots) {
                    if (isPlayerSideSlot(cs) && !(cs instanceof FakeSlot) && !(cs instanceof CraftingMatrixSlot)
                            && cs.isItemValid(tis)) {
                        selectedSlots.add(cs);
                    }
                }
            }

            // Handle Fake Slot Shift clicking.
            if (selectedSlots.isEmpty() && playerSide && !tis.isEmpty()) {
                // target slots in the container...
                for (final Slot cs : this.inventorySlots) {
                    final ItemStack destination = cs.getStack();

                    if (!isPlayerSideSlot(cs) && cs instanceof FakeSlot) {
                        if (Platform.itemComparisons().isSameItem(destination, tis)) {
                            break;
                        } else if (destination.isEmpty()) {
                            cs.putStack(tis.copy());
                            // ???
                            this.detectAndSendChanges();
                            break;
                        }
                    }
                }
            }

            if (!tis.isEmpty()) {
                // find slots to stack the item into
                for (final Slot d : selectedSlots) {
                    if (d.isItemValid(tis) && d.getHasStack() && x(clickSlot, tis, d)) {
                        return ItemStack.EMPTY;
                    }
                }

                // FIXME figure out whats the difference between this and the one above ?!
                // any match..
                for (final Slot d : selectedSlots) {
                    if (d.isItemValid(tis)) {
                        if (d.getHasStack()) {
                            if (x(clickSlot, tis, d)) {
                                return ItemStack.EMPTY;
                            }
                        } else {
                            int maxSize = tis.getMaxStackSize();
                            if (maxSize > d.getSlotStackLimit()) {
                                maxSize = d.getSlotStackLimit();
                            }

                            final ItemStack tmp = tis.copy();
                            if (tmp.getCount() > maxSize) {
                                tmp.setCount(maxSize);
                            }

                            tis.setCount(tis.getCount() - tmp.getCount());
                            d.putStack(tmp);

                            if (tis.getCount() <= 0) {
                                clickSlot.putStack(ItemStack.EMPTY);
                                d.onSlotChanged();

                                this.detectAndSendChanges();
                                return ItemStack.EMPTY;
                            } else {
                                this.detectAndSendChanges();
                            }
                        }
                    }
                }
            }

            clickSlot.putStack(!tis.isEmpty() ? tis : ItemStack.EMPTY);
        }

        // ???
        this.detectAndSendChanges();
        return ItemStack.EMPTY;
    }

    private boolean x(Slot clickSlot, ItemStack tis, Slot d) {
        final ItemStack t = d.getStack().copy();

        if (Platform.itemComparisons().isSameItem(t, tis)) {
            int maxSize = t.getMaxStackSize();
            if (maxSize > d.getSlotStackLimit()) {
                maxSize = d.getSlotStackLimit();
            }

            int placeable = maxSize - t.getCount();
            if (placeable > 0) {
                if (tis.getCount() < placeable) {
                    placeable = tis.getCount();
                }

                t.setCount(t.getCount() + placeable);
                tis.setCount(tis.getCount() - placeable);

                d.putStack(t);

                if (tis.getCount() <= 0) {
                    clickSlot.putStack(ItemStack.EMPTY);
                    d.onSlotChanged();

                    // ???
                    this.detectAndSendChanges();
                    // ???
                    this.detectAndSendChanges();
                    return true;
                } else {
                    // ???
                    this.detectAndSendChanges();
                }
            }
        }
        return false;
    }

    @Override
    public boolean canInteractWith(final PlayerEntity PlayerEntity) {
        if (this.isValidContainer()) {
            if (this.tileEntity instanceof IInventory) {
                return ((IInventory) this.tileEntity).isUsableByPlayer(PlayerEntity);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canDragIntoSlot(final Slot s) {
        if (s instanceof AppEngSlot) {
            return ((AppEngSlot) s).isDraggable();
        } else {
            return super.canDragIntoSlot(s);
        }
    }

    /**
     * Sets a filter slot based on a <b>non-existent</b> item sent by the client.
     */
    public void setFilter(final int slotIndex, ItemStack item) {
        if (slotIndex < 0 || slotIndex >= this.inventorySlots.size()) {
            return;
        }
        final Slot s = this.getSlot(slotIndex);
        if (!(s instanceof AppEngSlot)) {
            return;
        }
        AppEngSlot appEngSlot = (AppEngSlot) s;
        if (!appEngSlot.isSlotEnabled()) {
            return;
        }

        if (s instanceof FakeSlot) {
            s.putStack(item);
        }
    }

    public void doAction(final ServerPlayerEntity player, final InventoryAction action, final int slot, final long id) {
        if (slot < 0 || slot >= this.inventorySlots.size()) {
            return;
        }
        final Slot s = this.getSlot(slot);

        if (s instanceof CraftingTermSlot) {
            switch (action) {
                case CRAFT_SHIFT:
                case CRAFT_ITEM:
                case CRAFT_STACK:
                    ((CraftingTermSlot) s).doClick(action, player);
                    this.updateHeld(player);
                default:
            }
        }

        if (s instanceof FakeSlot) {
            final ItemStack hand = player.inventory.getItemStack();

            switch (action) {
                case PICKUP_OR_SET_DOWN:

                    if (hand.isEmpty()) {
                        s.putStack(ItemStack.EMPTY);
                    } else {
                        s.putStack(hand.copy());
                    }

                    break;
                case PLACE_SINGLE:

                    if (!hand.isEmpty()) {
                        final ItemStack is = hand.copy();
                        is.setCount(1);
                        s.putStack(is);
                    }

                    break;
                case SPLIT_OR_PLACE_SINGLE:

                    ItemStack is = s.getStack();
                    if (!is.isEmpty()) {
                        if (hand.isEmpty()) {
                            is.setCount(Math.max(1, is.getCount() - 1));
                        } else if (hand.isItemEqual(is)) {
                            is.setCount(Math.min(is.getMaxStackSize(), is.getCount() + 1));
                        } else {
                            is = hand.copy();
                            is.setCount(1);
                        }

                        s.putStack(is);
                    } else if (!hand.isEmpty()) {
                        is = hand.copy();
                        is.setCount(1);
                        s.putStack(is);
                    }

                    break;
                case CREATIVE_DUPLICATE:
                case MOVE_REGION:
                case SHIFT_CLICK:
                default:
                    break;
            }
        }

        if (action == InventoryAction.MOVE_REGION) {
            final List<Slot> from = new ArrayList<>();

            for (final Slot j : this.inventorySlots) {
                if (j != null && j.getClass() == s.getClass() && !(j instanceof CraftingTermSlot)) {
                    from.add(j);
                }
            }

            for (final Slot fr : from) {
                this.transferStackInSlot(player, fr.slotNumber);
            }
        }

    }

    protected void updateHeld(final ServerPlayerEntity p) {
        NetworkHandler.instance().sendTo(new InventoryActionPacket(InventoryAction.UPDATE_HAND, 0,
                p.inventory.getItemStack()), p);
    }

    protected ItemStack transferStackToContainer(final ItemStack input) {
        return input;
    }

    public void swapSlotContents(final int slotA, final int slotB) {
        final Slot a = this.getSlot(slotA);
        final Slot b = this.getSlot(slotB);

        // NPE protection...
        if (a == null || b == null) {
            return;
        }

        final ItemStack isA = a.getStack();
        final ItemStack isB = b.getStack();

        // something to do?
        if (isA.isEmpty() && isB.isEmpty()) {
            return;
        }

        // can take?

        if (!isA.isEmpty() && !a.canTakeStack(this.getPlayerInventory().player)) {
            return;
        }

        if (!isB.isEmpty() && !b.canTakeStack(this.getPlayerInventory().player)) {
            return;
        }

        // swap valid?

        if (!isB.isEmpty() && !a.isItemValid(isB)) {
            return;
        }

        if (!isA.isEmpty() && !b.isItemValid(isA)) {
            return;
        }

        ItemStack testA = isB.isEmpty() ? ItemStack.EMPTY : isB.copy();
        ItemStack testB = isA.isEmpty() ? ItemStack.EMPTY : isA.copy();

        // can put some back?
        if (!testA.isEmpty() && testA.getCount() > a.getSlotStackLimit()) {
            if (!testB.isEmpty()) {
                return;
            }

            final int totalA = testA.getCount();
            testA.setCount(a.getSlotStackLimit());
            testB = testA.copy();

            testB.setCount(totalA - testA.getCount());
        }

        if (!testB.isEmpty() && testB.getCount() > b.getSlotStackLimit()) {
            if (!testA.isEmpty()) {
                return;
            }

            final int totalB = testB.getCount();
            testB.setCount(b.getSlotStackLimit());
            testA = testB.copy();

            testA.setCount(totalB - testA.getCount());
        }

        a.putStack(testA);
        b.putStack(testB);
    }

    /**
     * Can be overridden in subclasses to be notified of GUI data updates sent by the server.
     */
    @OverridingMethodsMustInvokeSuper
    public void onServerDataSync() {
    }

    public void onSlotChange(final Slot s) {

    }

    public boolean isValidForSlot(final Slot s, final ItemStack i) {
        return true;
    }

    public boolean isValidContainer() {
        return this.isContainerValid;
    }

    public void setValidContainer(final boolean isContainerValid) {
        this.isContainerValid = isContainerValid;
    }

    public ContainerLocator getLocator() {
        return this.locator;
    }

    public void setLocator(final ContainerLocator locator) {
        this.locator = locator;
    }

    /**
     * Returns whether this container instance lives on the client.
     */
    protected boolean isClient() {
        return playerInventory.player.getEntityWorld().isRemote();
    }

    /**
     * Returns whether this container instance lives on the server.
     */
    protected boolean isServer() {
        return !isClient();
    }

    protected final void sendPacketToClient(BasePacket packet) {
        for (IContainerListener c : this.listeners) {
            if (c instanceof ServerPlayerEntity) {
                NetworkHandler.instance().sendTo(packet, (ServerPlayerEntity) c);
            }
        }
    }

    /**
     * Ensures that the item stack referenced by a given GUI item object is still in the expected player inventory slot.
     * If necessary, referential equality is restored by overwriting the item in the player inventory if it is equal to
     * the expected item.
     *
     * @return True if {@link IGuiItemObject#getItemStack()} is still in the expected slot.
     */
    protected final boolean ensureGuiItemIsInSlot(IGuiItemObject guiObject, int slot) {
        ItemStack expectedItem = guiObject.getItemStack();

        ItemStack currentItem = this.getPlayerInventory().getStackInSlot(slot);
        if (!currentItem.isEmpty() && !expectedItem.isEmpty()) {
            if (currentItem == expectedItem) {
                return true;
            } else if (ItemStack.areItemsEqual(expectedItem, currentItem)) {
                // If the items are still equivalent, we just restore referential equality so that modifications
                // to the GUI item are reflected in the slot
                this.getPlayerInventory().setInventorySlotContents(slot, expectedItem);
                return true;
            }
        }

        return false;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);

        // The first listener that is added is our opportunity to send the initial data packet, since
        // this happens after the OpenContainer packet has been sent to the client, but before any other
        // processing continues.
        if (listener instanceof ServerPlayerEntity && dataSync.hasFields()) {
            sendPacketToClient(new GuiDataSyncPacket(windowId, dataSync::writeFull));
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
     * Receives a container action from the client.
     */
    public final void receiveClientAction(GuiDataSyncPacket packet) {
        PacketBuffer data = packet.getData();
        String name = data.readString(256);

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

        // We do not allow for longer strings than BasePacket.MAX_STRING_LENGTH
        if (jsonPayload != null && jsonPayload.length() > BasePacket.MAX_STRING_LENGTH) {
            throw new IllegalArgumentException(
                    "Cannot send client action " + action + " because serialized argument is longer than "
                            + BasePacket.MAX_STRING_LENGTH + " (" + jsonPayload.length() + ")");
        }

        NetworkHandler.instance().sendToServer(new GuiDataSyncPacket(windowId, writer -> {
            writer.writeString(clientAction.name);
            if (jsonPayload != null) {
                writer.writeString(jsonPayload);
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

        public void handle(PacketBuffer buffer) {
            T arg = null;
            if (argClass != Void.class) {
                String payload = buffer.readString(BasePacket.MAX_STRING_LENGTH);
                AELog.debug("Handling client action '%s' with payload %s", name, payload);
                arg = gson.fromJson(payload, argClass);
            } else {
                AELog.debug("Handling client action '%s'", name);
            }

            this.handler.accept(arg);
        }
    }

}
