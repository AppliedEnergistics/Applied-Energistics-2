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

import appeng.blockentity.misc.VibrationChamberBlockEntity;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.RestrictedInputSlot;

/**
 * @see appeng.client.gui.implementations.VibrationChamberScreen
 */
public class VibrationChamberMenu extends AEBaseMenu implements IProgressProvider {

    public static final MenuType<VibrationChamberMenu> TYPE = MenuTypeBuilder
            .create(VibrationChamberMenu::new, VibrationChamberBlockEntity.class)
            .build("vibrationchamber");

    private final VibrationChamberBlockEntity vibrationChamber;
    @GuiSync(0)
    public double currentFuelTicksPerTick = 0;
    @GuiSync(1)
    public int remainingBurnTime = 0;

    public VibrationChamberMenu(int id, Inventory ip,
            VibrationChamberBlockEntity vibrationChamber) {
        super(TYPE, id, ip, vibrationChamber);
        this.vibrationChamber = vibrationChamber;

        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.FUEL,
                vibrationChamber.getInternalInventory(), 0), SlotSemantics.MACHINE_INPUT);

        this.createPlayerInventorySlots(ip);
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide()) {
            this.remainingBurnTime = this.vibrationChamber.getFuelItemFuelTicks() <= 0 ? 0
                    : (int) (100.0 * this.vibrationChamber.getRemainingFuelTicks()
                            / this.vibrationChamber.getFuelItemFuelTicks());
            this.currentFuelTicksPerTick = this.remainingBurnTime <= 0 ? 0
                    : this.vibrationChamber.getCurrentFuelTicksPerTick();

        }
        super.broadcastChanges();
    }

    @Override
    public int getCurrentProgress() {
        return (int) (this.currentFuelTicksPerTick * 100);
    }

    /**
     * @return A percentage value [0,100] to indicate how much of the current fuel item still remains.
     */
    public int getRemainingBurnTime() {
        return this.remainingBurnTime;
    }

    @Override
    public int getMaxProgress() {
        return (int) (vibrationChamber.getMaxFuelTicksPerTick() * 100);
    }

    /**
     * Current amount of power being generated.
     */
    public double getPowerPerTick() {
        return currentFuelTicksPerTick * vibrationChamber.getEnergyPerFuelTick();
    }
}
