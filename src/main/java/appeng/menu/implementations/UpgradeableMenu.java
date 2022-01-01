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
import net.minecraft.world.level.ItemLike;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.definitions.AEItems;
import appeng.menu.AEBaseMenu;
import appeng.menu.ToolboxMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.IOptionalSlotHost;
import appeng.menu.slot.OptionalFakeSlot;

public abstract class UpgradeableMenu<T extends IUpgradeableObject> extends AEBaseMenu implements IOptionalSlotHost {

    private final T host;
    @GuiSync(0)
    public RedstoneMode rsMode = RedstoneMode.IGNORE;
    @GuiSync(1)
    public FuzzyMode fzMode = FuzzyMode.IGNORE_ALL;
    @GuiSync(5)
    public YesNo cMode = YesNo.NO;
    @GuiSync(6)
    public SchedulingMode schedulingMode = SchedulingMode.DEFAULT;

    private final ToolboxMenu toolbox;

    public UpgradeableMenu(MenuType<?> menuType, int id, Inventory ip, T host) {
        super(menuType, id, ip, host);
        this.host = host;

        this.toolbox = new ToolboxMenu(this);

        this.setupConfig();

        this.createPlayerInventorySlots(ip);
    }

    protected abstract void setupConfig();

    protected final void setupUpgrades() {
        setupUpgrades(this.getHost().getUpgrades());
    }

    public ToolboxMenu getToolbox() {
        return toolbox;
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (isServer() && getHost() instanceof IConfigurableObject configurableObject) {
            this.loadSettingsFromHost(configurableObject.getConfigManager());
        }

        toolbox.tick();

        for (Object o : this.slots) {
            if (o instanceof OptionalFakeSlot fs) {
                if (!fs.isSlotEnabled() && !fs.getDisplayStack().isEmpty()) {
                    fs.clearStack();
                }
            }
        }

        this.standardDetectAndSendChanges();
    }

    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setFuzzyMode(cm.getSetting(Settings.FUZZY_MODE));
        this.setRedStoneMode(cm.getSetting(Settings.REDSTONE_CONTROLLED));
        if (cm.hasSetting(Settings.CRAFT_ONLY)) {
            this.setCraftingMode(cm.getSetting(Settings.CRAFT_ONLY));
        }
        if (cm.hasSetting(Settings.SCHEDULING_MODE)) {
            this.setSchedulingMode(cm.getSetting(Settings.SCHEDULING_MODE));
        }
    }

    protected void standardDetectAndSendChanges() {
        super.broadcastChanges();
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        int capacityUpgrades = this.getHost().getUpgrades().getInstalledUpgrades(AEItems.CAPACITY_CARD);
        return idx == 1 && capacityUpgrades >= 1
                || idx == 2 && capacityUpgrades >= 2;
    }

    public FuzzyMode getFuzzyMode() {
        return this.fzMode;
    }

    public void setFuzzyMode(FuzzyMode fzMode) {
        this.fzMode = fzMode;
    }

    public YesNo getCraftingMode() {
        return this.cMode;
    }

    public void setCraftingMode(YesNo cMode) {
        this.cMode = cMode;
    }

    public RedstoneMode getRedStoneMode() {
        return this.rsMode;
    }

    public void setRedStoneMode(RedstoneMode rsMode) {
        this.rsMode = rsMode;
    }

    public SchedulingMode getSchedulingMode() {
        return this.schedulingMode;
    }

    private void setSchedulingMode(SchedulingMode schedulingMode) {
        this.schedulingMode = schedulingMode;
    }

    public final T getHost() {
        return this.host;
    }

    public final IUpgradeInventory getUpgrades() {
        return getHost().getUpgrades();
    }

    public final boolean hasUpgrade(ItemLike upgradeCard) {
        return getUpgrades().isInstalled(upgradeCard);
    }

}
