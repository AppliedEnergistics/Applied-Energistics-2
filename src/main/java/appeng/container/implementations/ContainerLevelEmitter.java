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
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
import appeng.parts.automation.PartLevelEmitter;
import appeng.util.Platform;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;


public class ContainerLevelEmitter extends ContainerUpgradeable {

    private final PartLevelEmitter lvlEmitter;

    @SideOnly(Side.CLIENT)
    private GuiTextField textField;
    @GuiSync(2)
    public LevelType lvType;
    @GuiSync(3)
    public long EmitterValue = -1;
    @GuiSync(4)
    public YesNo cmType;

    public ContainerLevelEmitter(final InventoryPlayer ip, final PartLevelEmitter te) {
        super(ip, te);
        this.lvlEmitter = te;
    }

    @SideOnly(Side.CLIENT)
    public void setTextField(final GuiTextField level) {
        this.textField = level;
        this.textField.setText(String.valueOf(this.EmitterValue));
    }

    public void setLevel(final long l, final EntityPlayer player) {
        this.lvlEmitter.setReportingValue(l);
        this.EmitterValue = l;
    }

    @Override
    protected void setupConfig() {
        final IItemHandler upgrades = this.getUpgradeable().getInventoryByName("upgrades");
        if (this.availableUpgrades() > 0) {
            this.addSlotToContainer(
                    (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8, this.getInventoryPlayer()))
                            .setNotDraggable());
        }
        if (this.availableUpgrades() > 1) {
            this.addSlotToContainer(
                    (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18, this.getInventoryPlayer()))
                            .setNotDraggable());
        }
        if (this.availableUpgrades() > 2) {
            this.addSlotToContainer(
                    (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2, this.getInventoryPlayer()))
                            .setNotDraggable());
        }
        if (this.availableUpgrades() > 3) {
            this.addSlotToContainer(
                    (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3, this.getInventoryPlayer()))
                            .setNotDraggable());
        }

        final IItemHandler inv = this.getUpgradeable().getInventoryByName("config");
        final int y = 40;
        final int x = 80 + 44;
        this.addSlotToContainer(new SlotFakeTypeOnly(inv, 0, x, y));
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
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (Platform.isServer()) {
            this.EmitterValue = this.lvlEmitter.getReportingValue();
            this.setCraftingMode((YesNo) this.getUpgradeable().getConfigManager().getSetting(Settings.CRAFT_VIA_REDSTONE));
            this.setLevelMode((LevelType) this.getUpgradeable().getConfigManager().getSetting(Settings.LEVEL_TYPE));
            this.setFuzzyMode((FuzzyMode) this.getUpgradeable().getConfigManager().getSetting(Settings.FUZZY_MODE));
            this.setRedStoneMode((RedstoneMode) this.getUpgradeable().getConfigManager().getSetting(Settings.REDSTONE_EMITTER));
        }

        this.standardDetectAndSendChanges();
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        if (field.equals("EmitterValue")) {
            if (this.textField != null) {
                this.textField.setText(String.valueOf(this.EmitterValue));
            }
        }
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
