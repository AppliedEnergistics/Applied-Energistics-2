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


import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.AEConfig;
import appeng.tile.networking.TileWireless;
import net.minecraft.entity.player.InventoryPlayer;


public class ContainerWireless extends AEBaseContainer {

    private final TileWireless wirelessTerminal;
    private final SlotRestrictedInput boosterSlot;
    @GuiSync(1)
    public long range = 0;
    @GuiSync(2)
    public long drain = 0;

    public ContainerWireless(final InventoryPlayer ip, final TileWireless te) {
        super(ip, te, null);
        this.wirelessTerminal = te;

        this.addSlotToContainer(this.boosterSlot = new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.RANGE_BOOSTER, this.wirelessTerminal
                .getInternalInventory(), 0, 80, 47, this.getInventoryPlayer()));

        this.bindPlayerInventory(ip, 0, 166 - /* height of player inventory */82);
    }

    @Override
    public void detectAndSendChanges() {
        final int boosters = this.boosterSlot.getStack().isEmpty() ? 0 : this.boosterSlot.getStack().getCount();

        this.setRange((long) (10 * AEConfig.instance().wireless_getMaxRange(boosters)));
        this.setDrain((long) (100 * AEConfig.instance().wireless_getPowerDrain(boosters)));

        super.detectAndSendChanges();
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
