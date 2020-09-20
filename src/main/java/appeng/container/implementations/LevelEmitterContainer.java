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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerType;

import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.config.FuzzyMode;
import appeng.api.config.LevelType;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.FakeTypeOnlySlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.parts.automation.LevelEmitterPart;

public class LevelEmitterContainer extends UpgradeableContainer {

    public static ScreenHandlerType<LevelEmitterContainer> TYPE;

    private static final ContainerHelper<LevelEmitterContainer, LevelEmitterPart> helper = new ContainerHelper<>(
            LevelEmitterContainer::new, LevelEmitterPart.class, SecurityPermissions.BUILD);

    public static LevelEmitterContainer fromNetwork(int windowId, PlayerInventory inv, PacketByteBuf buf) {
        return helper.fromNetwork(windowId, inv, buf, (host, container, buffer) -> {
            container.reportingValue = buffer.readVarLong();
        });
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator, (host, buffer) -> {
            buffer.writeVarLong(host.getReportingValue());
        });
    }

    private final LevelEmitterPart lvlEmitter;

    @GuiSync(2)
    public LevelType lvType;
    @GuiSync(3)
    public YesNo cmType;

    // Only synced once on container-open, and only used on client
    private long reportingValue;

    public LevelEmitterContainer(int id, final PlayerInventory ip, final LevelEmitterPart te) {
        super(TYPE, id, ip, te);
        this.lvlEmitter = te;
    }

    public long getReportingValue() {
        return reportingValue;
    }

    public void setReportingValue(long reportingValue) {
        if (isClient()) {
            if (reportingValue != this.reportingValue) {
                this.reportingValue = reportingValue;
                NetworkHandler.instance()
                        .sendToServer(new ConfigValuePacket("LevelEmitter.Value", String.valueOf(reportingValue)));
            }
        } else {
            this.lvlEmitter.setReportingValue(reportingValue);
        }
    }

    @Override
    protected void setupConfig() {
        final FixedItemInv upgrades = this.getUpgradeable().getInventoryByName("upgrades");
        if (this.availableUpgrades() > 0) {
            this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, 0, 187, 8,
                    this.getPlayerInventory())).setNotDraggable());
        }
        if (this.availableUpgrades() > 1) {
            this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, 1, 187,
                    8 + 18, this.getPlayerInventory())).setNotDraggable());
        }
        if (this.availableUpgrades() > 2) {
            this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, 2, 187,
                    8 + 18 * 2, this.getPlayerInventory())).setNotDraggable());
        }
        if (this.availableUpgrades() > 3) {
            this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, 3, 187,
                    8 + 18 * 3, this.getPlayerInventory())).setNotDraggable());
        }

        final FixedItemInv inv = this.getUpgradeable().getInventoryByName("config");
        final int y = 40;
        final int x = 80 + 57;
        this.addSlot(new FakeTypeOnlySlot(inv, 0, x, y));
    }

    @Override
    protected boolean supportCapacity() {
        return false;
    }

    @Override
    public int availableUpgrades() {
        return 1;
    }

    @Override
    public void sendContentUpdates() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (isServer()) {
            this.setCraftingMode(
                    (YesNo) this.getUpgradeable().getConfigManager().getSetting(Settings.CRAFT_VIA_REDSTONE));
            this.setLevelMode((LevelType) this.getUpgradeable().getConfigManager().getSetting(Settings.LEVEL_TYPE));
            this.setFuzzyMode((FuzzyMode) this.getUpgradeable().getConfigManager().getSetting(Settings.FUZZY_MODE));
            this.setRedStoneMode(
                    (RedstoneMode) this.getUpgradeable().getConfigManager().getSetting(Settings.REDSTONE_EMITTER));
        }

        this.standardDetectAndSendChanges();
    }

    @Override
    public YesNo getCraftingMode() {
        return this.cmType;
    }

    @Override
    public void setCraftingMode(final YesNo cmType) {
        this.cmType = cmType;
    }

    public LevelType getLevelMode() {
        return this.lvType;
    }

    private void setLevelMode(final LevelType lvType) {
        this.lvType = lvType;
    }

}
