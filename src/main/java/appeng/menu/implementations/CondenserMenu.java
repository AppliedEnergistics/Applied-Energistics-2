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

import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.blockentity.misc.CondenserBlockEntity;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.OutputSlot;
import appeng.menu.slot.RestrictedInputSlot;

/**
 * @see appeng.client.gui.implementations.CondenserScreen
 */
public class CondenserMenu extends AEBaseMenu implements IProgressProvider {

    public static final MenuType<CondenserMenu> TYPE = MenuTypeBuilder
            .create(CondenserMenu::new, CondenserBlockEntity.class)
            .build("condenser");

    private final CondenserBlockEntity condenser;
    @GuiSync(0)
    public long requiredEnergy = 0;
    @GuiSync(1)
    public long storedPower = 0;
    @GuiSync(2)
    public CondenserOutput output = CondenserOutput.TRASH;

    public CondenserMenu(int id, final Inventory ip, final CondenserBlockEntity condenser) {
        super(TYPE, id, ip, condenser);
        this.condenser = condenser;

        var inv = condenser.getInternalInventory();

        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.TRASH, inv, 0),
                SlotSemantics.MACHINE_INPUT);
        this.addSlot(new OutputSlot(inv, 1, null), SlotSemantics.MACHINE_OUTPUT);
        this.addSlot(
                new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.STORAGE_COMPONENT, inv, 2)
                        .setStackLimit(1),
                SlotSemantics.STORAGE_CELL);

        this.createPlayerInventorySlots(ip);
    }

    @Override
    public void broadcastChanges() {
        if (isServer()) {
            final double maxStorage = this.condenser.getStorage();
            final double requiredEnergy = this.condenser.getRequiredPower();

            this.requiredEnergy = requiredEnergy == 0 ? (int) maxStorage : (int) Math.min(requiredEnergy, maxStorage);
            this.storedPower = (int) this.condenser.getStoredPower();
            this.output = this.condenser.getConfigManager().getSetting(Settings.CONDENSER_OUTPUT);
        }

        super.broadcastChanges();
    }

    @Override
    public int getCurrentProgress() {
        return (int) this.storedPower;
    }

    @Override
    public int getMaxProgress() {
        return (int) this.requiredEnergy;
    }

    public CondenserOutput getOutput() {
        return this.output;
    }

}
