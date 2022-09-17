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


import appeng.api.config.*;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.storage.TileIOPort;
import appeng.util.Platform;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;


public class ContainerIOPort extends ContainerUpgradeable {

    @GuiSync(2)
    public FullnessMode fMode = FullnessMode.EMPTY;
    @GuiSync(3)
    public OperationMode opMode = OperationMode.EMPTY;

    public ContainerIOPort(final InventoryPlayer ip, final TileIOPort te) {
        super(ip, te);
    }

    @Override
    protected int getHeight() {
        return 166;
    }

    @Override
    protected void setupConfig() {
        int offX = 19;
        int offY = 17;

        final IItemHandler cells = this.getUpgradeable().getInventoryByName("cells");

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 2; x++) {
                this.addSlotToContainer(
                        new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.STORAGE_CELLS, cells, x + y * 2, offX + x * 18, offY + y * 18, this
                                .getInventoryPlayer()));
            }
        }

        offX = 122;
        offY = 17;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 2; x++) {
                this.addSlotToContainer(
                        new SlotOutput(cells, 6 + x + y * 2, offX + x * 18, offY + y * 18, SlotRestrictedInput.PlacableItemType.STORAGE_CELLS.IIcon));
            }
        }

        final IItemHandler upgrades = this.getUpgradeable().getInventoryByName("upgrades");
        this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8, this.getInventoryPlayer()))
                .setNotDraggable());
        this.addSlotToContainer(
                (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18, this.getInventoryPlayer()))
                        .setNotDraggable());
        this.addSlotToContainer(
                (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2, this.getInventoryPlayer()))
                        .setNotDraggable());
    }

    @Override
    protected boolean supportCapacity() {
        return false;
    }

    @Override
    public int availableUpgrades() {
        return 3;
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (Platform.isServer()) {
            this.setOperationMode((OperationMode) this.getUpgradeable().getConfigManager().getSetting(Settings.OPERATION_MODE));
            this.setFullMode((FullnessMode) this.getUpgradeable().getConfigManager().getSetting(Settings.FULLNESS_MODE));
            this.setRedStoneMode((RedstoneMode) this.getUpgradeable().getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED));
        }

        this.standardDetectAndSendChanges();
    }

    public FullnessMode getFullMode() {
        return this.fMode;
    }

    private void setFullMode(final FullnessMode fMode) {
        this.fMode = fMode;
    }

    public OperationMode getOperationMode() {
        return this.opMode;
    }

    private void setOperationMode(final OperationMode opMode) {
        this.opMode = opMode;
    }
}
