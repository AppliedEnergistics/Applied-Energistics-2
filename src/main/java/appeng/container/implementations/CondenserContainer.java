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
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.container.interfaces.IProgressProvider;
import appeng.container.slot.OutputSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.tile.misc.CondenserTileEntity;

public class CondenserContainer extends AEBaseContainer implements IProgressProvider {

    public static ContainerType<CondenserContainer> TYPE;

    private static final ContainerHelper<CondenserContainer, CondenserTileEntity> helper = new ContainerHelper<>(
            CondenserContainer::new, CondenserTileEntity.class);

    private final CondenserTileEntity condenser;
    @GuiSync(0)
    public long requiredEnergy = 0;
    @GuiSync(1)
    public long storedPower = 0;
    @GuiSync(2)
    public CondenserOutput output = CondenserOutput.TRASH;

    public CondenserContainer(int id, final PlayerInventory ip, final CondenserTileEntity condenser) {
        super(TYPE, id, ip, condenser, null);
        this.condenser = condenser;

        IItemHandler inv = condenser.getInternalInventory();

        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.TRASH, inv, 0, 51, 52, ip));
        this.addSlot(new OutputSlot(inv, 1, 105, 52, -1));
        this.addSlot(
                (new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.STORAGE_COMPONENT, inv, 2, 101, 26, ip))
                        .setStackLimit(1));

        this.bindPlayerInventory(ip, 0, 197 - /* height of player inventory */82);
    }

    public static CondenserContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    @Override
    public void broadcastChanges() {
        if (isServer()) {
            final double maxStorage = this.condenser.getStorage();
            final double requiredEnergy = this.condenser.getRequiredPower();

            this.requiredEnergy = requiredEnergy == 0 ? (int) maxStorage : (int) Math.min(requiredEnergy, maxStorage);
            this.storedPower = (int) this.condenser.getStoredPower();
            this.output = (CondenserOutput) this.condenser.getConfigManager().getSetting(Settings.CONDENSER_OUTPUT);
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
