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

package appeng.integration.modules.waila;


import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;

import appeng.api.parts.IPart;
import appeng.integration.modules.waila.part.ChannelWailaDataProvider;
import appeng.integration.modules.waila.part.IPartWailaDataProvider;
import appeng.integration.modules.waila.part.PartAccessor;
import appeng.integration.modules.waila.part.PowerStateWailaDataProvider;
import appeng.integration.modules.waila.part.StorageMonitorWailaDataProvider;
import appeng.integration.modules.waila.part.Tracer;


/**
 * Delegation provider for parts through {@link appeng.integration.modules.waila.part.IPartWailaDataProvider}
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class PartWailaDataProvider implements IWailaDataProvider
{
	/**
	 * Contains all providers
	 */
	private final List<IPartWailaDataProvider> providers;

	/**
	 * Can access parts through view-hits
	 */
	private final PartAccessor accessor = new PartAccessor();

	/**
	 * Traces views hit on blocks
	 */
	private final Tracer tracer = new Tracer();

	/**
	 * Initializes the provider list with all wanted providers
	 */
	public PartWailaDataProvider()
	{
		final IPartWailaDataProvider channel = new ChannelWailaDataProvider();
		final IPartWailaDataProvider storageMonitor = new StorageMonitorWailaDataProvider();
		final IPartWailaDataProvider powerState = new PowerStateWailaDataProvider();

		this.providers = Lists.newArrayList( channel, storageMonitor, powerState );
	}

	@Override
	public ItemStack getWailaStack( IWailaDataAccessor accessor, IWailaConfigHandler config )
	{
		return null;
	}

	@Override
	public List<String> getWailaHead( ItemStack itemStack, List<String> currentToolTip, IWailaDataAccessor accessor, IWailaConfigHandler config )
	{
		final TileEntity te = accessor.getTileEntity();
		final MovingObjectPosition mop = accessor.getPosition();

		final Optional<IPart> maybePart = this.accessor.getMaybePart( te, mop );

		if ( maybePart.isPresent() )
		{
			final IPart part = maybePart.get();

			for ( IPartWailaDataProvider provider : this.providers )
			{
				provider.getWailaHead( part, currentToolTip, accessor, config );
			}
		}

		return currentToolTip;
	}

	@Override
	public List<String> getWailaBody( ItemStack itemStack, List<String> currentToolTip, IWailaDataAccessor accessor, IWailaConfigHandler config )
	{
		final TileEntity te = accessor.getTileEntity();
		final MovingObjectPosition mop = accessor.getPosition();

		final Optional<IPart> maybePart = this.accessor.getMaybePart( te, mop );

		if ( maybePart.isPresent() )
		{
			final IPart part = maybePart.get();

			for ( IPartWailaDataProvider provider : this.providers )
			{
				provider.getWailaBody( part, currentToolTip, accessor, config );
			}
		}

		return currentToolTip;
	}

	@Override
	public List<String> getWailaTail( ItemStack itemStack, List<String> currentToolTip, IWailaDataAccessor accessor, IWailaConfigHandler config )
	{
		final TileEntity te = accessor.getTileEntity();
		final MovingObjectPosition mop = accessor.getPosition();

		final Optional<IPart> maybePart = this.accessor.getMaybePart( te, mop );

		if ( maybePart.isPresent() )
		{
			final IPart part = maybePart.get();

			for ( IPartWailaDataProvider provider : this.providers )
			{
				provider.getWailaTail( part, currentToolTip, accessor, config );
			}
		}

		return currentToolTip;
	}

	@Override
	public NBTTagCompound getNBTData( EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z )
	{
		final MovingObjectPosition mop = this.tracer.retraceBlock( world, player, x, y, z );

		if ( mop != null )
		{
			final Optional<IPart> maybePart = this.accessor.getMaybePart( te, mop );

			if ( maybePart.isPresent() )
			{
				final IPart part = maybePart.get();

				for ( IPartWailaDataProvider provider : this.providers )
				{
					provider.getNBTData( player, part, te, tag, world, x, y, z );
				}
			}
		}

		return tag;
	}
}
