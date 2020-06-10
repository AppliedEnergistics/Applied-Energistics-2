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
import java.util.Optional;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.PlayerEntityMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;

import appeng.api.parts.IPart;
import appeng.integration.modules.waila.part.ChannelWailaDataProvider;
import appeng.integration.modules.waila.part.IPartWailaDataProvider;
import appeng.integration.modules.waila.part.P2PStateWailaDataProvider;
import appeng.integration.modules.waila.part.PartAccessor;
import appeng.integration.modules.waila.part.PartStackWailaDataProvider;
import appeng.integration.modules.waila.part.PowerStateWailaDataProvider;
import appeng.integration.modules.waila.part.StorageMonitorWailaDataProvider;
import appeng.integration.modules.waila.part.Tracer;


/**
 * Delegation provider for parts through {@link IPartWailaDataProvider}
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
		final IPartWailaDataProvider p2pState = new P2PStateWailaDataProvider();
		final IPartWailaDataProvider partStack = new PartStackWailaDataProvider();

		this.providers = Lists.newArrayList( channel, storageMonitor, powerState, partStack, p2pState );
	}

	@Override
	public ItemStack getWailaStack( final IWailaDataAccessor accessor, final IWailaConfigHandler config )
	{
		final TileEntity te = accessor.getTileEntity();
		final RayTraceResult mop = accessor.getMOP();

		final Optional<IPart> maybePart = this.accessor.getMaybePart( te, mop );

		if( maybePart.isPresent() )
		{
			final IPart part = maybePart.get();

			ItemStack wailaStack = ItemStack.EMPTY;

			for( final IPartWailaDataProvider provider : this.providers )
			{
				wailaStack = provider.getWailaStack( part, config, wailaStack );
			}
			return wailaStack;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public List<String> getWailaHead( final ItemStack itemStack, final List<String> currentToolTip, final IWailaDataAccessor accessor, final IWailaConfigHandler config )
	{
		final TileEntity te = accessor.getTileEntity();
		final RayTraceResult mop = accessor.getMOP();

		final Optional<IPart> maybePart = this.accessor.getMaybePart( te, mop );

		if( maybePart.isPresent() )
		{
			final IPart part = maybePart.get();

			for( final IPartWailaDataProvider provider : this.providers )
			{
				provider.getWailaHead( part, currentToolTip, accessor, config );
			}
		}

		return currentToolTip;
	}

	@Override
	public List<String> getWailaBody( final ItemStack itemStack, final List<String> currentToolTip, final IWailaDataAccessor accessor, final IWailaConfigHandler config )
	{
		final TileEntity te = accessor.getTileEntity();
		final RayTraceResult mop = accessor.getMOP();

		final Optional<IPart> maybePart = this.accessor.getMaybePart( te, mop );

		if( maybePart.isPresent() )
		{
			final IPart part = maybePart.get();

			for( final IPartWailaDataProvider provider : this.providers )
			{
				provider.getWailaBody( part, currentToolTip, accessor, config );
			}
		}

		return currentToolTip;
	}

	@Override
	public List<String> getWailaTail( final ItemStack itemStack, final List<String> currentToolTip, final IWailaDataAccessor accessor, final IWailaConfigHandler config )
	{
		final TileEntity te = accessor.getTileEntity();
		final RayTraceResult mop = accessor.getMOP();

		final Optional<IPart> maybePart = this.accessor.getMaybePart( te, mop );

		if( maybePart.isPresent() )
		{
			final IPart part = maybePart.get();

			for( final IPartWailaDataProvider provider : this.providers )
			{
				provider.getWailaTail( part, currentToolTip, accessor, config );
			}
		}

		return currentToolTip;
	}

	@Override
	public CompoundNBT getNBTData( final PlayerEntityMP player, final TileEntity te, final CompoundNBT tag, final World world, BlockPos pos )
	{
		final RayTraceResult mop = this.tracer.retraceBlock( world, player, pos );

		if( mop != null )
		{
			final Optional<IPart> maybePart = this.accessor.getMaybePart( te, mop );

			if( maybePart.isPresent() )
			{
				final IPart part = maybePart.get();

				for( final IPartWailaDataProvider provider : this.providers )
				{
					provider.getNBTData( player, part, te, tag, world, pos );
				}
			}
		}

		return tag;
	}
}
