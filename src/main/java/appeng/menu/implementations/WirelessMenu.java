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
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.SecurityPermissions;
import appeng.blockentity.networking.WirelessBlockEntity;
import appeng.core.AEConfig;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.RestrictedInputSlot;

/**
 * @see appeng.client.gui.implementations.WirelessScreen
 */
public class WirelessMenu extends AEBaseMenu {

    public static final MenuType<WirelessMenu> TYPE = MenuTypeBuilder
            .create(WirelessMenu::new, WirelessBlockEntity.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("wireless");

    private final RestrictedInputSlot boosterSlot;
    @GuiSync(1)
    public long range = 0;
    @GuiSync(2)
    public long drain = 0;

    public WirelessMenu(int id, final Inventory ip, final WirelessBlockEntity te) {
        super(TYPE, id, ip, te);

        this.addSlot(this.boosterSlot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.RANGE_BOOSTER,
                te.getInternalInventory(), 0), SlotSemantics.STORAGE);

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

    private void setRange(final long range) {
        this.range = range;
    }

    public long getDrain() {
        return this.drain;
    }

    private void setDrain(final long drain) {
        this.drain = drain;
    }
}
