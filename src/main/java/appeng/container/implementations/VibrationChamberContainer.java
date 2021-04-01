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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.container.interfaces.IProgressProvider;
import appeng.container.slot.RestrictedInputSlot;
import appeng.tile.misc.VibrationChamberTileEntity;

public class VibrationChamberContainer extends AEBaseContainer implements IProgressProvider {

    public static ContainerType<VibrationChamberContainer> TYPE;

    private static final ContainerHelper<VibrationChamberContainer, VibrationChamberTileEntity> helper = new ContainerHelper<>(
            VibrationChamberContainer::new, VibrationChamberTileEntity.class);

    public static VibrationChamberContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final VibrationChamberTileEntity vibrationChamber;
    @GuiSync(0)
    public int burnSpeed = 0;
    @GuiSync(1)
    public int remainingBurnTime = 0;

    public VibrationChamberContainer(int id, final PlayerInventory ip,
            final VibrationChamberTileEntity vibrationChamber) {
        super(TYPE, id, ip, vibrationChamber, null);
        this.vibrationChamber = vibrationChamber;

        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.FUEL,
                vibrationChamber.getInternalInventory(), 0, 80, 37, this.getPlayerInventory()));

        this.bindPlayerInventory(ip, 0, 166 - /* height of player inventory */82);
    }

    @Override
    public void detectAndSendChanges() {
        if (isServer()) {
            this.remainingBurnTime = this.vibrationChamber.getMaxBurnTime() <= 0 ? 0
                    : (int) (100.0 * this.vibrationChamber.getBurnTime() / this.vibrationChamber.getMaxBurnTime());
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
        return VibrationChamberTileEntity.MAX_BURN_SPEED;
    }
}
