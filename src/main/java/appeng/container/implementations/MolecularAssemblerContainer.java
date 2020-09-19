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
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.container.interfaces.IProgressProvider;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.MolecularAssemblerPatternSlot;
import appeng.container.slot.OutputSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.core.Api;
import appeng.items.misc.EncodedPatternItem;
import appeng.tile.crafting.MolecularAssemblerTileEntity;

public class MolecularAssemblerContainer extends UpgradeableContainer implements IProgressProvider {

    public static ContainerType<MolecularAssemblerContainer> TYPE;

    private static final ContainerHelper<MolecularAssemblerContainer, MolecularAssemblerTileEntity> helper = new ContainerHelper<>(
            MolecularAssemblerContainer::new, MolecularAssemblerTileEntity.class);

    public static MolecularAssemblerContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private static final int MAX_CRAFT_PROGRESS = 100;
    private final MolecularAssemblerTileEntity tma;
    @GuiSync(4)
    public int craftProgress = 0;

    private Slot encodedPatternSlot;

    public MolecularAssemblerContainer(int id, final PlayerInventory ip, final MolecularAssemblerTileEntity te) {
        super(TYPE, id, ip, te);
        this.tma = te;
    }

    public boolean isValidItemForSlot(final int slotIndex, final ItemStack i) {
        final IItemHandler mac = this.getUpgradeable().getInventoryByName(MolecularAssemblerTileEntity.INVENTORY_MAIN);

        final ItemStack is = mac.getStackInSlot(10);
        if (is.isEmpty()) {
            return false;
        }

        if (is.getItem() instanceof EncodedPatternItem) {
            final World w = this.getTileEntity().getWorld();
            final ICraftingPatternDetails ph = Api.instance().crafting().decodePattern(is, w);
            if (ph.isCraftable()) {
                return ph.isValidItemForSlot(slotIndex, i, w);
            }
        }

        return false;
    }

    @Override
    protected int getHeight() {
        return 197;
    }

    @Override
    protected void setupConfig() {
        int offX = 29;
        int offY = 30;

        final IItemHandler mac = this.getUpgradeable().getInventoryByName(MolecularAssemblerTileEntity.INVENTORY_MAIN);

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                final MolecularAssemblerPatternSlot s = new MolecularAssemblerPatternSlot(this, mac, x + y * 3,
                        offX + x * 18, offY + y * 18);
                this.addSlot(s);
            }
        }

        offX = 126;
        offY = 16;

        encodedPatternSlot = this
                .addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ENCODED_CRAFTING_PATTERN, mac, 10,
                        offX, offY, this.getPlayerInventory()));
        this.addSlot(new OutputSlot(mac, 9, offX, offY + 32, -1));

        offX = 122;
        offY = 17;

        final IItemHandler upgrades = this.getUpgradeable().getInventoryByName("upgrades");
        this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, 0, 187, 8,
                this.getPlayerInventory())).setNotDraggable());
        this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18,
                this.getPlayerInventory())).setNotDraggable());
        this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, 2, 187,
                8 + 18 * 2, this.getPlayerInventory())).setNotDraggable());
        this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, 3, 187,
                8 + 18 * 3, this.getPlayerInventory())).setNotDraggable());
        this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, 4, 187,
                8 + 18 * 4, this.getPlayerInventory())).setNotDraggable());
    }

    @Override
    protected boolean supportCapacity() {
        return false;
    }

    @Override
    public int availableUpgrades() {
        return 5;
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (isServer()) {
            this.setRedStoneMode(
                    (RedstoneMode) this.getUpgradeable().getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED));
        }

        this.craftProgress = this.tma.getCraftingProgress();

        this.standardDetectAndSendChanges();
    }

    @Override
    public int getCurrentProgress() {
        return this.craftProgress;
    }

    @Override
    public int getMaxProgress() {
        return MAX_CRAFT_PROGRESS;
    }

    @Override
    public void onSlotChange(Slot s) {

        // If the pattern changes, the crafting grid slots lose validity
        if (s == encodedPatternSlot) {
            for (Slot otherSlot : inventorySlots) {
                if (otherSlot != s && otherSlot instanceof AppEngSlot) {
                    ((AppEngSlot) otherSlot).setIsValid(AppEngSlot.CalculatedValidity.NotAvailable);
                }
            }
        }

    }

}
