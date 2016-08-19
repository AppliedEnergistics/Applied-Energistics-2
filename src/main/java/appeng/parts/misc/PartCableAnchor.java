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

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.client.BakingPipeline;
import appeng.api.networking.IGridNode;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AEPartLocation;


public class PartCableAnchor implements IPart
{

	private ItemStack is = null;
	private IPartHost host = null;
	private AEPartLocation mySide = AEPartLocation.UP;

	public PartCableAnchor( final ItemStack is )
	{
		this.is = is;
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{
		if( this.host != null && this.host.getFacadeContainer().getFacade( this.mySide ) != null )
		{
			bch.addBox( 7, 7, 10, 9, 9, 14 );
		}
		else
		{
			bch.addBox( 7, 7, 10, 9, 9, 16 );
		}
	}

	@Override
	public ItemStack getItemStack( final PartItemStack wrenched )
	{
		return this.is;
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
	public void writeToNBT( final NBTTagCompound data )
	{

	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{

	}

	@Override
	public int getLightLevel()
	{
		return 0;
	}

	@Override
	public boolean isLadder( final EntityLivingBase entity )
	{
		return this.mySide.yOffset == 0 && ( entity.isCollidedHorizontally || !entity.onGround );
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
	public void writeToStream( final ByteBuf data ) throws IOException
	{

	}

	@Override
	public boolean readFromStream( final ByteBuf data ) throws IOException
	{
		return false;
	}

	@Override
	public IGridNode getGridNode()
	{
		return null;
	}

	@Override
	public void onEntityCollision( final Entity entity )
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
	public void setPartHostInfo( final AEPartLocation side, final IPartHost host, final TileEntity tile )
	{
		this.host = host;
		this.mySide = side;
	}

	@Override
	public boolean onActivate( final EntityPlayer player, final EnumHand hand, final Vec3d pos )
	{
		return false;
	}

	@Override
	public boolean onShiftActivate( final EntityPlayer player, final EnumHand hand, final Vec3d pos )
	{
		return false;
	}

	@Override
	public void getDrops( final List<ItemStack> drops, final boolean wrenched )
	{

	}

	@Override
	public int getCableConnectionLength()
	{
		return 0;
	}

	@Override
	public void randomDisplayTick( final World world, final BlockPos pos, final Random r )
	{

	}

	@Override
	public void onPlacement( final EntityPlayer player, final EnumHand hand, final ItemStack held, final AEPartLocation side )
	{

	}

	@Override
	public boolean canBePlacedOn( final BusSupport what )
	{
		return what == BusSupport.CABLE || what == BusSupport.DENSE_CABLE;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public List<BakedQuad> getOrBakeQuads( BakingPipeline<BakedQuad, BakedQuad> rotatingPipeline, IBlockState state, EnumFacing side, long rand )
	{
		return null;
	}

}
