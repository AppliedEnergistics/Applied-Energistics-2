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


import java.io.IOException;
import java.util.List;
import java.util.Random;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.networking.IGridNode;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.ISimplifiedBundle;
import appeng.api.parts.PartItemStack;


public class PartCableAnchor implements IPart
{

	protected ISimplifiedBundle renderCache = null;
	ItemStack is = null;
	IPartHost host = null;
	ForgeDirection mySide = ForgeDirection.UP;

	public PartCableAnchor( ItemStack is )
	{
		this.is = is;
	}

	@Override
	public void getBoxes( IPartCollisionHelper bch )
	{
		if( this.host != null && this.host.getFacadeContainer().getFacade( this.mySide ) != null )
			bch.addBox( 7, 7, 10, 9, 9, 14 );
		else
			bch.addBox( 7, 7, 10, 9, 9, 16 );
	}

	@Override
	public ItemStack getItemStack( PartItemStack wrenched )
	{
		return this.is;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( IPartRenderHelper instance, RenderBlocks renderer )
	{
		instance.setTexture( this.is.getIconIndex() );
		instance.setBounds( 7, 7, 4, 9, 9, 14 );
		instance.renderInventoryBox( renderer );
		instance.setTexture( null );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer )
	{
		this.renderCache = rh.useSimplifiedRendering( x, y, z, this, this.renderCache );
		IIcon myIcon = this.is.getIconIndex();
		rh.setTexture( myIcon );
		if( this.host != null && this.host.getFacadeContainer().getFacade( this.mySide ) != null )
			rh.setBounds( 7, 7, 10, 9, 9, 14 );
		else
			rh.setBounds( 7, 7, 10, 9, 9, 16 );
		rh.renderBlock( x, y, z, renderer );
		rh.setTexture( null );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderDynamic( double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer )
	{

	}

	@Override
	public IIcon getBreakingTexture()
	{
		return null;
	}

	@Override
	public boolean requireDynamicRender()
	{
		return false;
	}

	@Override
	public boolean isSolid()
	{
		return false;
	}

	@Override
	public boolean canConnectRedstone()
	{
		return false;
	}

	@Override
	public void writeToNBT( NBTTagCompound data )
	{

	}

	@Override
	public void readFromNBT( NBTTagCompound data )
	{

	}

	@Override
	public int getLightLevel()
	{
		return 0;
	}

	@Override
	public boolean isLadder( EntityLivingBase entity )
	{
		return this.mySide.offsetY == 0 && ( entity.isCollidedHorizontally || !entity.onGround );
	}

	@Override
	public void onNeighborChanged()
	{

	}

	@Override
	public int isProvidingStrongPower()
	{
		return 0;
	}

	@Override
	public int isProvidingWeakPower()
	{
		return 0;
	}

	@Override
	public void writeToStream( ByteBuf data ) throws IOException
	{

	}

	@Override
	public boolean readFromStream( ByteBuf data ) throws IOException
	{
		return false;
	}

	@Override
	public IGridNode getGridNode()
	{
		return null;
	}

	@Override
	public void onEntityCollision( Entity entity )
	{

	}

	@Override
	public void removeFromWorld()
	{

	}

	@Override
	public void addToWorld()
	{

	}

	@Override
	public IGridNode getExternalFacingNode()
	{
		return null;
	}

	@Override
	public void setPartHostInfo( ForgeDirection side, IPartHost host, TileEntity tile )
	{
		this.host = host;
		this.mySide = side;
	}

	@Override
	public boolean onActivate( EntityPlayer player, Vec3 pos )
	{
		return false;
	}

	@Override
	public boolean onShiftActivate( EntityPlayer player, Vec3 pos )
	{
		return false;
	}

	@Override
	public void getDrops( List<ItemStack> drops, boolean wrenched )
	{

	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 0;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void randomDisplayTick( World world, int x, int y, int z, Random r )
	{

	}

	@Override
	public void onPlacement( EntityPlayer player, ItemStack held, ForgeDirection side )
	{

	}

	@Override
	public boolean canBePlacedOn( BusSupport what )
	{
		return what == BusSupport.CABLE || what == BusSupport.DENSE_CABLE;
	}
}
