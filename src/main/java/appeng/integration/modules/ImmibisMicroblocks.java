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

package appeng.integration.modules;


import appeng.api.AEApi;
import appeng.api.definitions.IBlockDefinition;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.core.AELog;
import appeng.helpers.Reflected;
import appeng.integration.IIntegrationModule;
import appeng.integration.IntegrationHelper;
import appeng.integration.abstraction.IImmibisMicroblocks;
import com.google.common.base.Optional;
import mods.immibis.core.api.multipart.ICoverSystem;
import mods.immibis.core.api.multipart.IMultipartTile;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.lang.reflect.Method;


public class ImmibisMicroblocks implements IImmibisMicroblocks, IIntegrationModule
{
	@Reflected
	public static ImmibisMicroblocks instance;

	private boolean canConvertTiles = false;

	private Class<?> MicroblockAPIUtils;
	private Method mergeIntoMicroblockContainer;

	@Reflected
	public ImmibisMicroblocks()
	{
		IntegrationHelper.testClassExistence( this, mods.immibis.core.api.multipart.IMultipartTile.class );
		IntegrationHelper.testClassExistence( this, mods.immibis.core.api.multipart.ICoverSystem.class );
		IntegrationHelper.testClassExistence( this, mods.immibis.core.api.multipart.IPartContainer.class );
	}

	@Override
	public void init() throws Throwable
	{
		try
		{
			this.MicroblockAPIUtils = Class.forName( "mods.immibis.microblocks.api.MicroblockAPIUtils" );
			this.mergeIntoMicroblockContainer = this.MicroblockAPIUtils.getMethod( "mergeIntoMicroblockContainer", ItemStack.class, EntityPlayer.class, World.class, int.class, int.class, int.class, int.class, Block.class, int.class );
			this.canConvertTiles = true;
		}
		catch( final Throwable t )
		{
			AELog.debug( t );
		}
	}

	@Override
	public void postInit()
	{

	}

	@Override
	public IPartHost getOrCreateHost( final EntityPlayer player, final int side, final TileEntity te )
	{
		final World w = te.getWorldObj();
		final int x = te.xCoord;
		final int y = te.yCoord;
		final int z = te.zCoord;
		final boolean isPartItem = player != null && player.getHeldItem() != null && player.getHeldItem().getItem() instanceof IPartItem;

		if( te instanceof IMultipartTile && this.canConvertTiles && isPartItem )
		{
			final IBlockDefinition multiPart = AEApi.instance().definitions().blocks().multiPart();
			final Optional<Block> maybeMultiPartBlock = multiPart.maybeBlock();
			final Optional<ItemStack> maybeMultiPartStack = multiPart.maybeStack( 1 );

			final boolean multiPartPresent = maybeMultiPartBlock.isPresent() && maybeMultiPartStack.isPresent();

			if( multiPartPresent )
			{
				final Block multiPartBlock = maybeMultiPartBlock.get();
				final ItemStack multiPartStack = maybeMultiPartStack.get();

				try
				{
					// ItemStack.class, EntityPlayer.class, World.class,
					// int.class, int.class, int.class, int.class, Block.class, int.class );
					this.mergeIntoMicroblockContainer.invoke( null, multiPartStack, player, w, x, y, z, side, multiPartBlock, 0 );
				}
				catch( final Throwable e )
				{
					this.canConvertTiles = false;
					return null;
				}
			}
		}

		final TileEntity tx = w.getTileEntity( x, y, z );
		if( tx instanceof IPartHost )
		{
			return (IPartHost) tx;
		}

		return null;
	}

	@Override
	public boolean leaveParts( final TileEntity te )
	{
		if( te instanceof IMultipartTile )
		{
			final ICoverSystem ci = ( (IMultipartTile) te ).getCoverSystem();
			if( ci != null )
			{
				ci.convertToContainerBlock();
			}

			return true;
		}
		return false;
	}
}
