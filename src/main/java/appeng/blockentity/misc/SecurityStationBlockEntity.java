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

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.features.IPlayerRegistry;
import appeng.api.features.Locatables;
import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.events.GridSecurityChange;
import appeng.api.networking.security.ISecurityProvider;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.MEMonitorStorage;
import appeng.api.storage.MEStorage;
import appeng.api.storage.data.AEItemKey;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.IConfigManager;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.items.tools.BiometricCardItem;
import appeng.me.helpers.MEMonitorHandler;
import appeng.me.storage.SecurityStationInventory;
import appeng.menu.ISubMenu;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.SecurityStationMenu;
import appeng.util.ConfigManager;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;

public class SecurityStationBlockEntity extends AENetworkBlockEntity implements ITerminalHost, InternalInventoryHost,
        ISecurityProvider, IColorableBlockEntity {

    private static int difference = 0;
    private final AppEngInternalInventory configSlot = new AppEngInternalInventory(this, 1);
    private final IConfigManager cm = new ConfigManager();
    private final SecurityStationInventory inventory = new SecurityStationInventory(this);
    private final MEMonitorHandler securityMonitor = new MEMonitorHandler(this.inventory);
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
    public void onChangeInventory(final InternalInventory inv, final int slot,
            final ItemStack removedStack, final ItemStack newStack) {

    }

    @Override
    public void getDrops(final Level level, final BlockPos pos, final List<ItemStack> drops) {
        if (!this.getConfigSlot().isEmpty()) {
            drops.add(this.getConfigSlot().getStackInSlot(0));
        }

        for (var key : this.inventory.getStoredItems()) {
            drops.add(key.toStack());
        }
    }

    MEStorage getSecurityInventory() {
        return this.inventory;
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) {
        final boolean c = super.readFromStream(data);
        final boolean wasActive = this.isActive;
        this.isActive = data.readBoolean();

        final AEColor oldPaintedColor = this.paintedColor;
        this.paintedColor = AEColor.values()[data.readByte()];

        return oldPaintedColor != this.paintedColor || wasActive != this.isActive || c;
    }

    @Override
    protected void writeToStream(final FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.getMainNode().isActive());
        data.writeByte(this.paintedColor.ordinal());
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.cm.writeToNBT(data);
        data.putByte("paintedColor", (byte) this.paintedColor.ordinal());

        data.putLong("securityKey", this.securityKey);
        this.getConfigSlot().writeToNBT(data, "config");

        var storedItems = new ListTag();

        for (var key : this.inventory.getStoredItems()) {
            storedItems.add(key.toTag());
        }

        data.put("cards", storedItems);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.cm.readFromNBT(data);
        if (data.contains("paintedColor")) {
            this.paintedColor = AEColor.values()[data.getByte("paintedColor")];
        }

        this.securityKey = data.getLong("securityKey");
        this.getConfigSlot().readFromNBT(data, "config");

        var cards = data.getList("cards", Tag.TAG_COMPOUND);
        for (var keyTag : cards) {
            var key = AEItemKey.fromTag((CompoundTag) keyTag);
            if (key != null) {
                this.inventory.getStoredItems().add(key);
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
        Locatables.securityStations().unregister(getLevel(), securityKey);
        this.isActive = false;
    }

    @Override
    public void onReady() {
        super.onReady();
        if (!isClientSide()) {
            this.isActive = true;
            Locatables.securityStations().register(getLevel(), securityKey, this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        Locatables.securityStations().unregister(getLevel(), securityKey);
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
    public MEMonitorStorage getInventory() {
        return securityMonitor;
    }

    public boolean isPowered() {
        return this.getMainNode().isActive();
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.cm;
    }

    @Override
    public long getSecurityKey() {
        return this.securityKey;
    }

    @Override
    public void readPermissions(final Map<Integer, EnumSet<SecurityPermissions>> playerPerms) {
        var pr = IPlayerRegistry.getMapping(getLevel());
        if (pr == null) {
            return;
        }

        // read permissions
        for (var key : this.inventory.getStoredItems()) {
            if (key.getItem() instanceof BiometricCardItem bc) {
                var playerId = -1;
                var profile = bc.getProfile(key);
                if (profile != null) {
                    playerId = pr.getPlayerId(profile);
                }
                var permissions = bc.getPermissions(key.getTag());

                playerPerms.put(playerId, permissions);
            }
        }

        // make sure the admin is Boss.
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

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        openMenu(player);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return getItemFromBlockEntity();
    }

    public void openMenu(Player p) {
        MenuOpener.open(SecurityStationMenu.TYPE, p, MenuLocator.forBlockEntitySide(this, getUp()));
    }
}
