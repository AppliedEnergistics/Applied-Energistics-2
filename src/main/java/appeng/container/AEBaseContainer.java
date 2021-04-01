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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.compat.FixedInventoryVanillaWrapper;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.SlotME;
import appeng.container.guisync.GuiSync;
import appeng.container.guisync.SyncData;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.CraftingMatrixSlot;
import appeng.container.slot.CraftingTermSlot;
import appeng.container.slot.DisabledSlot;
import appeng.container.slot.FakeSlot;
import appeng.container.slot.InaccessibleSlot;
import appeng.container.slot.PlayerHotBarSlot;
import appeng.container.slot.PlayerInvSlot;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.core.sync.packets.TargetItemStackPacket;
import appeng.helpers.InventoryAction;
import appeng.me.helpers.PlayerSource;
import appeng.mixins.ScreenHandlerListeners;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorFixedInv;
import appeng.util.inv.WrapperCursorItemHandler;
import appeng.util.item.AEItemStack;

public abstract class AEBaseContainer extends Container {
    private final PlayerInventory invPlayer;
    private final IActionSource mySrc;
    private final HashSet<Integer> locked = new HashSet<>();
    private final TileEntity tileEntity;
    private final IPart part;
    private final IGuiItemObject obj;
    private final HashMap<Integer, SyncData> syncData = new HashMap<>();
    private boolean isContainerValid = true;
    private ContainerLocator locator;
    private IMEInventoryHandler<IAEItemStack> cellInv;
    private IEnergySource powerSrc;
    private int ticksSinceCheck = 900;
    private IAEItemStack clientRequestedTargetItem = null;

    public AEBaseContainer(ContainerType<?> containerType, int id, final PlayerInventory ip,
            final TileEntity myTile, final IPart myPart) {
        this(containerType, id, ip, myTile, myPart, null);
    }

    public AEBaseContainer(ContainerType<?> containerType, int id, final PlayerInventory ip,
            final TileEntity myTile, final IPart myPart, final IGuiItemObject gio) {
        super(containerType, id);
        this.invPlayer = ip;
        this.tileEntity = myTile;
        this.part = myPart;
        this.obj = gio;
        this.mySrc = new PlayerSource(ip.player, this.getActionHost());
        this.prepareSync();
    }

    public AEBaseContainer(ContainerType<?> containerType, int id, final PlayerInventory ip, final Object anchor) {
        super(containerType, id);
        this.invPlayer = ip;
        this.tileEntity = anchor instanceof TileEntity ? (TileEntity) anchor : null;
        this.part = anchor instanceof IPart ? (IPart) anchor : null;
        this.obj = anchor instanceof IGuiItemObject ? (IGuiItemObject) anchor : null;

        if (this.tileEntity == null && this.part == null && this.obj == null) {
            throw new IllegalArgumentException("Must have a valid anchor, instead " + anchor + " in " + ip);
        }

        this.mySrc = new PlayerSource(ip.player, this.getActionHost());

        this.prepareSync();
    }

    protected IActionHost getActionHost() {
        if (this.obj instanceof IActionHost) {
            return (IActionHost) this.obj;
        }

        if (this.tileEntity instanceof IActionHost) {
            return (IActionHost) this.tileEntity;
        }

        if (this.part instanceof IActionHost) {
            return (IActionHost) this.part;
        }

        return null;
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

    public IAEItemStack getTargetStack() {
        return this.clientRequestedTargetItem;
    }

    public void setTargetStack(final IAEItemStack stack) {
        // client doesn't need to re-send, makes for lower overhead rapid packets.
        if (Platform.isClient()) {
            if (stack == null && this.clientRequestedTargetItem == null) {
                return;
            }
            if (stack != null && stack.isSameType(this.clientRequestedTargetItem)) {
                return;
            }

            NetworkHandler.instance().sendToServer(new TargetItemStackPacket((AEItemStack) stack));
        }

        this.clientRequestedTargetItem = stack == null ? null : stack.copy();
    }

    public IActionSource getActionSource() {
        return this.mySrc;
    }

    public void verifyPermissions(final SecurityPermissions security, final boolean requirePower) {
        if (Platform.isClient()) {
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

    public void lockPlayerInventorySlot(final int idx) {
        this.locked.add(idx);
    }

    public Object getTarget() {
        if (this.tileEntity != null) {
            return this.tileEntity;
        }
        if (this.part != null) {
            return this.part;
        }
        if (this.obj != null) {
            return this.obj;
        }
        return null;
    }

    public PlayerInventory getPlayerInv() {
        return this.getPlayerInventory();
    }

    public TileEntity getBlockEntity() {
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

    protected void bindPlayerInventory(final PlayerInventory playerInventory, final int offsetX, final int offsetY) {
        FixedItemInv ih = new FixedInventoryVanillaWrapper(playerInventory) {
            // Vanilla needs this to be modifiable otherwise stacking will eat items.
            // FIXME FABRIC: Remove when LBA makes these mutable as per
            // ModifiableFixedItemInv
            @Override
            public ItemStack getInvStack(int slot) {
                return playerInventory.getStackInSlot(slot);
            }
        };

        // bind player inventory
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                int invSlot = j + i * 9 + 9;
                int x = 8 + j * 18 + offsetX;
                int y = offsetY + i * 18;

                if (this.locked.contains(invSlot)) {
                    this.addSlot(new DisabledSlot(ih, invSlot, x, y));
                } else {
                    this.addSlot(new PlayerInvSlot(ih, invSlot, x, y));
                }
            }
        }

        // bind player hotbar
        for (int i = 0; i < 9; i++) {
            int x = 8 + i * 18 + offsetX;
            int y = 58 + offsetY;
            if (this.locked.contains(i)) {
                this.addSlot(new DisabledSlot(ih, i, x, y));
            } else {
                this.addSlot(new PlayerHotBarSlot(ih, i, x, y));
            }
        }
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

    public List<IContainerListener> getListeners() {
        return ((ScreenHandlerListeners) this).ae2_getListeners();
    }

    @Override
    public void detectAndSendChanges() {
        if (isServer()) {
            if (this.tileEntity != null
                    && this.tileEntity.getWorld().getTileEntity(this.tileEntity.getPos()) != this.tileEntity) {
                this.setValidContainer(false);
            }

            for (final IContainerListener listener : this.getListeners()) {
                for (final SyncData sd : this.syncData.values()) {
                    sd.tick(listener);
                }
            }
        }

        super.detectAndSendChanges();
    }

    @Override
    public ItemStack transferStackInSlot(final PlayerEntity p, final int idx) {
        if (Platform.isClient()) {
            return ItemStack.EMPTY;
        }

        final AppEngSlot clickSlot = (AppEngSlot) this.inventorySlots.get(idx); // require AE SLots!

        if (clickSlot instanceof DisabledSlot || clickSlot instanceof InaccessibleSlot) {
            return ItemStack.EMPTY;
        }
        if (clickSlot != null && clickSlot.getHasStack()) {
            ItemStack tis = clickSlot.getStack();

            if (tis.isEmpty()) {
                return ItemStack.EMPTY;
            }

            final List<Slot> selectedSlots = new ArrayList<>();

            /**
             * Gather a list of valid destinations.
             */
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

            /**
             * Handle Fake Slot Shift clicking.
             */
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
                                this.updateSlot(cs);
                                break;
                            }
                        }
                    }
                }
            }

            if (!tis.isEmpty()) {
                // find partials..
                for (final Slot d : selectedSlots) {
                    if (d instanceof DisabledSlot || d instanceof SlotME) {
                        continue;
                    }

                    if (d.isItemValid(tis)) {
                        if (d.getHasStack()) {
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

                                        this.updateSlot(clickSlot);
                                        this.updateSlot(d);
                                        return ItemStack.EMPTY;
                                    } else {
                                        this.updateSlot(d);
                                    }
                                }
                            }
                        }
                    }
                }

                // FIXME figure out whats the difference between this and the one above ?!
                // any match..
                for (final Slot d : selectedSlots) {
                    if (d instanceof DisabledSlot || d instanceof SlotME) {
                        continue;
                    }

                    if (d.isItemValid(tis)) {
                        if (d.getHasStack()) {
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

                                        this.updateSlot(clickSlot);
                                        this.updateSlot(d);
                                        return ItemStack.EMPTY;
                                    } else {
                                        this.updateSlot(d);
                                    }
                                }
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

                                this.updateSlot(clickSlot);
                                this.updateSlot(d);
                                return ItemStack.EMPTY;
                            } else {
                                this.updateSlot(d);
                            }
                        }
                    }
                }
            }

            clickSlot.putStack(!tis.isEmpty() ? tis : ItemStack.EMPTY);
        }

        this.updateSlot(clickSlot);
        return ItemStack.EMPTY;
    }

    @Override
    public final void updateProgressBar(final int idx, final int value) {
        if (this.syncData.containsKey(idx)) {
            this.syncData.get(idx).update((long) value);
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        if (this.isValidContainer()) {
            if (this.tileEntity instanceof IInventory) {
                return ((IInventory) this.tileEntity).isUsableByPlayer(player);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canDragIntoSlot(Slot slot) {
        return ((AppEngSlot) slot).isDraggable();
    }

    public void doAction(final ServerPlayerEntity player, final InventoryAction action, final int slot, final long id) {
        if (slot >= 0 && slot < this.inventorySlots.size()) {
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
                            } else if (hand.isItemEqualIgnoreDurability(is)) {
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
                final List<Integer> from = new ArrayList<>();

                for (int i = 0; i < this.inventorySlots.size(); i++) {
                    Slot j = this.inventorySlots.get(i);
                    if (j.getClass() == s.getClass() && !(j instanceof CraftingTermSlot)) {
                        from.add(i);
                    }
                }

                for (final int fromIndex : from) {
                    this.transferStackInSlot(player, fromIndex);
                }
            }

            return;
        }

        // get target item.
        final IAEItemStack slotItem = this.clientRequestedTargetItem;

        switch (action) {
            case SHIFT_CLICK:
                if (this.getPowerSource() == null || this.getCellInventory() == null) {
                    return;
                }

                if (slotItem != null) {
                    IAEItemStack ais = slotItem.copy();
                    ItemStack myItem = ais.createItemStack();

                    ais.setStackSize(myItem.getMaxStackSize());

                    final InventoryAdaptor adp = InventoryAdaptor.getAdaptor(player);
                    myItem.setCount((int) ais.getStackSize());
                    myItem = adp.simulateAdd(myItem);

                    if (!myItem.isEmpty()) {
                        ais.setStackSize(ais.getStackSize() - myItem.getCount());
                    }

                    ais = Platform.poweredExtraction(this.getPowerSource(), this.getCellInventory(), ais,
                            this.getActionSource());
                    if (ais != null) {
                        adp.addItems(ais.createItemStack());
                    }
                }
                break;
            case ROLL_DOWN:
                if (this.getPowerSource() == null || this.getCellInventory() == null) {
                    return;
                }

                final int releaseQty = 1;
                final ItemStack isg = player.inventory.getItemStack();

                if (!isg.isEmpty() && releaseQty > 0) {
                    IAEItemStack ais = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
                            .createStack(isg);
                    ais.setStackSize(1);
                    final IAEItemStack extracted = ais.copy();

                    ais = Platform.poweredInsert(this.getPowerSource(), this.getCellInventory(), ais,
                            this.getActionSource());
                    if (ais == null) {
                        final InventoryAdaptor ia = new AdaptorFixedInv(new WrapperCursorItemHandler(player.inventory));

                        final ItemStack fail = ia.removeItems(1, extracted.getDefinition(), null);
                        if (fail.isEmpty()) {
                            this.getCellInventory().extractItems(extracted, Actionable.MODULATE,
                                    this.getActionSource());
                        }

                        this.updateHeld(player);
                    }
                }

                break;
            case ROLL_UP:
            case PICKUP_SINGLE:
                if (this.getPowerSource() == null || this.getCellInventory() == null) {
                    return;
                }

                if (slotItem != null) {
                    int liftQty = 1;
                    final ItemStack item = player.inventory.getItemStack();

                    if (!item.isEmpty()) {
                        if (item.getCount() >= item.getMaxStackSize()) {
                            liftQty = 0;
                        }
                        if (!Platform.itemComparisons().isSameItem(slotItem.getDefinition(), item)) {
                            liftQty = 0;
                        }
                    }

                    if (liftQty > 0) {
                        IAEItemStack ais = slotItem.copy();
                        ais.setStackSize(1);
                        ais = Platform.poweredExtraction(this.getPowerSource(), this.getCellInventory(), ais,
                                this.getActionSource());
                        if (ais != null) {
                            final InventoryAdaptor ia = new AdaptorFixedInv(
                                    new WrapperCursorItemHandler(player.inventory));

                            final ItemStack fail = ia.addItems(ais.createItemStack());
                            if (!fail.isEmpty()) {
                                this.getCellInventory().injectItems(ais, Actionable.MODULATE, this.getActionSource());
                            }

                            this.updateHeld(player);
                        }
                    }
                }
                break;
            case PICKUP_OR_SET_DOWN:
                if (this.getPowerSource() == null || this.getCellInventory() == null) {
                    return;
                }

                if (player.inventory.getItemStack().isEmpty()) {
                    if (slotItem != null) {
                        IAEItemStack ais = slotItem.copy();
                        ais.setStackSize(ais.getDefinition().getMaxCount());
                        ais = Platform.poweredExtraction(this.getPowerSource(), this.getCellInventory(), ais,
                                this.getActionSource());
                        if (ais != null) {
                            player.inventory.setItemStack(ais.createItemStack());
                        } else {
                            player.inventory.setItemStack(ItemStack.EMPTY);
                        }
                        this.updateHeld(player);
                    }
                } else {
                    IAEItemStack ais = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
                            .createStack(player.inventory.getItemStack());
                    ais = Platform.poweredInsert(this.getPowerSource(), this.getCellInventory(), ais,
                            this.getActionSource());
                    if (ais != null) {
                        player.inventory.setItemStack(ais.createItemStack());
                    } else {
                        player.inventory.setItemStack(ItemStack.EMPTY);
                    }
                    this.updateHeld(player);
                }

                break;
            case SPLIT_OR_PLACE_SINGLE:
                if (this.getPowerSource() == null || this.getCellInventory() == null) {
                    return;
                }

                if (player.inventory.getItemStack().isEmpty()) {
                    if (slotItem != null) {
                        IAEItemStack ais = slotItem.copy();
                        final long maxSize = ais.getDefinition().getMaxCount();
                        ais.setStackSize(maxSize);
                        ais = this.getCellInventory().extractItems(ais, Actionable.SIMULATE, this.getActionSource());

                        if (ais != null) {
                            final long stackSize = Math.min(maxSize, ais.getStackSize());
                            ais.setStackSize((stackSize + 1) >> 1);
                            ais = Platform.poweredExtraction(this.getPowerSource(), this.getCellInventory(), ais,
                                    this.getActionSource());
                        }

                        if (ais != null) {
                            player.inventory.setItemStack(ais.createItemStack());
                        } else {
                            player.inventory.setItemStack(ItemStack.EMPTY);
                        }
                        this.updateHeld(player);
                    }
                } else {
                    IAEItemStack ais = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
                            .createStack(player.inventory.getItemStack());
                    ais.setStackSize(1);
                    ais = Platform.poweredInsert(this.getPowerSource(), this.getCellInventory(), ais,
                            this.getActionSource());
                    if (ais == null) {
                        final ItemStack is = player.inventory.getItemStack();
                        is.setCount(is.getCount() - 1);
                        if (is.getCount() <= 0) {
                            player.inventory.setItemStack(ItemStack.EMPTY);
                        }
                        this.updateHeld(player);
                    }
                }

                break;
            case CREATIVE_DUPLICATE:
                if (player.isCreative() && slotItem != null) {
                    final ItemStack is = slotItem.createItemStack();
                    is.setCount(is.getMaxStackSize());
                    player.inventory.setItemStack(is);
                    this.updateHeld(player);
                }
                break;
            case MOVE_REGION:

                if (this.getPowerSource() == null || this.getCellInventory() == null) {
                    return;
                }

                if (slotItem != null) {
                    final int playerInv = 9 * 4;
                    for (int slotNum = 0; slotNum < playerInv; slotNum++) {
                        IAEItemStack ais = slotItem.copy();
                        ItemStack myItem = ais.createItemStack();

                        ais.setStackSize(myItem.getMaxStackSize());

                        final InventoryAdaptor adp = InventoryAdaptor.getAdaptor(player);
                        myItem.setCount((int) ais.getStackSize());
                        myItem = adp.simulateAdd(myItem);

                        if (!myItem.isEmpty()) {
                            ais.setStackSize(ais.getStackSize() - myItem.getCount());
                        }

                        ais = Platform.poweredExtraction(this.getPowerSource(), this.getCellInventory(), ais,
                                this.getActionSource());
                        if (ais != null) {
                            adp.addItems(ais.createItemStack());
                        } else {
                            return;
                        }
                    }
                }

                break;
            default:
                break;
        }
    }

    protected void updateHeld(final ServerPlayerEntity p) {
        if (Platform.isServer()) {
            NetworkHandler.instance().sendTo(new InventoryActionPacket(InventoryAction.UPDATE_HAND, 0,
                    AEItemStack.fromItemStack(p.inventory.getItemStack())), p);
        }
    }

    protected ItemStack transferStackToContainer(final ItemStack input) {
        return this.shiftStoreItem(input);
    }

    private ItemStack shiftStoreItem(final ItemStack input) {
        if (this.getPowerSource() == null || this.getCellInventory() == null) {
            return input;
        }
        final IAEItemStack ais = Platform.poweredInsert(this.getPowerSource(), this.getCellInventory(),
                Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(input),
                this.getActionSource());
        if (ais == null) {
            return ItemStack.EMPTY;
        }
        return ais.createItemStack();
    }

    private void updateSlot(final Slot clickSlot) {
        // ???
        this.detectAndSendChanges();
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

    public IMEInventoryHandler<IAEItemStack> getCellInventory() {
        return this.cellInv;
    }

    public void setCellInventory(final IMEInventoryHandler<IAEItemStack> cellInv) {
        this.cellInv = cellInv;
    }

    public PlayerInventory getPlayerInventory() {
        return this.invPlayer;
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

    public IEnergySource getPowerSource() {
        return this.powerSrc;
    }

    public void setPowerSource(final IEnergySource powerSrc) {
        this.powerSrc = powerSrc;
    }

    /**
     * Returns whether this container instance lives on the client.
     */
    protected boolean isClient() {
        return invPlayer.player.getEntityWorld().isRemote();
    }

    /**
     * Returns whether this container instance lives on the server.
     */
    protected boolean isServer() {
        return !isClient();
    }

}
