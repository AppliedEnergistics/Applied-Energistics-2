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

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.IPart;
import appeng.container.guisync.GuiSync;
import appeng.container.guisync.SyncData;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.CraftingMatrixSlot;
import appeng.container.slot.CraftingTermSlot;
import appeng.container.slot.DisabledSlot;
import appeng.container.slot.FakeSlot;
import appeng.container.slot.InaccessibleSlot;
import appeng.container.slot.PlayerInvSlot;
import appeng.core.AELog;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.me.helpers.PlayerSource;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AEBaseContainer extends Container {
    private final IActionSource mySrc;
    private final TileEntity tileEntity;
    private final IPart part;
    private final IGuiItemObject guiItem;
    private final HashMap<Integer, SyncData> syncData = new HashMap<>();
    private final PlayerInventory playerInventory;
    private final List<AppEngSlot> playerInventorySlots = new ArrayList<>();
    private final Set<Integer> lockedPlayerInventorySlots = new HashSet<>();
    private final Map<Slot, SlotSemantic> semanticBySlot = new HashMap<>();
    private final ArrayListMultimap<SlotSemantic, Slot> slotsBySemantic = ArrayListMultimap.create();
    private boolean isContainerValid = true;
    private ContainerLocator locator;
    private int ticksSinceCheck = 900;

    public AEBaseContainer(ContainerType<?> containerType, int id, final PlayerInventory ip, final TileEntity myTile,
                           final IPart myPart) {
        this(containerType, id, ip, myTile, myPart, null);
    }

    public AEBaseContainer(ContainerType<?> containerType, int id, final PlayerInventory playerInventory,
                           final TileEntity myTile,
                           final IPart myPart, final IGuiItemObject gio) {
        super(containerType, id);
        this.playerInventory = playerInventory;
        this.tileEntity = myTile;
        this.part = myPart;
        this.guiItem = gio;
        this.mySrc = new PlayerSource(playerInventory.player, this.getActionHost());
        this.prepareSync();
    }

    public AEBaseContainer(ContainerType<?> containerType, int id, final PlayerInventory playerInventory,
                           final Object host) {
        super(containerType, id);
        this.playerInventory = playerInventory;
        this.tileEntity = host instanceof TileEntity ? (TileEntity) host : null;
        this.part = host instanceof IPart ? (IPart) host : null;
        this.guiItem = host instanceof IGuiItemObject ? (IGuiItemObject) host : null;

        if (this.tileEntity == null && this.part == null && this.guiItem == null) {
            throw new IllegalArgumentException("Must have a valid host, instead " + host + " in " + playerInventory);
        }

        this.mySrc = new PlayerSource(playerInventory.player, this.getActionHost());
        this.prepareSync();
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

    private void prepareSync() {
        for (final Field f : this.getClass().getFields()) {
            if (f.isAnnotationPresent(GuiSync.class)) {
                final GuiSync annotation = f.getAnnotation(GuiSync.class);
                if (this.syncData.containsKey(annotation.value())) {
                    AELog.warn("Channel already in use: " + annotation.value() + " for " + f.getName());
                } else {
                    this.syncData.put(annotation.value(), new SyncData(this, f, annotation));
                }
            }
        }
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

    public List<AppEngSlot> getPlayerInventorySlots() {
        return playerInventorySlots;
    }

    public void lockPlayerInventorySlot(final int invSlot) {
        Preconditions.checkArgument(invSlot >= 0 && invSlot < playerInventory.mainInventory.size(),
                "cannot lock plaer inventory slot: %s", invSlot);
        this.lockedPlayerInventorySlots.add(invSlot);
    }

    public Object getTarget() {
        if (this.tileEntity != null) {
            return this.tileEntity;
        }
        if (this.part != null) {
            return this.part;
        }
        if (this.guiItem != null) {
            return this.guiItem;
        }
        return null;
    }

    public TileEntity getTileEntity() {
        return this.tileEntity;
    }

    public final void updateFullProgressBar(final int idx, final long value) {
        if (this.syncData.containsKey(idx)) {
            this.syncData.get(idx).update(value);
            return;
        }

        this.updateProgressBar(idx, (int) value);
    }

    public void stringSync(final int idx, final String value) {
        if (this.syncData.containsKey(idx)) {
            this.syncData.get(idx).update(value);
        }
    }

    protected final void createPlayerInventorySlots(PlayerInventory playerInventory) {
        Preconditions.checkState(playerInventorySlots.isEmpty(), "Player inventory was already created");

        IItemHandler ih = new PlayerInvWrapper(playerInventory);

        for (int i = 0; i < playerInventory.mainInventory.size(); i++) {
            AppEngSlot slot;
            if (this.lockedPlayerInventorySlots.contains(i)) {
                slot = new DisabledSlot(ih, i);
            } else {
                slot = new PlayerInvSlot(ih, i);
            }
            playerInventorySlots.add(slot);
            SlotSemantic s = i < PlayerInventory.getHotbarSize()
                    ? SlotSemantic.PLAYER_HOTBAR : SlotSemantic.PLAYER_INVENTORY;
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
            return super.addSlot(newSlot);
        } else {
            throw new IllegalArgumentException(
                    "Invalid Slot [" + newSlot + "] for AE Container instead of AppEngSlot.");
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (isServer()) {
            if (this.tileEntity != null
                    && this.tileEntity.getWorld().getTileEntity(this.tileEntity.getPos()) != this.tileEntity) {
                this.setValidContainer(false);
            }

            for (final IContainerListener listener : this.listeners) {
                for (final SyncData sd : this.syncData.values()) {
                    sd.tick(listener);
                }
            }
        }

        super.detectAndSendChanges();
    }

    @Override
    public ItemStack transferStackInSlot(final PlayerEntity p, final int idx) {
        if (isRemote()) {
            return ItemStack.EMPTY;
        }

        final AppEngSlot clickSlot = (AppEngSlot) this.inventorySlots.get(idx); // require AE Slots!

        if (clickSlot instanceof DisabledSlot || clickSlot instanceof InaccessibleSlot) {
            return ItemStack.EMPTY;
        }
        if (clickSlot != null && clickSlot.getHasStack()) {
            ItemStack tis = clickSlot.getStack();

            if (tis.isEmpty()) {
                return ItemStack.EMPTY;
            }

            final List<Slot> selectedSlots = new ArrayList<>();

            // Gather a list of valid destinations.
            if (clickSlot.isPlayerSide()) {
                tis = this.transferStackToContainer(tis);

                if (!tis.isEmpty()) {
                    // target slots in the container...
                    for (final Object inventorySlot : this.inventorySlots) {
                        final AppEngSlot cs = (AppEngSlot) inventorySlot;

                        if (!(cs.isPlayerSide()) && !(cs instanceof FakeSlot) && !(cs instanceof CraftingMatrixSlot)) {
                            if (cs.isItemValid(tis)) {
                                selectedSlots.add(cs);
                            }
                        }
                    }
                }
            } else {
                tis = tis.copy();

                // target slots in the container...
                for (final Object inventorySlot : this.inventorySlots) {
                    final AppEngSlot cs = (AppEngSlot) inventorySlot;

                    if ((cs.isPlayerSide()) && !(cs instanceof FakeSlot) && !(cs instanceof CraftingMatrixSlot)) {
                        if (cs.isItemValid(tis)) {
                            selectedSlots.add(cs);
                        }
                    }
                }
            }

            // Handle Fake Slot Shift clicking.
            if (selectedSlots.isEmpty() && clickSlot.isPlayerSide()) {
                if (!tis.isEmpty()) {
                    // target slots in the container...
                    for (final Object inventorySlot : this.inventorySlots) {
                        final AppEngSlot cs = (AppEngSlot) inventorySlot;
                        final ItemStack destination = cs.getStack();

                        if (!(cs.isPlayerSide()) && cs instanceof FakeSlot) {
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
            }

            if (!tis.isEmpty()) {
                // find slots to stack the item into
                for (final Slot d : selectedSlots) {
                    if (d.isItemValid(tis)) {
                        if (d.getHasStack()) {
                            if (x(clickSlot, tis, d)) {
                                return ItemStack.EMPTY;
                            }
                        }
                    }
                }

                // FIXME figure out whats the difference between this and the one above ?!
                // any match..
                for (final Slot d : selectedSlots) {
                    if (d.isItemValid(tis)) {
                        if (d.getHasStack()) {
                            if (x(clickSlot, tis, d))
                                return ItemStack.EMPTY;
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

    private boolean x(AppEngSlot clickSlot, ItemStack tis, Slot d) {
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
    public final void updateProgressBar(final int idx, final int value) {
        if (this.syncData.containsKey(idx)) {
            this.syncData.get(idx).update((long) value);
        }
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
        return ((AppEngSlot) s).isDraggable();
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
                AEItemStack.fromItemStack(p.inventory.getItemStack())), p);
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

    public void onUpdate(final String field, final Object oldValue, final Object newValue) {

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
}
