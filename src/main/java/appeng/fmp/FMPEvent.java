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

package appeng.fmp;


import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import codechicken.lib.packet.PacketCustom;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

import appeng.block.AEBaseItemBlock;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMultiPart;
import appeng.integration.modules.helpers.FMPPacketEvent;


/**
 * Basically a total rip of of the FMP version for vanilla, seemed to work well enough...
 */
public final class FMPEvent
{

	private final ThreadLocal<Object> placing = new ThreadLocal<Object>();

	@SubscribeEvent
	public void ServerFMPEvent( FMPPacketEvent event )
	{
		FMPEvent.place( event.sender, event.sender.worldObj );
	}

	public static boolean place( EntityPlayer player, World world )
	{
		MovingObjectPosition hit = RayTracer.reTrace( world, player );
		if( hit == null )
		{
			return false;
		}

		BlockCoord pos = new BlockCoord( hit.blockX, hit.blockY, hit.blockZ ).offset( hit.sideHit );
		ItemStack held = player.getHeldItem();
		TMultiPart part = null;

		Block blk = null;

		if( held == null )
		{
			return false;
		}

		if( held.getItem() instanceof AEBaseItemBlock )
		{
			AEBaseItemBlock ib = (AEBaseItemBlock) held.getItem();
			blk = Block.getBlockFromItem( ib );
			part = PartRegistry.getPartByBlock( blk, hit.sideHit );
		}

		if( part == null )
		{
			return false;
		}

		if( world.isRemote && !player.isSneaking() )// attempt to use block activated like normal and tell the server
		// the right stuff
		{
			Vector3 f = new Vector3( hit.hitVec ).add( -hit.blockX, -hit.blockY, -hit.blockZ );
			Block block = world.getBlock( hit.blockX, hit.blockY, hit.blockZ );
			if( block != null && !ignoreActivate( block ) && block.onBlockActivated( world, hit.blockX, hit.blockY, hit.blockZ, player, hit.sideHit, (float) f.x, (float) f.y, (float) f.z ) )
			{
				player.swingItem();
				PacketCustom.sendToServer( new C08PacketPlayerBlockPlacement( hit.blockX, hit.blockY, hit.blockZ, hit.sideHit, player.inventory.getCurrentItem(), (float) f.x, (float) f.y, (float) f.z ) );
				return true;
			}
		}

		TileMultipart tile = TileMultipart.getOrConvertTile( world, pos );
		if( tile == null || !tile.canAddPart( part ) )
		{
			return false;
		}

		if( !world.isRemote )
		{
			TileMultipart.addPart( world, pos, part );
			world.playSoundEffect( pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, blk.stepSound.func_150496_b(), ( blk.stepSound.getVolume() + 1.0F ) / 2.0F, blk.stepSound.getPitch() * 0.8F );
			if( !player.capabilities.isCreativeMode )
			{
				held.stackSize--;
				if( held.stackSize == 0 )
				{
					player.inventory.mainInventory[player.inventory.currentItem] = null;
					MinecraftForge.EVENT_BUS.post( new PlayerDestroyItemEvent( player, held ) );
				}
			}
		}
		else
		{
			player.swingItem();
			NetworkHandler.instance.sendToServer( new PacketMultiPart() );
		}
		return true;
	}

	/**
	 * Because vanilla is weird.
	 */
	private static boolean ignoreActivate( Block block )
	{
		if( block instanceof BlockFence )
		{
			return true;
		}
		return false;
	}

	@SubscribeEvent
	public void playerInteract( PlayerInteractEvent event )
	{
		if( event.action == Action.RIGHT_CLICK_BLOCK && event.entityPlayer.worldObj.isRemote )
		{
			if( this.placing.get() != null )
			{
				return;
			}
			this.placing.set( event );
			if( place( event.entityPlayer, event.entityPlayer.worldObj ) )
			{
				event.setCanceled( true );
			}
			this.placing.set( null );
		}
	}
}
