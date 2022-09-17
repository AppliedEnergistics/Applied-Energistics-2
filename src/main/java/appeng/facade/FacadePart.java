/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.facade;


import appeng.api.AEApi;
import appeng.api.parts.IBoxProvider;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.util.AEPartLocation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


public class FacadePart implements IFacadePart, IBoxProvider {

    private final ItemStack facade;
    private final AEPartLocation side;

    public FacadePart(final ItemStack facade, final AEPartLocation side) {
        if (facade == null) {
            throw new IllegalArgumentException("Facade Part constructed on null item.");
        }
        this.facade = facade.copy();
        this.facade.setCount(1);
        this.side = side;
    }

    public static boolean isFacade(final ItemStack is) {
        return is.getItem() instanceof IFacadeItem;
    }

    @Override
    public ItemStack getItemStack() {
        return this.facade;
    }

    @Override
    public void getBoxes(final IPartCollisionHelper ch, final Entity e) {
        if (e instanceof EntityLivingBase) {
            // prevent weird snag behavior
            ch.addBox(0.0, 0.0, 14, 16.0, 16.0, 16.0);
        } else {
            // the box is 15.9 for transition planes to pick up collision events.
            ch.addBox(0.0, 0.0, 14, 16.0, 16.0, 15.9);
        }
    }

    @Override
    public AEPartLocation getSide() {
        return this.side;
    }

    @Override
    public Item getItem() {
        final ItemStack is = this.getTextureItem();
        if (is.isEmpty()) {
            return Items.AIR;
        }
        return is.getItem();
    }

    @Override
    public int getItemDamage() {
        final ItemStack is = this.getTextureItem();
        if (is.isEmpty()) {
            return 0;
        }
        return is.getItemDamage();
    }

    @Override
    public boolean notAEFacade() {
        return !(this.facade.getItem() instanceof IFacadeItem);
    }

    @Override
    public boolean isTransparent() {
        if (AEApi.instance().partHelper().getCableRenderMode().transparentFacades) {
            return true;
        }

        return this.getBlockState().isOpaqueCube();
    }

    @Override
    public ItemStack getTextureItem() {
        final Item maybeFacade = this.facade.getItem();

        // AE Facade
        if (maybeFacade instanceof IFacadeItem) {
            final IFacadeItem facade = (IFacadeItem) maybeFacade;

            return facade.getTextureItem(this.facade);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public IBlockState getBlockState() {
        final Item maybeFacade = this.facade.getItem();

        // AE Facade
        if (maybeFacade instanceof IFacadeItem) {
            final IFacadeItem facade = (IFacadeItem) maybeFacade;

            return facade.getTextureBlockState(this.facade);
        }

        return Blocks.GLASS.getDefaultState();
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        this.getBoxes(bch, null);
    }
}
