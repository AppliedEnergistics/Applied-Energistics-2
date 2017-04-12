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


import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnection;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AECableType;
import appeng.client.texture.CableBusTextures;
import appeng.helpers.Reflected;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.PartBasicState;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


public class PartToggleBus extends PartBasicState
{
	private static final int REDSTONE_FLAG = 4;
	private final AENetworkProxy outerProxy = new AENetworkProxy( this, "outer", null, true );
	private IGridConnection connection;
	private boolean hasRedstone = false;

	@Reflected
	public PartToggleBus( final ItemStack is )
	{
		super( is );

		this.getProxy().setIdlePowerUsage( 0.0 );
		this.getOuterProxy().setIdlePowerUsage( 0.0 );
		this.getProxy().setFlags();
		this.getOuterProxy().setFlags();
	}

	@Override
	public void setColors( final boolean hasChan, final boolean hasPower )
	{
		this.hasRedstone = ( this.getClientFlags() & REDSTONE_FLAG ) == REDSTONE_FLAG;
		super.setColors( hasChan && this.hasRedstone, hasPower && this.hasRedstone );
	}

	@Override
	protected int populateFlags( final int cf )
	{
		return cf | ( this.getIntention() ? REDSTONE_FLAG : 0 );
	}

	protected boolean getIntention()
	{
		return this.getHost().hasRedstone( this.getSide() );
	}

	@Override
	public IIcon getBreakingTexture()
	{
		return this.getItemStack().getIconIndex();
	}

	@Override
	public AECableType getCableConnectionType( final ForgeDirection dir )
	{
		return AECableType.GLASS;
	}

	@Override
	public void securityBreak()
	{
		if( this.getItemStack().stackSize > 0 )
		{
			final List<ItemStack> items = new ArrayList<ItemStack>();
			items.add( this.getItemStack().copy() );
			this.getHost().removePart( this.getSide(), false );
			Platform.spawnDrops( this.getTile().getWorldObj(), this.getTile().xCoord, this.getTile().yCoord, this.getTile().zCoord, items );
			this.getItemStack().stackSize = 0;
		}
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{
		bch.addBox( 6, 6, 11, 10, 10, 16 );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( final IPartRenderHelper rh, final RenderBlocks renderer )
	{
		GL11.glTranslated( -0.2, -0.3, 0.0 );

		rh.setTexture( this.getItemStack().getIconIndex() );
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
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper rh, final RenderBlocks renderer )
	{
		this.setRenderCache( rh.useSimplifiedRendering( x, y, z, this, this.getRenderCache() ) );
		rh.setTexture( this.getItemStack().getIconIndex() );

		rh.setBounds( 6, 6, 14, 10, 10, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 6, 6, 11, 10, 10, 13 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), this.getItemStack().getIconIndex(), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon() );

		rh.setBounds( 6, 6, 13, 10, 10, 14 );
		rh.renderBlock( x, y, z, renderer );

		this.renderLights( x, y, z, rh, renderer );
	}

	@Override
	public void onNeighborChanged()
	{
		final boolean oldHasRedstone = this.hasRedstone;
		this.hasRedstone = this.getHost().hasRedstone( this.getSide() );

		if( this.hasRedstone != oldHasRedstone )
		{
			this.updateInternalState();
			this.getHost().markForUpdate();
		}
	}

	@Override
	public void readFromNBT( final NBTTagCompound extra )
	{
		super.readFromNBT( extra );
		this.getOuterProxy().readFromNBT( extra );
	}

	@Override
	public void writeToNBT( final NBTTagCompound extra )
	{
		super.writeToNBT( extra );
		this.getOuterProxy().writeToNBT( extra );
	}

	@Override
	public void removeFromWorld()
	{
		super.removeFromWorld();
		this.getOuterProxy().invalidate();
	}

	@Override
	public void addToWorld()
	{
		super.addToWorld();
		this.getOuterProxy().onReady();
		this.hasRedstone = this.getHost().hasRedstone( this.getSide() );
		this.updateInternalState();
	}

	@Override
	public void setPartHostInfo( final ForgeDirection side, final IPartHost host, final TileEntity tile )
	{
		super.setPartHostInfo( side, host, tile );
		this.getOuterProxy().setValidSides( EnumSet.of( side ) );
	}

	@Override
	public IGridNode getExternalFacingNode()
	{
		return this.getOuterProxy().getNode();
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 5;
	}

	@Override
	public void onPlacement( final EntityPlayer player, final ItemStack held, final ForgeDirection side )
	{
		super.onPlacement( player, held, side );
		this.getOuterProxy().setOwner( player );
	}

	private void updateInternalState()
	{
		final boolean intention = this.getIntention();
		if( intention == ( this.connection == null ) )
		{
			if( this.getProxy().getNode() != null && this.getOuterProxy().getNode() != null )
			{
				if( intention )
				{
					try
					{
						this.connection = AEApi.instance().createGridConnection( this.getProxy().getNode(), this.getOuterProxy().getNode() );
					}
					catch( final FailedConnection e )
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

	AENetworkProxy getOuterProxy()
	{
		return this.outerProxy;
	}
}
