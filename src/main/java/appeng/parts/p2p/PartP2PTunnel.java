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

package appeng.parts.p2p;


import java.util.ArrayList;
import java.util.Collection;

import com.google.common.base.Optional;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.core.AEConfig;
import appeng.me.GridAccessException;
import appeng.me.cache.P2PCache;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.parts.PartBasicState;
import appeng.util.Platform;


public abstract class PartP2PTunnel<T extends PartP2PTunnel> extends PartBasicState
{
	private final TunnelCollection type = new TunnelCollection<T>( null, this.getClass() );
	public boolean output;
	public long freq;

	public PartP2PTunnel( ItemStack is )
	{
		super( is );
	}

	public TunnelCollection<T> getCollection( Collection<PartP2PTunnel> collection, Class<? extends PartP2PTunnel> c )
	{
		if( this.type.matches( c ) )
		{
			this.type.setSource( collection );
			return this.type;
		}

		return null;
	}

	public T getInput()
	{
		if( this.freq == 0 )
		{
			return null;
		}

		PartP2PTunnel tunnel;
		try
		{
			tunnel = this.proxy.getP2P().getInput( this.freq );
			if( this.getClass().isInstance( tunnel ) )
			{
				return (T) tunnel;
			}
		}
		catch( GridAccessException e )
		{
			// :P
		}
		return null;
	}

	public TunnelCollection<T> getOutputs() throws GridAccessException
	{
		if( this.proxy.isActive() )
		{
			return (TunnelCollection<T>) this.proxy.getP2P().getOutputs( this.freq, this.getClass() );
		}
		return new TunnelCollection( new ArrayList(), this.getClass() );
	}

	@Override
	public void getBoxes( IPartCollisionHelper bch )
	{
		bch.addBox( 5, 5, 12, 11, 11, 13 );
		bch.addBox( 3, 3, 13, 13, 13, 14 );
		bch.addBox( 2, 2, 14, 14, 14, 16 );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( IPartRenderHelper rh, RenderBlocks renderer )
	{
		rh.setTexture( this.getTypeTexture() );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderInventoryBox( renderer );

		rh.setTexture( CableBusTextures.PartTunnelSides.getIcon(), CableBusTextures.PartTunnelSides.getIcon(), CableBusTextures.BlockP2PTunnel2.getIcon(), this.is.getIconIndex(), CableBusTextures.PartTunnelSides.getIcon(), CableBusTextures.PartTunnelSides.getIcon() );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderInventoryBox( renderer );
	}

	/**
	 * @return If enabled it returns the icon of an AE quartz block, else vanilla quartz block icon
	 */
	protected IIcon getTypeTexture()
	{
		final Optional<Block> maybeBlock = AEApi.instance().definitions().blocks().quartz().maybeBlock();
		if( maybeBlock.isPresent() )
		{
			return maybeBlock.get().getIcon( 0, 0 );
		}
		else
		{
			return Blocks.quartz_block.getIcon( 0, 0 );
		}
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer )
	{
		this.renderCache = rh.useSimplifiedRendering( x, y, z, this, this.renderCache );
		rh.setTexture( this.getTypeTexture() );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartTunnelSides.getIcon(), CableBusTextures.PartTunnelSides.getIcon(), CableBusTextures.BlockP2PTunnel2.getIcon(), this.is.getIconIndex(), CableBusTextures.PartTunnelSides.getIcon(), CableBusTextures.PartTunnelSides.getIcon() );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 3, 3, 13, 13, 13, 14 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.BlockP2PTunnel3.getIcon() );

		rh.setBounds( 6, 5, 12, 10, 11, 13 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 5, 6, 12, 11, 10, 13 );
		rh.renderBlock( x, y, z, renderer );

		this.renderLights( x, y, z, rh, renderer );
	}

	@Override
	public ItemStack getItemStack( PartItemStack type )
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
	public void readFromNBT( NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.output = data.getBoolean( "output" );
		this.freq = data.getLong( "freq" );
	}

	@Override
	public void writeToNBT( NBTTagCompound data )
	{
		super.writeToNBT( data );
		data.setBoolean( "output", this.output );
		data.setLong( "freq", this.freq );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 1;
	}

	@Override
	public boolean useStandardMemoryCard()
	{
		return false;
	}

	@Override
	public boolean onPartActivate( EntityPlayer player, Vec3 pos )
	{
		ItemStack is = player.inventory.getCurrentItem();

		// UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor( is.getItem() );
		// AELog.info( "ID:" + id.toString() + " : " + is.getItemDamage() );

		TunnelType tt = AEApi.instance().registries().p2pTunnel().getTunnelTypeByItem( is );
		if( is != null && is.getItem() instanceof IMemoryCard )
		{
			IMemoryCard mc = (IMemoryCard) is.getItem();
			NBTTagCompound data = mc.getData( is );

			ItemStack newType = ItemStack.loadItemStackFromNBT( data );
			long freq = data.getLong( "freq" );

			if( newType != null )
			{
				if( newType.getItem() instanceof IPartItem )
				{
					IPart testPart = ( (IPartItem) newType.getItem() ).createPartFromItemStack( newType );
					if( testPart instanceof PartP2PTunnel )
					{
						this.getHost().removePart( this.side, true );
						ForgeDirection dir = this.getHost().addPart( newType, this.side, player );
						IPart newBus = this.getHost().getPart( dir );

						if( newBus instanceof PartP2PTunnel )
						{
							PartP2PTunnel newTunnel = (PartP2PTunnel) newBus;
							newTunnel.output = true;

							try
							{
								P2PCache p2p = newTunnel.proxy.getP2P();
								p2p.updateFreq( newTunnel, freq );
							}
							catch( GridAccessException e )
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
					for( ItemStack stack : parts.p2PTunnelLight().maybeStack( 1 ).asSet() )
					{
						newType = stack;
					}
					break;

				case RF_POWER:
					for( ItemStack stack : parts.p2PTunnelRF().maybeStack( 1 ).asSet() )
					{
						newType = stack;
					}
					break;

				case FLUID:
					for( ItemStack stack : parts.p2PTunnelLiquids().maybeStack( 1 ).asSet() )
					{
						newType = stack;
					}
					break;

				case IC2_POWER:
					for( ItemStack stack : parts.p2PTunnelEU().maybeStack( 1 ).asSet() )
					{
						newType = stack;
					}
					break;

				case ITEM:
					for( ItemStack stack : parts.p2PTunnelItems().maybeStack( 1 ).asSet() )
					{
						newType = stack;
					}
					break;

				case ME:
					for( ItemStack stack : parts.p2PTunnelME().maybeStack( 1 ).asSet() )
					{
						newType = stack;
					}
					break;

				case REDSTONE:
					for( ItemStack stack : parts.p2PTunnelRedstone().maybeStack( 1 ).asSet() )
					{
						newType = stack;
					}
					break;

				case COMPUTER_MESSAGE:
					for( ItemStack stack : parts.p2PTunnelOpenComputers().maybeStack( 1 ).asSet() )
					{
						newType = stack;
					}
					break;

				default:
					break;
			}

			if( newType != null && !Platform.isSameItem( newType, this.is ) )
			{
				boolean oldOutput = this.output;
				long myFreq = this.freq;

				this.getHost().removePart( this.side, false );
				ForgeDirection dir = this.getHost().addPart( newType, this.side, player );
				IPart newBus = this.getHost().getPart( dir );

				if( newBus instanceof PartP2PTunnel )
				{
					PartP2PTunnel newTunnel = (PartP2PTunnel) newBus;
					newTunnel.output = oldOutput;
					newTunnel.onTunnelNetworkChange();

					try
					{
						P2PCache p2p = newTunnel.proxy.getP2P();
						p2p.updateFreq( newTunnel, myFreq );
					}
					catch( GridAccessException e )
					{
						// :P
					}
				}

				Platform.notifyBlocksOfNeighbors( this.tile.getWorldObj(), this.tile.xCoord, this.tile.yCoord, this.tile.zCoord );
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean onPartShiftActivate( EntityPlayer player, Vec3 pos )
	{
		ItemStack is = player.inventory.getCurrentItem();
		if( is != null && is.getItem() instanceof IMemoryCard )
		{
			IMemoryCard mc = (IMemoryCard) is.getItem();
			NBTTagCompound data = new NBTTagCompound();

			long newFreq = this.freq;
			boolean wasOutput = this.output;
			this.output = false;

			if( wasOutput || this.freq == 0 )
			{
				newFreq = System.currentTimeMillis();
			}

			try
			{
				this.proxy.getP2P().updateFreq( this, newFreq );
			}
			catch( GridAccessException e )
			{
				// :P
			}

			this.onTunnelConfigChange();

			ItemStack p2pItem = this.getItemStack( PartItemStack.Wrench );
			String type = p2pItem.getUnlocalizedName();

			p2pItem.writeToNBT( data );
			data.setLong( "freq", this.freq );

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

	@Override
	@SideOnly( Side.CLIENT )
	public IIcon getBreakingTexture()
	{
		return CableBusTextures.BlockP2PTunnel2.getIcon();
	}

	protected void QueueTunnelDrain( PowerUnits unit, double f )
	{
		double ae_to_tax = unit.convertTo( PowerUnits.AE, f * AEConfig.TUNNEL_POWER_LOSS );

		try
		{
			this.proxy.getEnergy().extractAEPower( ae_to_tax, Actionable.MODULATE, PowerMultiplier.ONE );
		}
		catch( GridAccessException e )
		{
			// :P
		}
	}
}
