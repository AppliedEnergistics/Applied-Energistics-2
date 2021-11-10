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

import javax.annotation.Nonnull;
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

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityService;
import appeng.api.parts.IPart;
import appeng.core.AELog;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.GuiDataSyncPacket;
import appeng.helpers.InventoryAction;
import appeng.me.helpers.PlayerSource;
import appeng.menu.guisync.DataSynchronization;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.CraftingMatrixSlot;
import appeng.menu.slot.CraftingTermSlot;
import appeng.menu.slot.DisabledSlot;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.InaccessibleSlot;

public abstract class AEBaseMenu extends AbstractContainerMenu {
    private static final int MAX_STRING_LENGTH = 32767;

    private final IActionSource mySrc;
    private final BlockEntity blockEntity;
    private final IPart part;
    private final IGuiItemObject guiItem;
    private final DataSynchronization dataSync = new DataSynchronization(this);
    private final Inventory playerInventory;
    private final Set<Integer> lockedPlayerInventorySlots = new HashSet<>();
    private final Map<Slot, SlotSemantic> semanticBySlot = new HashMap<>();
    private final ArrayListMultimap<SlotSemantic, Slot> slotsBySemantic = ArrayListMultimap.create();
    private final Map<String, ClientAction<?>> clientActions = new HashMap<>();
    private boolean menuValid = true;
    private MenuLocator locator;
    private int ticksSinceCheck = 900;

    public AEBaseMenu(MenuType<?> menuType, int id, final Inventory playerInventory,
            final Object host) {
        super(menuType, id);
        this.playerInventory = playerInventory;
        this.blockEntity = host instanceof BlockEntity ? (BlockEntity) host : null;
        this.part = host instanceof IPart ? (IPart) host : null;
        this.guiItem = host instanceof IGuiItemObject ? (IGuiItemObject) host : null;

        if (host != null && this.blockEntity == null && this.part == null && this.guiItem == null) {
            throw new IllegalArgumentException("Must have a valid host, instead " + host + " in " + playerInventory);
        }

        this.mySrc = new PlayerSource(getPlayer(), this.getActionHost());
    }

    protected final IActionHost getActionHost() {
        if (this.guiItem instanceof IActionHost) {
            return (IActionHost) this.guiItem;
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
        return this.guiItem instanceof IActionHost
                || this.blockEntity instanceof IActionHost
                || this.part instanceof IActionHost;
    }

    public boolean isRemote() {
        return getPlayer().getCommandSenderWorld().isClientSide();
    }

    /**
     * Convenience method to get the player owning this menu.
     */
    @Nonnull
    public Player getPlayer() {
        return getPlayerInventory().player;
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
        this.setValidMenu(this.isValidMenu() && this.hasAccess(security, requirePower));
    }

    protected boolean hasAccess(final SecurityPermissions perm, final boolean requirePower) {
        if (!isActionHost() && !requirePower) {
            return true; // Hosts that are not grid connected always give access
        }

        var host = this.getActionHost();

        if (host != null) {
            final IGridNode gn = host.getActionableNode();
            if (gn != null) {
                final IGrid g = gn.getGrid();
                if (g != null) {
                    if (requirePower) {
                        final IEnergyService eg = g.getEnergyService();
                        if (!eg.isNetworkPowered()) {
                            return false;
                        }
                    }

                    final ISecurityService sg = g.getSecurityService();
                    if (sg.hasPermission(this.getPlayerInventory().player, perm)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public Inventory getPlayerInventory() {
        return this.playerInventory;
    }

    public void lockPlayerInventorySlot(final int invSlot) {
        Preconditions.checkArgument(invSlot >= 0 && invSlot < playerInventory.items.size(),
                "cannot lock player inventory slot: %s", invSlot);
        this.lockedPlayerInventorySlots.add(invSlot);
    }

    public Object getTarget() {
        if (this.blockEntity != null) {
            return this.blockEntity;
        }
        if (this.part != null) {
            return this.part;
        }
        return this.guiItem;
    }

    public BlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    protected final void createPlayerInventorySlots(Inventory playerInventory) {
        Preconditions.checkState(
                getSlots(SlotSemantic.PLAYER_INVENTORY).isEmpty(),
                "Player inventory was already created");

        for (int i = 0; i < playerInventory.items.size(); i++) {
            Slot slot;
            if (this.lockedPlayerInventorySlots.contains(i)) {
                slot = new DisabledSlot(playerInventory, i);
            } else {
                slot = new Slot(playerInventory, i, 0, 0);
            }
            SlotSemantic s = i < Inventory.getSelectionSize()
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
        if (newSlot instanceof AppEngSlot s) {
            s.setMenu(this);
        }
        return super.addSlot(newSlot);
    }

    @Override
    public void broadcastChanges() {
        if (isServer()) {
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
        return slotSemantic == SlotSemantic.PLAYER_INVENTORY
                || slotSemantic == SlotSemantic.PLAYER_HOTBAR
                // The crafting grid in the crafting terminal also shift-clicks into the network
                || slotSemantic == SlotSemantic.CRAFTING_GRID;
    }

    @Override
    public ItemStack quickMoveStack(final Player p, final int idx) {
        if (isRemote()) {
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
                    for (final Slot cs : this.slots) {
                        if (!isPlayerSideSlot(cs) && !(cs instanceof FakeSlot) && !(cs instanceof CraftingMatrixSlot)
                                && cs.mayPlace(tis)) {
                            selectedSlots.add(cs);
                        }
                    }
                }
            } else {
                tis = tis.copy();

                // target slots in the menu...
                for (final Slot cs : this.slots) {
                    if (isPlayerSideSlot(cs) && !(cs instanceof FakeSlot) && !(cs instanceof CraftingMatrixSlot)
                            && cs.mayPlace(tis)) {
                        selectedSlots.add(cs);
                    }
                }
            }

            // Handle Fake Slot Shift clicking.
            if (selectedSlots.isEmpty() && playerSide && !tis.isEmpty()) {
                // target slots in the menu...
                for (final Slot cs : this.slots) {
                    final ItemStack destination = cs.getItem();

                    if (!isPlayerSideSlot(cs) && cs instanceof FakeSlot) {
                        if (ItemStack.isSameItemSameTags(destination, tis)) {
                            break;
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
                for (final Slot d : selectedSlots) {
                    if (d.mayPlace(tis) && d.hasItem() && x(clickSlot, tis, d)) {
                        return ItemStack.EMPTY;
                    }
                }

                // FIXME figure out whats the difference between this and the one above ?!
                // any match..
                for (final Slot d : selectedSlots) {
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
    public boolean stillValid(final Player PlayerEntity) {
        if (this.isValidMenu()) {
            if (this.blockEntity instanceof Container) {
                return ((Container) this.blockEntity).stillValid(PlayerEntity);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canDragTo(final Slot s) {
        if (s instanceof AppEngSlot) {
            return ((AppEngSlot) s).isDraggable();
        } else {
            return super.canDragTo(s);
        }
    }

    /**
     * Sets a filter slot based on a <b>non-existent</b> item sent by the client.
     */
    public void setFilter(final int slotIndex, ItemStack item) {
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

    public void doAction(final ServerPlayer player, final InventoryAction action, final int slot, final long id) {
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

        if (s instanceof FakeSlot) {
            var hand = getCarried();

            switch (action) {
                case PICKUP_OR_SET_DOWN:

                    if (hand.isEmpty()) {
                        s.set(ItemStack.EMPTY);
                    } else {
                        s.set(hand.copy());
                    }

                    break;
                case PLACE_SINGLE:

                    if (!hand.isEmpty()) {
                        final ItemStack is = hand.copy();
                        is.setCount(1);
                        s.set(is);
                    }

                    break;
                case SPLIT_OR_PLACE_SINGLE:

                    ItemStack is = s.getItem();
                    if (!is.isEmpty()) {
                        if (hand.isEmpty()) {
                            is.setCount(Math.max(1, is.getCount() - 1));
                        } else if (hand.sameItem(is)) {
                            is.setCount(Math.min(is.getMaxStackSize(), is.getCount() + 1));
                        } else {
                            is = hand.copy();
                            is.setCount(1);
                        }

                        s.set(is);
                    } else if (!hand.isEmpty()) {
                        is = hand.copy();
                        is.setCount(1);
                        s.set(is);
                    }

                    break;
                case CREATIVE_DUPLICATE:
                case MOVE_REGION:
                case SHIFT_CLICK:
                default:
                    break;
            }

            // No further interaction allowed with fake slots
            return;
        }

        if (action == InventoryAction.MOVE_REGION) {
            final List<Slot> from = new ArrayList<>();

            for (final Slot j : this.slots) {
                if (j != null && j.getClass() == s.getClass() && !(j instanceof CraftingTermSlot)) {
                    from.add(j);
                }
            }

            for (final Slot fr : from) {
                this.quickMoveStack(player, fr.index);
            }
        }

    }

    protected ItemStack transferStackToMenu(final ItemStack input) {
        return input;
    }

    public void swapSlotContents(final int slotA, final int slotB) {
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

    public void onSlotChange(final Slot s) {

    }

    public boolean isValidForSlot(final Slot s, final ItemStack i) {
        return true;
    }

    public boolean isValidMenu() {
        return this.menuValid;
    }

    public void setValidMenu(final boolean isContainerValid) {
        this.menuValid = isContainerValid;
    }

    public MenuLocator getLocator() {
        return this.locator;
    }

    public void setLocator(final MenuLocator locator) {
        this.locator = locator;
    }

    /**
     * Returns whether this menu instance lives on the client.
     */
    protected boolean isClient() {
        return getPlayer().getCommandSenderWorld().isClientSide();
    }

    /**
     * Returns whether this menu instance lives on the server.
     */
    protected boolean isServer() {
        return !isClient();
    }

    protected final void sendPacketToClient(BasePacket packet) {
        if (getPlayer() instanceof ServerPlayer serverPlayer) {
            NetworkHandler.instance().sendTo(packet, serverPlayer);
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

        ItemStack currentItem = this.getPlayerInventory().getItem(slot);
        if (!currentItem.isEmpty() && !expectedItem.isEmpty()) {
            if (currentItem == expectedItem) {
                return true;
            } else if (ItemStack.isSame(expectedItem, currentItem)) {
                // If the items are still equivalent, we just restore referential equality so that modifications
                // to the GUI item are reflected in the slot
                this.getPlayerInventory().setItem(slot, expectedItem);
                return true;
            }
        }

        return false;
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

}
