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

package appeng.core.sync.network;


import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

import appeng.core.sync.AppEngPacket;
import appeng.core.sync.packets.PacketAssemblerAnimation;
import appeng.core.sync.packets.PacketClick;
import appeng.core.sync.packets.PacketCompassRequest;
import appeng.core.sync.packets.PacketCompassResponse;
import appeng.core.sync.packets.PacketCompressedNBT;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketCraftRequest;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.core.sync.packets.PacketLightning;
import appeng.core.sync.packets.PacketMEInventoryUpdate;
import appeng.core.sync.packets.PacketMatterCannon;
import appeng.core.sync.packets.PacketMockExplosion;
import appeng.core.sync.packets.PacketMultiPart;
import appeng.core.sync.packets.PacketNEIRecipe;
import appeng.core.sync.packets.PacketNewStorageDimension;
import appeng.core.sync.packets.PacketPaintedEntity;
import appeng.core.sync.packets.PacketPartPlacement;
import appeng.core.sync.packets.PacketPartialItem;
import appeng.core.sync.packets.PacketPatternSlot;
import appeng.core.sync.packets.PacketProgressBar;
import appeng.core.sync.packets.PacketSwapSlots;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketTransitionEffect;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.core.worlddata.WorldData;


public class NetworkHandler
{
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel( "AE2" );

	private static int disc = 0;

	public static void init()
	{
		registerMessage( PacketAssemblerAnimation.class, PacketAssemblerAnimation.class, Side.CLIENT );
		registerMessage( PacketClick.class, PacketClick.class, Side.SERVER );
		registerMessage( PacketCompassRequest.class, PacketCompassRequest.class, Side.SERVER );
		registerMessage( PacketCompassResponse.class, PacketCompassResponse.class, Side.CLIENT );
		registerMessage( PacketCompressedNBT.class, PacketCompressedNBT.class, Side.CLIENT );
		registerMessage( PacketConfigButton.class, PacketConfigButton.class, Side.SERVER );
		registerMessage( PacketCraftRequest.class, PacketCraftRequest.class, Side.SERVER );
		registerMessage( PacketInventoryAction.class, PacketInventoryAction.class, Side.SERVER );
		registerMessage( PacketInventoryAction.class, PacketInventoryAction.class, Side.CLIENT );
		registerMessage( PacketLightning.class, PacketLightning.class, Side.CLIENT );
		registerMessage( PacketMatterCannon.class, PacketMatterCannon.class, Side.CLIENT );
		registerMessage( PacketMEInventoryUpdate.class, PacketMEInventoryUpdate.class, Side.CLIENT );
		registerMessage( PacketMockExplosion.class, PacketMockExplosion.class, Side.CLIENT );
		registerMessage( PacketMultiPart.class, PacketMultiPart.class, Side.SERVER );
		registerMessage( PacketNEIRecipe.class, PacketNEIRecipe.class, Side.SERVER );
		registerMessage( PacketNewStorageDimension.class, PacketNewStorageDimension.class, Side.CLIENT );
		registerMessage( PacketPaintedEntity.class, PacketPaintedEntity.class, Side.CLIENT );
		registerMessage( PacketPartialItem.class, PacketPartialItem.class, Side.SERVER );
		registerMessage( PacketPartPlacement.class, PacketPartPlacement.class, Side.SERVER );
		registerMessage( PacketPatternSlot.class, PacketPatternSlot.class, Side.SERVER );
		registerMessage( PacketProgressBar.class, PacketProgressBar.class, Side.SERVER );
		registerMessage( PacketProgressBar.class, PacketProgressBar.class, Side.CLIENT );
		registerMessage( PacketSwapSlots.class, PacketSwapSlots.class, Side.SERVER );
		registerMessage( PacketSwitchGuis.class, PacketSwitchGuis.class, Side.SERVER );
		registerMessage( PacketSwitchGuis.class, PacketSwitchGuis.class, Side.CLIENT );
		registerMessage( PacketTransitionEffect.class, PacketTransitionEffect.class, Side.CLIENT );
		registerMessage( PacketValueConfig.class, PacketValueConfig.class, Side.SERVER );
		registerMessage( PacketValueConfig.class, PacketValueConfig.class, Side.CLIENT );
	}

	@SubscribeEvent
	public void newConnection( final ServerConnectionFromClientEvent ev )
	{
		WorldData.instance().dimensionData().sendToPlayer( ev.manager );
	}

	@SubscribeEvent
	public void newConnection( final PlayerLoggedInEvent loginEvent )
	{
		if( loginEvent.player instanceof EntityPlayerMP )
		{
			WorldData.instance().dimensionData().sendToPlayer( null );
		}
	}

	private static final <REQ extends AppEngPacket, REPLY extends AppEngPacket> void registerMessage( Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side side )
	{
		NetworkHandler.INSTANCE.registerMessage( messageHandler, requestMessageType, disc, side );
		disc++;
	}
}
