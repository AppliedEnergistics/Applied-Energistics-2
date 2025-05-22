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

import appeng.api.features.GridLinkables;
import appeng.api.inventories.InternalInventory;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.client.gui.implementations.WirelessAccessPointScreen;
import appeng.core.AEConfig;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.Tooltips;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.OutputSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;

/**
 * @see WirelessAccessPointScreen
 */
public class WirelessAccessPointMenu extends AEBaseMenu implements InternalInventoryHost {

    public static final MenuType<WirelessAccessPointMenu> TYPE = MenuTypeBuilder
            .create(WirelessAccessPointMenu::new, WirelessAccessPointBlockEntity.class)
            .build("wireless_access_point");

    private final WirelessAccessPointBlockEntity accessPoint;
    private final RestrictedInputSlot boosterSlot;
    private final RestrictedInputSlot linkableIn;
    private final OutputSlot linkableOut;

    @GuiSync(1)
    public long range = 0;
    @GuiSync(2)
    public long drain = 0;

    public WirelessAccessPointMenu(int id, Inventory ip, WirelessAccessPointBlockEntity host) {
        super(TYPE, id, ip, host);

        this.accessPoint = host;

        this.addSlot(this.boosterSlot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.RANGE_BOOSTER,
                host.getInternalInventory(), 0), SlotSemantics.STORAGE);
        this.boosterSlot.setEmptyTooltip(() -> Tooltips.slotTooltip(ButtonToolTips.PlaceWirelessBooster.text()));

        // Add a small inventory and two slots for linking items to the connected grid
        AppEngInternalInventory gridLinkingInv = new AppEngInternalInventory(this, 2);
        this.addSlot(this.linkableIn = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.GRID_LINKABLE_ITEM,
                gridLinkingInv, 0), SlotSemantics.MACHINE_INPUT);
        this.linkableIn.setEmptyTooltip(() -> Tooltips.slotTooltip(ButtonToolTips.LinkWirelessTerminal.text()));
        this.addSlot(this.linkableOut = new OutputSlot(gridLinkingInv, 1, null), SlotSemantics.MACHINE_OUTPUT);

        this.createPlayerInventorySlots(ip);
    }

    @Override
    public void broadcastChanges() {
        final int boosters = this.boosterSlot.getItem().isEmpty() ? 0 : this.boosterSlot.getItem().getCount();

        this.setRange((long) (10 * AEConfig.instance().wireless_getMaxRange(boosters)));
        this.setDrain((long) (100 * AEConfig.instance().wireless_getPowerDrain(boosters)));

        super.broadcastChanges();
    }

    public long getRange() {
        return this.range;
    }

    private void setRange(long range) {
        this.range = range;
    }

    public long getDrain() {
        return this.drain;
    }

    private void setDrain(long drain) {
        this.drain = drain;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        if (this.linkableIn.hasItem()) {
            player.drop(this.linkableIn.getItem(), false);
        }

        if (this.linkableOut.hasItem()) {
            player.drop(this.linkableOut.getItem(), false);
        }
    }

    @Override
    public void saveChanges() {
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        if (!this.linkableOut.hasItem() && this.linkableIn.hasItem()) {
            var term = this.linkableIn.getItem().copy();

            var handler = GridLinkables.get(term.getItem());

            if (handler != null && handler.canLink(term)) {
                handler.link(term, accessPoint.getGlobalPos());

                this.linkableIn.set(ItemStack.EMPTY);
                this.linkableOut.set(term);
            }
        }
    }
}
