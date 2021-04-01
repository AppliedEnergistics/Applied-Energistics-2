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
import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.config.FullnessMode;
import appeng.api.config.OperationMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.OutputSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.tile.storage.IOPortBlockEntity;

public class IOPortContainer extends UpgradeableContainer {

    public static ContainerType<IOPortContainer> TYPE;

    private static final ContainerHelper<IOPortContainer, IOPortBlockEntity> helper = new ContainerHelper<>(
            IOPortContainer::new, IOPortBlockEntity.class, SecurityPermissions.BUILD);

    public static IOPortContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    @GuiSync(2)
    public FullnessMode fMode = FullnessMode.EMPTY;
    @GuiSync(3)
    public OperationMode opMode = OperationMode.EMPTY;

    public IOPortContainer(int id, final PlayerInventory ip, final IOPortBlockEntity te) {
        super(TYPE, id, ip, te);
    }

    @Override
    protected int getHeight() {
        return 166;
    }

    @Override
    protected void setupConfig() {
        int offX = 19;
        int offY = 17;

        final FixedItemInv cells = this.getUpgradeable().getInventoryByName("cells");

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 2; x++) {
                this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.STORAGE_CELLS, cells,
                        x + y * 2, offX + x * 18, offY + y * 18, this.getPlayerInventory()));
            }
        }

        offX = 122;
        offY = 17;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 2; x++) {
                this.addSlot(new OutputSlot(cells, 6 + x + y * 2, offX + x * 18, offY + y * 18,
                        RestrictedInputSlot.PlacableItemType.STORAGE_CELLS.IIcon));
            }
        }

        final FixedItemInv upgrades = this.getUpgradeable().getInventoryByName("upgrades");
        this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, 0, 187, 8,
                this.getPlayerInventory())).setNotDraggable());
        this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18,
                this.getPlayerInventory())).setNotDraggable());
        this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, 2, 187,
                8 + 18 * 2, this.getPlayerInventory())).setNotDraggable());
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

        if (isServer()) {
            this.setOperationMode(
                    (OperationMode) this.getUpgradeable().getConfigManager().getSetting(Settings.OPERATION_MODE));
            this.setFullMode(
                    (FullnessMode) this.getUpgradeable().getConfigManager().getSetting(Settings.FULLNESS_MODE));
            this.setRedStoneMode(
                    (RedstoneMode) this.getUpgradeable().getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED));
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
