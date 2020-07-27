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

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.parts.IPart;
import appeng.api.util.IConfigManager;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.FakeTypeOnlySlot;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalFakeSlot;
import appeng.container.slot.OptionalTypeOnlyFakeSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.items.contents.NetworkToolViewer;
import appeng.items.tools.NetworkToolItem;
import appeng.parts.automation.ExportBusPart;
import appeng.util.Platform;

public class UpgradeableContainer extends AEBaseContainer implements IOptionalSlotHost {

    public static ScreenHandlerType<UpgradeableContainer> TYPE;

    private static final ContainerHelper<UpgradeableContainer, IUpgradeableHost> helper = new ContainerHelper<>(
            UpgradeableContainer::new, IUpgradeableHost.class, SecurityPermissions.BUILD);

    public static UpgradeableContainer fromNetwork(int windowId, PlayerInventory inv, PacketByteBuf buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

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

    public UpgradeableContainer(int id, final PlayerInventory ip, final IUpgradeableHost te) {
        this(TYPE, id, ip, te);
    }

    public UpgradeableContainer(ScreenHandlerType<?> containerType, int id, final PlayerInventory ip,
            final IUpgradeableHost te) {
        super(containerType, id, ip, (BlockEntity) (te instanceof BlockEntity ? te : null),
                (IPart) (te instanceof IPart ? te : null));
        this.upgradeable = te;

        World w = null;
        int xCoord = 0;
        int yCoord = 0;
        int zCoord = 0;

        if (te instanceof BlockEntity) {
            final BlockEntity myTile = (BlockEntity) te;
            w = myTile.getWorld();
            xCoord = myTile.getPos().getX();
            yCoord = myTile.getPos().getY();
            zCoord = myTile.getPos().getZ();
        }

        if (te instanceof IPart) {
            final BlockEntity mk = te.getTile();
            w = mk.getWorld();
            xCoord = mk.getPos().getX();
            yCoord = mk.getPos().getY();
            zCoord = mk.getPos().getZ();
        }

        final Inventory pi = this.getPlayerInv();
        for (int x = 0; x < pi.size(); x++) {
            final ItemStack pii = pi.getStack(x);
            if (!pii.isEmpty() && pii.getItem() instanceof NetworkToolItem) {
                this.lockPlayerInventorySlot(x);
                this.tbSlot = x;
                this.tbInventory = (NetworkToolViewer) ((IGuiItem) pii.getItem()).getGuiObject(pii, x, w,
                        new BlockPos(xCoord, yCoord, zCoord));
                break;
            }
        }

        if (this.hasToolbox()) {
            for (int v = 0; v < 3; v++) {
                for (int u = 0; u < 3; u++) {
                    this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES,
                            this.tbInventory.getInternalInventory(), u + v * 3, 186 + u * 18,
                            this.getHeight() - 82 + v * 18, this.getPlayerInventory())).setPlayerSide());
                }
            }
        }

        this.setupConfig();

        this.bindPlayerInventory(ip, 0, this.getHeight() - /* height of player inventory */82);
    }

    public boolean hasToolbox() {
        return this.tbInventory != null;
    }

    protected int getHeight() {
        return 184;
    }

    protected void setupConfig() {
        this.setupUpgrades();

        final FixedItemInv inv = this.getUpgradeable().getInventoryByName("config");
        final int y = 40;
        final int x = 80;
        this.addSlot(new FakeTypeOnlySlot(inv, 0, x, y));

        if (this.supportCapacity()) {
            this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 1, x, y, -1, 0, 1));
            this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 2, x, y, 1, 0, 1));
            this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 3, x, y, 0, -1, 1));
            this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 4, x, y, 0, 1, 1));

            this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 5, x, y, -1, -1, 2));
            this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 6, x, y, 1, -1, 2));
            this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 7, x, y, -1, 1, 2));
            this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 8, x, y, 1, 1, 2));
        }
    }

    protected void setupUpgrades() {
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
    }

    protected boolean supportCapacity() {
        return true;
    }

    public int availableUpgrades() {
        return 4;
    }

    @Override
    public void sendContentUpdates() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (Platform.isServer()) {
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
            final ItemStack currentItem = this.getPlayerInv().getStack(this.tbSlot);

            if (currentItem != this.tbInventory.getItemStack()) {
                if (!currentItem.isEmpty()) {
                    if (ItemStack.areItemsEqual(this.tbInventory.getItemStack(), currentItem)) {
                        this.getPlayerInv().setStack(this.tbSlot, this.tbInventory.getItemStack());
                    } else {
                        this.setValidContainer(false);
                    }
                } else {
                    this.setValidContainer(false);
                }
            }
        }
    }

    protected void standardDetectAndSendChanges() {
        super.sendContentUpdates();
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        final int upgrades = this.getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY);

        if (idx == 1 && upgrades > 0) {
            return true;
        }
        if (idx == 2 && upgrades > 1) {
            return true;
        }

        return false;
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

    protected IUpgradeableHost getUpgradeable() {
        return this.upgradeable;
    }
}
