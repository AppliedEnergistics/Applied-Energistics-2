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

package appeng.block.misc;

import java.util.EnumSet;
import java.util.UUID;

import appeng.core.WorldSettings;
import com.google.common.base.Optional;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.BlockEvent;

import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RendererSecurity;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.misc.TileSecurity;
import appeng.util.Platform;

public class BlockSecurity extends AEBaseBlock
{

	public BlockSecurity()
	{
		super( BlockSecurity.class, Material.iron );
		this.setFeature( EnumSet.of( AEFeature.Security ) );
		this.setTileEntity( TileSecurity.class );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RendererSecurity.class;
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		if ( p.isSneaking() )
			return false;

		TileSecurity tg = this.getTileEntity( w, x, y, z );
		if ( tg != null )
		{
			if ( Platform.isClient() )
				return true;

			Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_SECURITY );
			return true;

		}
		return false;
	}

	public static class Listener
	{
		private static final Listener instance = new Listener();
		public static Listener instance() { return instance; }

		@SubscribeEvent
		public void listenBreakBlock(BlockEvent.BreakEvent event)
		{
			// If logging is disabled, the listener isn't registered

			// if ( ! AEConfig.instance.isFeatureEnabled( AEFeature.LogSecurityTerminalBreak ) )
			//	return;

			// Only listen to events for breaking BlockSecurity
			if ( ! ( event.block instanceof BlockSecurity ) )
				return;

			BlockSecurity self = (BlockSecurity) event.block;
			EntityPlayer breaker = event.getPlayer();

			TileSecurity te = self.getTileEntity( event.world, event.x, event.y, event.z );
			String owner = getOwnerNameForLog( te.getOwner() );

			// Don't log breaking your own terminal
			if ( owner.equals( breaker.getCommandSenderName() ) )
				return;

			AELog.info( "[AE2] %s broke a security terminal owned by %s at (DIM%d, %d, %d, %d)",
					breaker.getDisplayName(),
					owner,
					event.world.getWorldInfo().getVanillaDimension(),
					event.x, event.y, event.z );
		}


		/**
		 * Get a log-readable string representing the owner of a security terminal.
		 *
		 * @return string representing the owner
		 */
		private static String getOwnerNameForLog(int playerID)
		{
			String ownerName;

			if ( playerID == -1 )
			{
				return "(no owner)";
			}
			else
			{
				Optional<UUID> maybeUUID = WorldSettings.getInstance().getUUIDFromNumericalId( playerID );

				if ( maybeUUID.isPresent() )
				{
					UUID uuid = maybeUUID.get();
					ownerName = Platform.getPlayerNameFromUUID( uuid );
					if ( ownerName != null )
						return ownerName;

					// fall back to UUID
					return String.format( "UUID:%s", uuid );
				}
				else
				{
					// This will happen for situations e.g. when a security terminal is imported in a schematic
					return String.format( "(unknown owner, internal ID %d)", playerID );
				}
			}
		}
	}

}
