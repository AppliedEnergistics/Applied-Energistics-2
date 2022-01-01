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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.SecurityPermissions;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.MolecularAssemblerPatternSlot;
import appeng.menu.slot.OutputSlot;
import appeng.menu.slot.RestrictedInputSlot;

/**
 * @see appeng.client.gui.implementations.MolecularAssemblerScreen
 */
public class MolecularAssemblerMenu extends UpgradeableMenu<MolecularAssemblerBlockEntity>
        implements IProgressProvider {

    public static final MenuType<MolecularAssemblerMenu> TYPE = MenuTypeBuilder
            .create(MolecularAssemblerMenu::new, MolecularAssemblerBlockEntity.class)
            .build("molecular_assembler");

    private static final int MAX_CRAFT_PROGRESS = 100;
    private final MolecularAssemblerBlockEntity molecularAssembler;
    @GuiSync(4)
    public int craftProgress = 0;

    private Slot encodedPatternSlot;

    public MolecularAssemblerMenu(int id, Inventory playerInv, MolecularAssemblerBlockEntity be) {
        super(TYPE, id, playerInv, be);
        this.molecularAssembler = be;
    }

    public boolean isValidItemForSlot(int slotIndex, ItemStack i) {
        var details = molecularAssembler.getCurrentPattern();
        if (details != null) {
            return details.isItemValid(slotIndex, AEItemKey.of(i), molecularAssembler.getLevel());
        }

        return false;
    }

    @Override
    protected void setupConfig() {
        var mac = this.getHost().getSubInventory(MolecularAssemblerBlockEntity.INV_MAIN);

        for (int i = 0; i < 9; i++) {
            this.addSlot(new MolecularAssemblerPatternSlot(this, mac, i), SlotSemantics.MACHINE_CRAFTING_GRID);
        }

        encodedPatternSlot = this.addSlot(
                new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ENCODED_AE_CRAFTING_PATTERN, mac, 10),
                SlotSemantics.ENCODED_PATTERN);

        this.addSlot(new OutputSlot(mac, 9, null), SlotSemantics.MACHINE_OUTPUT);

        setupUpgrades();
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        this.craftProgress = this.molecularAssembler.getCraftingProgress();

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
            for (Slot otherSlot : slots) {
                if (otherSlot != s && otherSlot instanceof AppEngSlot) {
                    ((AppEngSlot) otherSlot).resetCachedValidation();
                }
            }
        }

    }

}
