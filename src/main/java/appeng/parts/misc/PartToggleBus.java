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

package appeng.parts.misc;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnection;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.client.render.ModelGenerator;
import appeng.client.texture.CableBusTextures;
import appeng.helpers.Reflected;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.PartBasicState;
import appeng.util.Platform;


public class PartToggleBus extends PartBasicState
{
	private static final int REDSTONE_FLAG = 4;
	final AENetworkProxy outerProxy = new AENetworkProxy( this, "outer", null, true );
	IGridConnection connection;
	boolean hasRedstone = false;

	@Reflected
	public PartToggleBus( ItemStack is )
	{
		super( is );

		this.proxy.setIdlePowerUsage( 0.0 );
		this.outerProxy.setIdlePowerUsage( 0.0 );
		this.proxy.setFlags();
		this.outerProxy.setFlags();
	}

	@Override
	public void setColors( ModelGenerator renderer, boolean hasChan, boolean hasPower )
	{
		this.hasRedstone = ( this.clientFlags & REDSTONE_FLAG ) == REDSTONE_FLAG;
		super.setColors( renderer, hasChan && this.hasRedstone, hasPower && this.hasRedstone );
	}

	@Override
	protected int populateFlags( int cf )
	{
		return cf | ( this.getIntention() ? REDSTONE_FLAG : 0 );
	}

	protected boolean getIntention()
	{
		return this.getHost().hasRedstone( this.side );
	}

	@Override
	public TextureAtlasSprite getBreakingTexture( ModelGenerator renderer )
	{
		return renderer.getIcon( is  ).getAtlas();
	}

	@Override
	public AECableType getCableConnectionType( AEPartLocation dir )
	{
		return AECableType.GLASS;
	}

	@Override
	public void securityBreak()
	{
		if( this.is.stackSize > 0 )
		{
			List<ItemStack> items = new ArrayList<ItemStack>();
			items.add( this.is.copy() );
			this.host.removePart( this.side, false );
			Platform.spawnDrops( this.tile.getWorld(), this.tile.getPos(), items );
			this.is.stackSize = 0;
		}
	}

	@Override
	public void getBoxes( IPartCollisionHelper bch )
	{
		bch.addBox( 6, 6, 11, 10, 10, 16 );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( IPartRenderHelper rh, ModelGenerator renderer )
	{
		GL11.glTranslated( -0.2, -0.3, 0.0 );

		rh.setTexture( renderer.getIcon( is ) );
		rh.setBounds( 6, 6, 14 - 4, 10, 10, 16 - 4 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 6, 6, 11 - 4, 10, 10, 13 - 4 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 6, 6, 13 - 4, 10, 10, 14 - 4 );
		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon() );
		rh.renderInventoryBox( renderer );

		rh.setTexture( CableBusTextures.PartMonitorSidesStatusLights.getIcon() );
		rh.setInvColor( 0x000000 );
		rh.renderInventoryBox( renderer );
		rh.setInvColor( 0xffffff );

		rh.setTexture( null );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( BlockPos pos, IPartRenderHelper rh, ModelGenerator renderer )
	{
		rh.setTexture( renderer.getIcon( is ) );

		rh.setBounds( 6, 6, 14, 10, 10, 16 );
		rh.renderBlock( pos, renderer );

		rh.setBounds( 6, 6, 11, 10, 10, 13 );
		rh.renderBlock( pos, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), renderer.getIcon( is ), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon() );

		rh.setBounds( 6, 6, 13, 10, 10, 14 );
		rh.renderBlock( pos, renderer );

		this.renderLights( pos, rh, renderer );
	}

	@Override
	public void onNeighborChanged()
	{
		boolean oldHasRedstone = this.hasRedstone;
		this.hasRedstone = this.getHost().hasRedstone( this.side );

		if( this.hasRedstone != oldHasRedstone )
		{
			this.updateInternalState();
			this.getHost().markForUpdate();
		}
	}

	@Override
	public void readFromNBT( NBTTagCompound extra )
	{
		super.readFromNBT( extra );
		this.outerProxy.readFromNBT( extra );
	}

	@Override
	public void writeToNBT( NBTTagCompound extra )
	{
		super.writeToNBT( extra );
		this.outerProxy.writeToNBT( extra );
	}

	@Override
	public void removeFromWorld()
	{
		super.removeFromWorld();
		this.outerProxy.invalidate();
	}

	@Override
	public void addToWorld()
	{
		super.addToWorld();
		this.outerProxy.onReady();
		this.hasRedstone = this.getHost().hasRedstone( this.side );
		this.updateInternalState();
	}

	@Override
	public void setPartHostInfo( AEPartLocation side, IPartHost host, TileEntity tile )
	{
		super.setPartHostInfo( side, host, tile );
		this.outerProxy.setValidSides( EnumSet.of( side.getFacing() ) );
	}

	@Override
	public IGridNode getExternalFacingNode()
	{
		return this.outerProxy.getNode();
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 5;
	}

	@Override
	public void onPlacement( EntityPlayer player, ItemStack held, AEPartLocation side )
	{
		super.onPlacement( player, held, side );
		this.outerProxy.setOwner( player );
	}

	private void updateInternalState()
	{
		boolean intention = this.getIntention();
		if( intention == ( this.connection == null ) )
		{
			if( this.proxy.getNode() != null && this.outerProxy.getNode() != null )
			{
				if( intention )
				{
					try
					{
						this.connection = AEApi.instance().createGridConnection( this.proxy.getNode(), this.outerProxy.getNode() );
					}
					catch( FailedConnection e )
					{
						// :(
					}
				}
				else
				{
					this.connection.destroy();
					this.connection = null;
				}
			}
		}
	}
}
