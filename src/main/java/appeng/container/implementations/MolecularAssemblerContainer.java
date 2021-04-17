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

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.container.SlotSemantic;
import appeng.container.guisync.GuiSync;
import appeng.container.interfaces.IProgressProvider;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.MolecularAssemblerPatternSlot;
import appeng.container.slot.OutputSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.core.Api;
import appeng.items.misc.EncodedPatternItem;
import appeng.tile.crafting.MolecularAssemblerTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

/**
 * @see appeng.client.gui.implementations.MolecularAssemblerScreen
 */
public class MolecularAssemblerContainer extends UpgradeableContainer implements IProgressProvider {

    public static final ContainerType<MolecularAssemblerContainer> TYPE = ContainerTypeBuilder
            .create(MolecularAssemblerContainer::new, MolecularAssemblerTileEntity.class)
            .build("molecular_assembler");

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
            if (ph != null && ph.isCraftable()) {
                return ph.isValidItemForSlot(slotIndex, i, w);
            }
        }

        return false;
    }

    @Override
    protected void setupConfig() {
        final IItemHandler mac = this.getUpgradeable().getInventoryByName(MolecularAssemblerTileEntity.INVENTORY_MAIN);

        for (int i = 0; i < 9; i++) {
            this.addSlot(new MolecularAssemblerPatternSlot(this, mac, i), SlotSemantic.MACHINE_CRAFTING_GRID);
        }

        encodedPatternSlot = this.addSlot(
                new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ENCODED_CRAFTING_PATTERN, mac, 10),
                SlotSemantic.ENCODED_PATTERN);

        this.addSlot(new OutputSlot(mac, 9, -1), SlotSemantic.MACHINE_OUTPUT);

        setupUpgrades();
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

        // If the pattern changes, the crafting grid slots have to be revalidated
        if (s == encodedPatternSlot) {
            for (Slot otherSlot : inventorySlots) {
                if (otherSlot != s && otherSlot instanceof AppEngSlot) {
                    ((AppEngSlot) otherSlot).resetCachedValidation();
                }
            }
        }

    }

}
