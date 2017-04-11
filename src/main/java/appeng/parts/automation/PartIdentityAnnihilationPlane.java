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

package appeng.parts.automation;


import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.client.texture.CableBusTextures;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;


public class PartIdentityAnnihilationPlane extends PartAnnihilationPlane
{
	private static final IIcon ACTIVE_ICON = CableBusTextures.BlockIdentityAnnihilationPlaneOn.getIcon();

	private static final float SILK_TOUCH_FACTOR = 16;

	public PartIdentityAnnihilationPlane( final ItemStack is )
	{
		super( is );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper rh, final RenderBlocks renderer )
	{
		this.renderStaticWithIcon( x, y, z, rh, renderer, ACTIVE_ICON );
	}

	@Override
	protected boolean isAnnihilationPlane( final TileEntity blockTileEntity, final ForgeDirection side )
	{
		if( blockTileEntity instanceof IPartHost )
		{
			final IPart p = ( (IPartHost) blockTileEntity ).getPart( side );
			return p != null && p.getClass() == this.getClass();
		}
		return false;
	}

	@Override
	protected float calculateEnergyUsage( final WorldServer w, final int x, final int y, final int z, final List<ItemStack> items )
	{
		final float requiredEnergy = super.calculateEnergyUsage( w, x, y, z, items );

		return requiredEnergy * SILK_TOUCH_FACTOR;
	}

	@Override
	protected List<ItemStack> obtainBlockDrops( final WorldServer w, final int x, final int y, final int z )
	{
		final EntityPlayer fakePlayer = Platform.getPlayer( w );
		final Block block = w.getBlock( x, y, z );
		final int blockMeta = w.getBlockMetadata( x, y, z );

		if( block.canSilkHarvest( w, fakePlayer, x, y, z, blockMeta ) )
		{
			final List<ItemStack> out = new ArrayList<ItemStack>( 1 );
			final Item item = Item.getItemFromBlock( block );

			if( item != null )
			{
				int meta = 0;
				if( item.getHasSubtypes() )
				{
					meta = blockMeta;
				}
				final ItemStack itemstack = new ItemStack( item, 1, meta );
				out.add( itemstack );
			}
			return out;
		}
		else
		{
			return super.obtainBlockDrops( w, x, y, z );
		}
	}
}
