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
import appeng.container.interfaces.IProgressProvider;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.misc.TileVibrationChamber;
import appeng.util.Platform;
import net.minecraft.entity.player.InventoryPlayer;


public class ContainerVibrationChamber extends AEBaseContainer implements IProgressProvider {
    private final TileVibrationChamber vibrationChamber;
    @GuiSync(0)
    public int burnSpeed = 0;
    @GuiSync(1)
    public int remainingBurnTime = 0;

    public ContainerVibrationChamber(final InventoryPlayer ip, final TileVibrationChamber vibrationChamber) {
        super(ip, vibrationChamber, null);
        this.vibrationChamber = vibrationChamber;

        this.addSlotToContainer(new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.FUEL, vibrationChamber.getInternalInventory(), 0, 80, 37, this
                .getInventoryPlayer()));

        this.bindPlayerInventory(ip, 0, 166 - /* height of player inventory */82);
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isServer()) {
            this.remainingBurnTime = this.vibrationChamber
                    .getMaxBurnTime() <= 0 ? 0 : (int) (100.0 * this.vibrationChamber.getBurnTime() / this.vibrationChamber.getMaxBurnTime());
            this.burnSpeed = this.remainingBurnTime <= 0 ? 0 : this.vibrationChamber.getBurnSpeed();

        }
        super.detectAndSendChanges();
    }

    @Override
    public int getCurrentProgress() {
        return this.burnSpeed;
    }

    public int getRemainingBurnTime() {
        return this.remainingBurnTime;
    }

    @Override
    public int getMaxProgress() {
        return TileVibrationChamber.MAX_BURN_SPEED;
    }
}
