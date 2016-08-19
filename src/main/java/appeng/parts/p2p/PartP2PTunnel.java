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

package appeng.parts.p2p;


import java.util.ArrayList;
import java.util.Collection;

import com.google.common.base.Optional;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.config.TunnelType;
import appeng.api.definitions.IParts;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AEPartLocation;
import appeng.core.AEConfig;
import appeng.me.GridAccessException;
import appeng.me.cache.P2PCache;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.parts.PartBasicState;
import appeng.util.Platform;


public abstract class PartP2PTunnel<T extends PartP2PTunnel> extends PartBasicState
{
	private final TunnelCollection type = new TunnelCollection<T>( null, this.getClass() );
	private boolean output;
	private long freq;

	public PartP2PTunnel( final ItemStack is )
	{
		super( is );
	}

	public TunnelCollection<T> getCollection( final Collection<PartP2PTunnel> collection, final Class<? extends PartP2PTunnel> c )
	{
		if( this.type.matches( c ) )
		{
			this.type.setSource( collection );
			return this.type;
		}

		return null;
	}

	T getInput()
	{
		if( this.getFrequency() == 0 )
		{
			return null;
		}

		try
		{
			final PartP2PTunnel tunnel = this.getProxy().getP2P().getInput( this.getFrequency() );
			if( this.getClass().isInstance( tunnel ) )
			{
				return (T) tunnel;
			}
		}
		catch( final GridAccessException e )
		{
			// :P
		}
		return null;
	}

	TunnelCollection<T> getOutputs() throws GridAccessException
	{
		if( this.getProxy().isActive() )
		{
			return (TunnelCollection<T>) this.getProxy().getP2P().getOutputs( this.getFrequency(), this.getClass() );
		}
		return new TunnelCollection( new ArrayList(), this.getClass() );
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{
		bch.addBox( 5, 5, 12, 11, 11, 13 );
		bch.addBox( 3, 3, 13, 13, 13, 14 );
		bch.addBox( 2, 2, 14, 14, 14, 16 );
	}

	@Override
	public ItemStack getItemStack( final PartItemStack type )
	{
		if( type == PartItemStack.World || type == PartItemStack.Network || type == PartItemStack.Wrench || type == PartItemStack.Pick )
		{
			return super.getItemStack( type );
		}

		final Optional<ItemStack> maybeMEStack = AEApi.instance().definitions().parts().p2PTunnelME().maybeStack( 1 );
		if( maybeMEStack.isPresent() )
		{
			return maybeMEStack.get();
		}

		return super.getItemStack( type );
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.setOutput( data.getBoolean( "output" ) );
		this.setFrequency( data.getLong( "freq" ) );
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );
		data.setBoolean( "output", this.isOutput() );
		data.setLong( "freq", this.getFrequency() );
	}

	@Override
	public int getCableConnectionLength()
	{
		return 1;
	}

	@Override
	public boolean useStandardMemoryCard()
	{
		return false;
	}

	@Override
	public boolean onPartActivate( final EntityPlayer player, final EnumHand hand, final Vec3d pos )
	{
		final ItemStack is = player.inventory.getCurrentItem();

		// UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor( is.getItem() );
		// AELog.info( "ID:" + id.toString() + " : " + is.getItemDamage() );

		final TunnelType tt = AEApi.instance().registries().p2pTunnel().getTunnelTypeByItem( is );
		if( is != null && is.getItem() instanceof IMemoryCard )
		{
			final IMemoryCard mc = (IMemoryCard) is.getItem();
			final NBTTagCompound data = mc.getData( is );

			final ItemStack newType = ItemStack.loadItemStackFromNBT( data );
			final long freq = data.getLong( "freq" );

			if( newType != null )
			{
				if( newType.getItem() instanceof IPartItem )
				{
					final IPart testPart = ( (IPartItem) newType.getItem() ).createPartFromItemStack( newType );
					if( testPart instanceof PartP2PTunnel )
					{
						this.getHost().removePart( this.getSide(), true );
						final AEPartLocation dir = this.getHost().addPart( newType, this.getSide(), player, hand );
						final IPart newBus = this.getHost().getPart( dir );

						if( newBus instanceof PartP2PTunnel )
						{
							final PartP2PTunnel newTunnel = (PartP2PTunnel) newBus;
							newTunnel.setOutput( true );

							try
							{
								final P2PCache p2p = newTunnel.getProxy().getP2P();
								p2p.updateFreq( newTunnel, freq );
							}
							catch( final GridAccessException e )
							{
								// :P
							}

							newTunnel.onTunnelNetworkChange();
						}

						mc.notifyUser( player, MemoryCardMessages.SETTINGS_LOADED );
						return true;
					}
				}
			}
			mc.notifyUser( player, MemoryCardMessages.INVALID_MACHINE );
		}
		else if( tt != null ) // attunement
		{
			ItemStack newType = null;

			final IParts parts = AEApi.instance().definitions().parts();

			switch( tt )
			{
				case LIGHT:
					for( final ItemStack stack : parts.p2PTunnelLight().maybeStack( 1 ).asSet() )
					{
						newType = stack;
					}
					break;

				/*
				 * case RF_POWER:
				 * for( ItemStack stack : parts.p2PTunnelRF().maybeStack( 1 ).asSet() )
				 * {
				 * newType = stack;
				 * }
				 * break;
				 */

				case FLUID:
					for( final ItemStack stack : parts.p2PTunnelLiquids().maybeStack( 1 ).asSet() )
					{
						newType = stack;
					}
					break;

				/*
				 * case IC2_POWER:
				 * for( ItemStack stack : parts.p2PTunnelEU().maybeStack( 1 ).asSet() )
				 * {
				 * newType = stack;
				 * }
				 * break;
				 */

				case ITEM:
					for( final ItemStack stack : parts.p2PTunnelItems().maybeStack( 1 ).asSet() )
					{
						newType = stack;
					}
					break;

				case ME:
					for( final ItemStack stack : parts.p2PTunnelME().maybeStack( 1 ).asSet() )
					{
						newType = stack;
					}
					break;

				case REDSTONE:
					for( final ItemStack stack : parts.p2PTunnelRedstone().maybeStack( 1 ).asSet() )
					{
						newType = stack;
					}
					break;

				/*
				 * case COMPUTER_MESSAGE:
				 * for( ItemStack stack : parts.p2PTunnelOpenComputers().maybeStack( 1 ).asSet() )
				 * {
				 * newType = stack;
				 * }
				 * break;
				 * case PRESSURE:
				 * for( ItemStack stack : parts.p2PTunnelPneumaticCraft().maybeStack( 1 ).asSet() )
				 * {
				 * newType = stack;
				 * }
				 * break;
				 */

				default:
					break;
			}

			if( newType != null && !Platform.isSameItem( newType, this.getItemStack() ) )
			{
				final boolean oldOutput = this.isOutput();
				final long myFreq = this.getFrequency();

				this.getHost().removePart( this.getSide(), false );
				final AEPartLocation dir = this.getHost().addPart( newType, this.getSide(), player, hand );
				final IPart newBus = this.getHost().getPart( dir );

				if( newBus instanceof PartP2PTunnel )
				{
					final PartP2PTunnel newTunnel = (PartP2PTunnel) newBus;
					newTunnel.setOutput( oldOutput );
					newTunnel.onTunnelNetworkChange();

					try
					{
						final P2PCache p2p = newTunnel.getProxy().getP2P();
						p2p.updateFreq( newTunnel, myFreq );
					}
					catch( final GridAccessException e )
					{
						// :P
					}
				}

				Platform.notifyBlocksOfNeighbors( this.getTile().getWorld(), this.getTile().getPos() );
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean onPartShiftActivate( final EntityPlayer player, final EnumHand hand, final Vec3d pos )
	{
		final ItemStack is = player.inventory.getCurrentItem();
		if( is != null && is.getItem() instanceof IMemoryCard )
		{
			final IMemoryCard mc = (IMemoryCard) is.getItem();
			final NBTTagCompound data = new NBTTagCompound();

			long newFreq = this.getFrequency();
			final boolean wasOutput = this.isOutput();
			this.setOutput( false );

			if( wasOutput || this.getFrequency() == 0 )
			{
				newFreq = System.currentTimeMillis();
			}

			try
			{
				this.getProxy().getP2P().updateFreq( this, newFreq );
			}
			catch( final GridAccessException e )
			{
				// :P
			}

			this.onTunnelConfigChange();

			final ItemStack p2pItem = this.getItemStack( PartItemStack.Wrench );
			final String type = p2pItem.getUnlocalizedName();

			p2pItem.writeToNBT( data );
			data.setLong( "freq", this.getFrequency() );

			mc.setMemoryCardContents( is, type + ".name", data );
			mc.notifyUser( player, MemoryCardMessages.SETTINGS_SAVED );
			return true;
		}
		return false;
	}

	public void onTunnelConfigChange()
	{
	}

	public void onTunnelNetworkChange()
	{

	}

	protected void queueTunnelDrain( final PowerUnits unit, final double f )
	{
		final double ae_to_tax = unit.convertTo( PowerUnits.AE, f * AEConfig.TUNNEL_POWER_LOSS );

		try
		{
			this.getProxy().getEnergy().extractAEPower( ae_to_tax, Actionable.MODULATE, PowerMultiplier.ONE );
		}
		catch( final GridAccessException e )
		{
			// :P
		}
	}

	public long getFrequency()
	{
		return this.freq;
	}

	public void setFrequency( final long freq )
	{
		this.freq = freq;
	}

	public boolean isOutput()
	{
		return this.output;
	}

	void setOutput( final boolean output )
	{
		this.output = output;
	}
}
