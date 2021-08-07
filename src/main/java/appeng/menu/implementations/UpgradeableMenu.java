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

import javax.annotation.Nonnull;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.util.IConfigManager;
import appeng.items.contents.NetworkToolViewer;
import appeng.items.tools.NetworkToolItem;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.IOptionalSlotHost;
import appeng.menu.slot.OptionalFakeSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.parts.automation.ExportBusPart;

public abstract class UpgradeableMenu extends AEBaseMenu implements IOptionalSlotHost {

    private final IUpgradeableHost upgradeable;
    @GuiSync(0)
    public RedstoneMode rsMode = RedstoneMode.IGNORE;
    @GuiSync(1)
    public FuzzyMode fzMode = FuzzyMode.IGNORE_ALL;
    @GuiSync(5)
    public YesNo cMode = YesNo.NO;
    @GuiSync(6)
    public SchedulingMode schedulingMode = SchedulingMode.DEFAULT;
    private int tbSlot;
    private NetworkToolViewer tbInventory;

    public UpgradeableMenu(MenuType<?> menuType, int id, final Inventory ip,
            final IUpgradeableHost te) {
        super(menuType, id, ip, te);
        this.upgradeable = te;

        final Container pi = this.getPlayerInventory();
        for (int x = 0; x < pi.getContainerSize(); x++) {
            final ItemStack pii = pi.getItem(x);
            if (!pii.isEmpty() && pii.getItem() instanceof NetworkToolItem) {
                this.lockPlayerInventorySlot(x);
                this.tbSlot = x;
                this.tbInventory = ((NetworkToolItem) pii.getItem()).getGuiObject(pii, x,
                        getPlayerInventory().player.level, null);
                break;
            }
        }

        if (this.hasToolbox()) {
            for (int i = 0; i < 9; i++) {
                RestrictedInputSlot slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES,
                        this.tbInventory.getInternalInventory(), i);
                // The toolbox is in the network tool that is part of the player inventory
                this.addSlot(slot, SlotSemantic.TOOLBOX);
            }
        }

        this.setupConfig();

        this.createPlayerInventorySlots(ip);
    }

    public boolean hasToolbox() {
        return this.tbInventory != null;
    }

    @Nonnull
    public Component getToolboxName() {
        return this.tbInventory != null ? this.tbInventory.getItemStack().getHoverName()
                : TextComponent.EMPTY;
    }

    protected abstract void setupConfig();

    protected final void setupUpgrades() {
        final IItemHandler upgrades = this.getUpgradeable().getUpgradeInventory();

        for (int i = 0; i < availableUpgrades(); i++) {
            RestrictedInputSlot slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades,
                    i);
            slot.setNotDraggable();
            this.addSlot(slot, SlotSemantic.UPGRADE);
        }
    }

    /**
     * Indicates whether capacity upgrades can be used to increase the number of filter slots in this UI.
     */
    protected boolean supportCapacity() {
        return true;
    }

    public int availableUpgrades() {
        return 4;
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (isServer()) {
            final IConfigManager cm = this.getUpgradeable().getConfigManager();
            this.loadSettingsFromHost(cm);
        }

        this.checkToolbox();

        for (final Object o : this.slots) {
            if (o instanceof OptionalFakeSlot) {
                final OptionalFakeSlot fs = (OptionalFakeSlot) o;
                if (!fs.isSlotEnabled() && !fs.getDisplayStack().isEmpty()) {
                    fs.clearStack();
                }
            }
        }

        this.standardDetectAndSendChanges();
    }

    protected void loadSettingsFromHost(final IConfigManager cm) {
        this.setFuzzyMode((FuzzyMode) cm.getSetting(Settings.FUZZY_MODE));
        this.setRedStoneMode((RedstoneMode) cm.getSetting(Settings.REDSTONE_CONTROLLED));
        if (this.getUpgradeable() instanceof ExportBusPart) {
            this.setCraftingMode((YesNo) cm.getSetting(Settings.CRAFT_ONLY));
            this.setSchedulingMode((SchedulingMode) cm.getSetting(Settings.SCHEDULING_MODE));
        }
    }

    protected void checkToolbox() {
        if (this.hasToolbox()) {
            final ItemStack currentItem = this.getPlayerInventory().getItem(this.tbSlot);

            if (currentItem != this.tbInventory.getItemStack()) {
                if (!currentItem.isEmpty()) {
                    if (ItemStack.isSame(this.tbInventory.getItemStack(), currentItem)) {
                        this.getPlayerInventory().setItem(this.tbSlot,
                                this.tbInventory.getItemStack());
                    } else {
                        this.setValidMenu(false);
                    }
                } else {
                    this.setValidMenu(false);
                }
            }
        }
    }

    protected void standardDetectAndSendChanges() {
        super.broadcastChanges();
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        int capacityUpgrades = this.getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY);
        return idx == 1 && capacityUpgrades >= 1
                || idx == 2 && capacityUpgrades >= 2;
    }

    public FuzzyMode getFuzzyMode() {
        return this.fzMode;
    }

    public void setFuzzyMode(final FuzzyMode fzMode) {
        this.fzMode = fzMode;
    }

    public YesNo getCraftingMode() {
        return this.cMode;
    }

    public void setCraftingMode(final YesNo cMode) {
        this.cMode = cMode;
    }

    public RedstoneMode getRedStoneMode() {
        return this.rsMode;
    }

    public void setRedStoneMode(final RedstoneMode rsMode) {
        this.rsMode = rsMode;
    }

    public SchedulingMode getSchedulingMode() {
        return this.schedulingMode;
    }

    private void setSchedulingMode(final SchedulingMode schedulingMode) {
        this.schedulingMode = schedulingMode;
    }

    public IUpgradeableHost getUpgradeable() {
        return this.upgradeable;
    }

    public final boolean hasUpgrade(Upgrades upgrade) {
        return this.upgradeable.getInstalledUpgrades(upgrade) > 0;
    }

}
