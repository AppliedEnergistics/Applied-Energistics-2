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

package appeng.menu.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.SecurityPermissions;
import appeng.api.features.INetworkEncodable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.storage.ITerminalHost;
import appeng.blockentity.inventory.AppEngInternalInventory;
import appeng.blockentity.misc.SecurityStationBlockEntity;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.me.items.ItemTerminalMenu;
import appeng.menu.slot.OutputSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.core.Api;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;

/**
 * @see appeng.client.gui.implementations.SecurityStationScreen
 */
public class SecurityStationMenu extends ItemTerminalMenu implements IAEAppEngInventory {

    public static final MenuType<SecurityStationMenu> TYPE = MenuTypeBuilder
            .create(SecurityStationMenu::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.SECURITY)
            .build("securitystation");

    private final RestrictedInputSlot configSlot;

    private final RestrictedInputSlot wirelessIn;
    private final OutputSlot wirelessOut;

    private final SecurityStationBlockEntity securityBox;
    @GuiSync(0)
    public int permissionMode = 0;

    public SecurityStationMenu(int id, final Inventory ip, final ITerminalHost monitorable) {
        super(TYPE, id, ip, monitorable, false);

        this.securityBox = (SecurityStationBlockEntity) monitorable;

        this.addSlot(this.configSlot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.BIOMETRIC_CARD,
                this.securityBox.getConfigSlot(), 0), SlotSemantic.BIOMETRIC_CARD);

        AppEngInternalInventory wirelessEncoder = new AppEngInternalInventory(this, 2);
        this.addSlot(this.wirelessIn = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ENCODABLE_ITEM,
                wirelessEncoder, 0), SlotSemantic.MACHINE_INPUT);
        this.addSlot(this.wirelessOut = new OutputSlot(wirelessEncoder, 1, null), SlotSemantic.MACHINE_OUTPUT);

        this.createPlayerInventorySlots(ip);
    }

    public void toggleSetting(final String value, final Player player) {
        try {
            final SecurityPermissions permission = SecurityPermissions.valueOf(value);

            final ItemStack a = this.configSlot.getItem();
            if (!a.isEmpty() && a.getItem() instanceof IBiometricCard) {
                final IBiometricCard bc = (IBiometricCard) a.getItem();
                if (bc.hasPermission(a, permission)) {
                    bc.removePermission(a, permission);
                } else {
                    bc.addPermission(a, permission);
                }
            }
        } catch (final EnumConstantNotPresentException ex) {
            // :(
        }
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.SECURITY, false);

        this.setPermissionMode(0);

        final ItemStack a = this.configSlot.getItem();
        if (!a.isEmpty() && a.getItem() instanceof IBiometricCard) {
            final IBiometricCard bc = (IBiometricCard) a.getItem();

            for (final SecurityPermissions sp : bc.getPermissions(a)) {
                this.setPermissionMode(this.getPermissionMode() | 1 << sp.ordinal());
            }
        }

        this.updatePowerStatus();

        super.broadcastChanges();
    }

    @Override
    public void removed(final Player player) {
        super.removed(player);

        if (this.wirelessIn.hasItem()) {
            player.drop(this.wirelessIn.getItem(), false);
        }

        if (this.wirelessOut.hasItem()) {
            player.drop(this.wirelessOut.getItem(), false);
        }
    }

    @Override
    public void saveChanges() {
        // :P
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        if (!this.wirelessOut.hasItem() && this.wirelessIn.hasItem()) {
            final ItemStack term = this.wirelessIn.getItem().copy();
            INetworkEncodable networkEncodable = null;

            if (term.getItem() instanceof INetworkEncodable) {
                networkEncodable = (INetworkEncodable) term.getItem();
            }

            final IWirelessTermHandler wTermHandler = Api.instance().registries().wireless()
                    .getWirelessTerminalHandler(term);
            if (wTermHandler != null) {
                networkEncodable = wTermHandler;
            }

            if (networkEncodable != null) {
                networkEncodable.setEncryptionKey(term, String.valueOf(this.securityBox.getSecurityKey()), "");

                this.wirelessIn.set(ItemStack.EMPTY);
                this.wirelessOut.set(term);
            }
        }
    }

    public int getPermissionMode() {
        return this.permissionMode;
    }

    private void setPermissionMode(final int permissionMode) {
        this.permissionMode = permissionMode;
    }

    public RestrictedInputSlot getConfigSlot() {
        return configSlot;
    }
}
