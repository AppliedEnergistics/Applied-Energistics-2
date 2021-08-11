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

package appeng.blockentity.misc;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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
import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.events.GridSecurityChange;
import appeng.api.networking.security.ISecurityProvider;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.IConfigManager;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.blockentity.inventory.AppEngInternalInventory;
import appeng.core.Api;
import appeng.helpers.PlayerSecurityWrapper;
import appeng.me.helpers.MEMonitorHandler;
import appeng.me.storage.SecurityStationInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.item.AEItemStack;

public class SecurityStationBlockEntity extends AENetworkBlockEntity implements ITerminalHost, IAEAppEngInventory,
        ILocatable, IConfigManagerHost, ISecurityProvider, IColorableBlockEntity {

    private static int difference = 0;
    private final AppEngInternalInventory configSlot = new AppEngInternalInventory(this, 1);
    private final IConfigManager cm = new ConfigManager(this);
    private final SecurityStationInventory inventory = new SecurityStationInventory(this);
    private final MEMonitorHandler<IAEItemStack> securityMonitor = new MEMonitorHandler<>(this.inventory);
    private long securityKey;
    private AEColor paintedColor = AEColor.TRANSPARENT;
    private boolean isActive = false;

    public SecurityStationBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(2.0)
                .addService(ISecurityProvider.class, this);
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
    public void getDrops(final Level level, final BlockPos pos, final List<ItemStack> drops) {
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
    protected boolean readFromStream(final FriendlyByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);
        final boolean wasActive = this.isActive;
        this.isActive = data.readBoolean();

        final AEColor oldPaintedColor = this.paintedColor;
        this.paintedColor = AEColor.values()[data.readByte()];

        return oldPaintedColor != this.paintedColor || wasActive != this.isActive || c;
    }

    @Override
    protected void writeToStream(final FriendlyByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.getMainNode().isActive());
        data.writeByte(this.paintedColor.ordinal());
    }

    @Override
    public CompoundTag save(final CompoundTag data) {
        super.save(data);
        this.cm.writeToNBT(data);
        data.putByte("paintedColor", (byte) this.paintedColor.ordinal());

        data.putLong("securityKey", this.securityKey);
        this.getConfigSlot().writeToNBT(data, "config");

        final CompoundTag storedItems = new CompoundTag();

        int offset = 0;
        for (final IAEItemStack ais : this.inventory.getStoredItems()) {
            final CompoundTag it = new CompoundTag();
            ais.createItemStack().save(it);
            storedItems.put(String.valueOf(offset), it);
            offset++;
        }

        data.put("storedItems", storedItems);
        return data;
    }

    @Override
    public void load(final CompoundTag data) {
        super.load(data);
        this.cm.readFromNBT(data);
        if (data.contains("paintedColor")) {
            this.paintedColor = AEColor.values()[data.getByte("paintedColor")];
        }

        this.securityKey = data.getLong("securityKey");
        this.getConfigSlot().readFromNBT(data, "config");

        final CompoundTag storedItems = data.getCompound("storedItems");
        for (final Object key : storedItems.getAllKeys()) {
            final Tag obj = storedItems.get((String) key);
            if (obj instanceof CompoundTag) {
                this.inventory.getStoredItems().add(AEItemStack.fromItemStack(ItemStack.of((CompoundTag) obj)));
            }
        }
    }

    public void inventoryChanged() {
        this.saveChanges();
        getMainNode().ifPresent(grid -> grid.postEvent(new GridSecurityChange()));
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
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
    public void setRemoved() {
        super.setRemoved();
        MinecraftForge.EVENT_BUS.post(new LocatableEventAnnounce(this, LocatableEvent.UNREGISTER));
        this.isActive = false;
    }

    public boolean isActive() {
        if (level != null && !level.isClientSide) {
            return isPowered();
        } else {
            return this.isActive;
        }
    }

    @Override
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        if (channel == StorageChannels.items()) {
            return (IMEMonitor<T>) this.securityMonitor;
        }
        return null;

    }

    @Override
    public long getLocatableSerial() {
        return this.securityKey;
    }

    public boolean isPowered() {
        return this.getMainNode().isActive();
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
            if (i instanceof IBiometricCard bc) {
                bc.registerPermissions(new PlayerSecurityWrapper(playerPerms), pr, is);
            }
        }

        // make sure thea admin is Boss.
        playerPerms.put(this.getMainNode().getNode().getOwningPlayerId(), EnumSet.allOf(SecurityPermissions.class));
    }

    @Override
    public boolean isSecurityEnabled() {
        return this.isActive && this.getMainNode().isActive();
    }

    @Override
    public int getOwner() {
        return this.getMainNode().getNode().getOwningPlayerId();
    }

    @Override
    public AEColor getColor() {
        return this.paintedColor;
    }

    @Override
    public boolean recolourBlock(final Direction side, final AEColor newPaintedColor, final Player who) {
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
