/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.tile.misc;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.events.LocatableEventAnnounce;
import appeng.api.events.LocatableEventAnnounce.LocatableEvent;
import appeng.api.features.ILocatable;
import appeng.api.features.IPlayerRegistry;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkSecurityChange;
import appeng.api.networking.security.ISecurityProvider;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.IConfigManager;
import appeng.core.Api;
import appeng.helpers.PlayerSecurityWrapper;
import appeng.me.GridAccessException;
import appeng.me.helpers.MEMonitorHandler;
import appeng.me.storage.SecurityStationInventory;
import appeng.tile.grid.AENetworkTileEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.item.AEItemStack;

public class SecurityStationTileEntity extends AENetworkTileEntity implements ITerminalHost, IAEAppEngInventory,
        ILocatable, IConfigManagerHost, ISecurityProvider, IColorableTile {

    private static int difference = 0;
    private final AppEngInternalInventory configSlot = new AppEngInternalInventory(this, 1);
    private final IConfigManager cm = new ConfigManager(this);
    private final SecurityStationInventory inventory = new SecurityStationInventory(this);
    private final MEMonitorHandler<IAEItemStack> securityMonitor = new MEMonitorHandler<>(this.inventory);
    private long securityKey;
    private AEColor paintedColor = AEColor.TRANSPARENT;
    private boolean isActive = false;

    public SecurityStationTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.getProxy().setIdlePowerUsage(2.0);
        difference++;

        this.securityKey = System.currentTimeMillis() * 10 + difference;
        if (difference > 10) {
            difference = 0;
        }

        this.cm.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.cm.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        this.cm.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {

    }

    @Override
    public void getDrops(final World w, final BlockPos pos, final List<ItemStack> drops) {
        if (!ItemHandlerUtil.isEmpty(this.getConfigSlot())) {
            drops.add(this.getConfigSlot().getStackInSlot(0));
        }

        for (final IAEItemStack ais : this.inventory.getStoredItems()) {
            drops.add(ais.createItemStack());
        }
    }

    IMEInventoryHandler<IAEItemStack> getSecurityInventory() {
        return this.inventory;
    }

    @Override
    protected boolean readFromStream(final PacketBuffer data) throws IOException {
        final boolean c = super.readFromStream(data);
        final boolean wasActive = this.isActive;
        this.isActive = data.readBoolean();

        final AEColor oldPaintedColor = this.paintedColor;
        this.paintedColor = AEColor.values()[data.readByte()];

        return oldPaintedColor != this.paintedColor || wasActive != this.isActive || c;
    }

    @Override
    protected void writeToStream(final PacketBuffer data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.getProxy().isActive());
        data.writeByte(this.paintedColor.ordinal());
    }

    @Override
    public CompoundNBT write(final CompoundNBT data) {
        super.write(data);
        this.cm.writeToNBT(data);
        data.putByte("paintedColor", (byte) this.paintedColor.ordinal());

        data.putLong("securityKey", this.securityKey);
        this.getConfigSlot().writeToNBT(data, "config");

        final CompoundNBT storedItems = new CompoundNBT();

        int offset = 0;
        for (final IAEItemStack ais : this.inventory.getStoredItems()) {
            final CompoundNBT it = new CompoundNBT();
            ais.createItemStack().write(it);
            storedItems.put(String.valueOf(offset), it);
            offset++;
        }

        data.put("storedItems", storedItems);
        return data;
    }

    @Override
    public void read(BlockState blockState, final CompoundNBT data) {
        super.read(blockState, data);
        this.cm.readFromNBT(data);
        if (data.contains("paintedColor")) {
            this.paintedColor = AEColor.values()[data.getByte("paintedColor")];
        }

        this.securityKey = data.getLong("securityKey");
        this.getConfigSlot().readFromNBT(data, "config");

        final CompoundNBT storedItems = data.getCompound("storedItems");
        for (final Object key : storedItems.keySet()) {
            final INBT obj = storedItems.get((String) key);
            if (obj instanceof CompoundNBT) {
                this.inventory.getStoredItems().add(AEItemStack.fromItemStack(ItemStack.read((CompoundNBT) obj)));
            }
        }
    }

    public void inventoryChanged() {
        try {
            this.saveChanges();
            this.getProxy().getGridOrThrow().postEvent(new MENetworkSecurityChange());
        } catch (final GridAccessException e) {
            // :P
        }
    }

    @MENetworkEventSubscribe
    public void bootUpdate(final MENetworkChannelsChanged changed) {
        this.markForUpdate();
    }

    @MENetworkEventSubscribe
    public void powerUpdate(final MENetworkPowerStatusChange changed) {
        this.markForUpdate();
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        MinecraftForge.EVENT_BUS.post(new LocatableEventAnnounce(this, LocatableEvent.UNREGISTER));
        this.isActive = false;
    }

    @Override
    public void onReady() {
        super.onReady();
        if (!isRemote()) {
            this.isActive = true;
            MinecraftForge.EVENT_BUS.post(new LocatableEventAnnounce(this, LocatableEvent.REGISTER));
        }
    }

    @Override
    public void remove() {
        super.remove();
        MinecraftForge.EVENT_BUS.post(new LocatableEventAnnounce(this, LocatableEvent.UNREGISTER));
        this.isActive = false;
    }

    @Override
    public DimensionalBlockPos getLocation() {
        return new DimensionalBlockPos(this);
    }

    public boolean isActive() {
        if (world != null && !world.isRemote) {
            return isPowered();
        } else {
            return this.isActive;
        }
    }

    @Override
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        if (channel == Api.instance().storage().getStorageChannel(IItemStorageChannel.class)) {
            return (IMEMonitor<T>) this.securityMonitor;
        }
        return null;

    }

    @Override
    public long getLocatableSerial() {
        return this.securityKey;
    }

    public boolean isPowered() {
        return this.getProxy().isActive();
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.cm;
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Settings settingName, final Enum<?> newValue) {

    }

    @Override
    public long getSecurityKey() {
        return this.securityKey;
    }

    @Override
    public void readPermissions(final Map<Integer, EnumSet<SecurityPermissions>> playerPerms) {
        final IPlayerRegistry pr = Api.instance().registries().players();

        // read permissions
        for (final IAEItemStack ais : this.inventory.getStoredItems()) {
            final ItemStack is = ais.createItemStack();
            final Item i = is.getItem();
            if (i instanceof IBiometricCard) {
                final IBiometricCard bc = (IBiometricCard) i;
                bc.registerPermissions(new PlayerSecurityWrapper(playerPerms), pr, is);
            }
        }

        // make sure thea admin is Boss.
        playerPerms.put(this.getProxy().getNode().getOwner(), EnumSet.allOf(SecurityPermissions.class));
    }

    @Override
    public boolean isSecurityEnabled() {
        return this.isActive && this.getProxy().isActive();
    }

    @Override
    public int getOwner() {
        return this.getProxy().getNode().getOwner();
    }

    @Override
    public AEColor getColor() {
        return this.paintedColor;
    }

    @Override
    public boolean recolourBlock(final Direction side, final AEColor newPaintedColor, final PlayerEntity who) {
        if (this.paintedColor == newPaintedColor) {
            return false;
        }

        this.paintedColor = newPaintedColor;
        this.saveChanges();
        this.markForUpdate();
        return true;
    }

    public AppEngInternalInventory getConfigSlot() {
        return this.configSlot;
    }
}
