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

package appeng.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.misc.BlockLightDetector;
import appeng.block.misc.BlockSkyCompass;
import appeng.block.networking.BlockWireless;
import appeng.client.render.ItemRenderer;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;

public class AEBaseItemBlock extends ItemBlock
{

	final AEBaseBlock blockType;

	public AEBaseItemBlock(Block id)
	{
		super( id );
		this.blockType = (AEBaseBlock) id;
		this.hasSubtypes = this.blockType.hasSubtypes;

		if ( Platform.isClient() )
			MinecraftForgeClient.registerItemRenderer( this, ItemRenderer.INSTANCE );
	}

	@Override
	public int getMetadata(int dmg)
	{
		if ( this.hasSubtypes )
			return dmg;
		return 0;
	}

	@Override
	public String getUnlocalizedName(ItemStack is)
	{
		return this.blockType.getUnlocalizedName( is );
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("unchecked")
	public final void addInformation(ItemStack itemStack, EntityPlayer player, List toolTip, boolean advancedTooltips)
	{
		this.addCheckedInformation( itemStack, player, toolTip, advancedTooltips );
	}

	@SideOnly(Side.CLIENT)
	public void addCheckedInformation(ItemStack itemStack, EntityPlayer player, List<String> toolTip, boolean advancedToolTips)
	{
		this.blockType.addInformation( itemStack, player, toolTip, advancedToolTips );
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World w, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		ForgeDirection up = ForgeDirection.UNKNOWN;
		ForgeDirection forward = ForgeDirection.UNKNOWN;

		IOrientable ori = null;

		if ( this.blockType.hasBlockTileEntity() )
		{
			if ( this.blockType instanceof BlockLightDetector )
			{
				up = ForgeDirection.getOrientation( side );
				if ( up == ForgeDirection.UP || up == ForgeDirection.DOWN )
					forward = ForgeDirection.SOUTH;
				else
					forward = ForgeDirection.UP;
			}
			else if ( this.blockType instanceof BlockWireless || this.blockType instanceof BlockSkyCompass )
			{
				forward = ForgeDirection.getOrientation( side );
				if ( forward == ForgeDirection.UP || forward == ForgeDirection.DOWN )
					up = ForgeDirection.SOUTH;
				else
					up = ForgeDirection.UP;
			}
			else
			{
				up = ForgeDirection.UP;

				byte rotation = (byte) (MathHelper.floor_double( (player.rotationYaw * 4F) / 360F + 2.5D ) & 3);

				switch (rotation)
				{
				default:
				case 0:
					forward = ForgeDirection.SOUTH;
					break;
				case 1:
					forward = ForgeDirection.WEST;
					break;
				case 2:
					forward = ForgeDirection.NORTH;
					break;
				case 3:
					forward = ForgeDirection.EAST;
					break;
				}

				if ( player.rotationPitch > 65 )
				{
					up = forward.getOpposite();
					forward = ForgeDirection.UP;
				}
				else if ( player.rotationPitch < -65 )
				{
					up = forward.getOpposite();
					forward = ForgeDirection.DOWN;
				}
			}
		}

		if ( this.blockType instanceof IOrientableBlock )
		{
			ori = ((IOrientableBlock) this.blockType).getOrientable( w, x, y, z );
			up = ForgeDirection.getOrientation( side );
			forward = ForgeDirection.SOUTH;
			if ( up.offsetY == 0 )
				forward = ForgeDirection.UP;

			ori.setOrientation( forward, up );
		}

		if ( !this.blockType.isValidOrientation( w, x, y, z, forward, up ) )
			return false;

		if ( super.placeBlockAt( stack, player, w, x, y, z, side, hitX, hitY, hitZ, metadata ) )
		{
			if ( this.blockType.hasBlockTileEntity() && !(this.blockType instanceof BlockLightDetector) )
			{
				AEBaseTile tile = this.blockType.getTileEntity( w, x, y, z );
				ori = tile;

				if ( tile == null )
					return true;

				if ( ori.canBeRotated() && !this.blockType.hasCustomRotation() )
				{
					if ( ori.getForward() == null || ori.getUp() == null || // null
							tile.getForward() == ForgeDirection.UNKNOWN || ori.getUp() == ForgeDirection.UNKNOWN )
						ori.setOrientation( forward, up );
				}

				if ( tile instanceof IGridProxyable )
				{
					((IGridProxyable) tile).getProxy().setOwner( player );
				}

				tile.onPlacement( stack, player, side );
			}
			else if ( this.blockType instanceof IOrientableBlock )
			{
				ori.setOrientation( forward, up );
			}

			return true;
		}
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack itemstack1, ItemStack itemstack2)
	{
		return false;
	}

}
