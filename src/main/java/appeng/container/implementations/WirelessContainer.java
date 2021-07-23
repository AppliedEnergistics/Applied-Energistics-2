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

package appeng.container.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.SecurityPermissions;
import appeng.container.AEBaseContainer;
import appeng.container.SlotSemantic;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.RestrictedInputSlot;
import appeng.core.AEConfig;
import appeng.tile.networking.WirelessTileEntity;

/**
 * @see appeng.client.gui.implementations.WirelessScreen
 */
public class WirelessContainer extends AEBaseContainer {

    public static final MenuType<WirelessContainer> TYPE = ContainerTypeBuilder
            .create(WirelessContainer::new, WirelessTileEntity.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("wireless");

    private final RestrictedInputSlot boosterSlot;
    @GuiSync(1)
    public long range = 0;
    @GuiSync(2)
    public long drain = 0;

    public WirelessContainer(int id, final Inventory ip, final WirelessTileEntity te) {
        super(TYPE, id, ip, te);

        this.addSlot(this.boosterSlot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.RANGE_BOOSTER,
                te.getInternalInventory(), 0), SlotSemantic.STORAGE);

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
