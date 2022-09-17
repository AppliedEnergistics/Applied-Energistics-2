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

package appeng.container.slot;


import appeng.container.AEBaseContainer;
import appeng.util.inv.WrapperInvItemHandler;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;


public class SlotCraftingMatrix extends AppEngSlot {
    private final AEBaseContainer c;
    private final IInventory wrappedInventory;

    public SlotCraftingMatrix(final AEBaseContainer c, final IItemHandler par1iInventory, final int par2, final int par3, final int par4) {
        super(par1iInventory, par2, par3, par4);
        this.c = c;
        this.wrappedInventory = new WrapperInvItemHandler(par1iInventory);
    }

    @Override
    public void clearStack() {
        super.clearStack();
        this.c.onCraftMatrixChanged(this.wrappedInventory);
    }

    @Override
    public void putStack(final ItemStack par1ItemStack) {
        super.putStack(par1ItemStack);
        this.c.onCraftMatrixChanged(this.wrappedInventory);
    }

    @Override
    public boolean isPlayerSide() {
        return true;
    }

    @Override
    public ItemStack decrStackSize(final int par1) {
        final ItemStack is = super.decrStackSize(par1);
        this.c.onCraftMatrixChanged(this.wrappedInventory);
        return is;
    }
}
