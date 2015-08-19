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


import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

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


public class NetworkHandler
{
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel( "AE2" );

	public static void init()
	{
		int disc = 0;
		NetworkHandler.INSTANCE.registerMessage( PacketAssemblerAnimation.class, PacketAssemblerAnimation.class, disc++, Side.CLIENT );
		NetworkHandler.INSTANCE.registerMessage( PacketClick.class, PacketClick.class, disc++, Side.SERVER );
		NetworkHandler.INSTANCE.registerMessage( PacketCompassRequest.class, PacketCompassRequest.class, disc++, Side.SERVER );
		NetworkHandler.INSTANCE.registerMessage( PacketCompassResponse.class, PacketCompassResponse.class, disc++, Side.CLIENT );
		NetworkHandler.INSTANCE.registerMessage( PacketCompressedNBT.class, PacketCompressedNBT.class, disc++, Side.CLIENT );
		NetworkHandler.INSTANCE.registerMessage( PacketConfigButton.class, PacketConfigButton.class, disc++, Side.SERVER );
		NetworkHandler.INSTANCE.registerMessage( PacketCraftRequest.class, PacketCraftRequest.class, disc++, Side.SERVER );
		NetworkHandler.INSTANCE.registerMessage( PacketInventoryAction.class, PacketInventoryAction.class, disc++, Side.SERVER );
		NetworkHandler.INSTANCE.registerMessage( PacketInventoryAction.class, PacketInventoryAction.class, disc++, Side.CLIENT );
		NetworkHandler.INSTANCE.registerMessage( PacketLightning.class, PacketLightning.class, disc++, Side.CLIENT );
		NetworkHandler.INSTANCE.registerMessage( PacketMatterCannon.class, PacketMatterCannon.class, disc++, Side.CLIENT );
		NetworkHandler.INSTANCE.registerMessage( PacketMEInventoryUpdate.class, PacketMEInventoryUpdate.class, disc++, Side.CLIENT );
		NetworkHandler.INSTANCE.registerMessage( PacketMockExplosion.class, PacketMockExplosion.class, disc++, Side.CLIENT );
		NetworkHandler.INSTANCE.registerMessage( PacketMultiPart.class, PacketMultiPart.class, disc++, Side.SERVER );
		NetworkHandler.INSTANCE.registerMessage( PacketNEIRecipe.class, PacketNEIRecipe.class, disc++, Side.SERVER );
		NetworkHandler.INSTANCE.registerMessage( PacketNewStorageDimension.class, PacketNewStorageDimension.class, disc++, Side.CLIENT );
		NetworkHandler.INSTANCE.registerMessage( PacketPaintedEntity.class, PacketPaintedEntity.class, disc++, Side.CLIENT );
		NetworkHandler.INSTANCE.registerMessage( PacketPartialItem.class, PacketPartialItem.class, disc++, Side.SERVER );
		NetworkHandler.INSTANCE.registerMessage( PacketPartPlacement.class, PacketPartPlacement.class, disc++, Side.SERVER );
		NetworkHandler.INSTANCE.registerMessage( PacketPatternSlot.class, PacketPatternSlot.class, disc++, Side.SERVER );
		NetworkHandler.INSTANCE.registerMessage( PacketProgressBar.class, PacketProgressBar.class, disc++, Side.SERVER );
		NetworkHandler.INSTANCE.registerMessage( PacketProgressBar.class, PacketProgressBar.class, disc++, Side.CLIENT );
		NetworkHandler.INSTANCE.registerMessage( PacketSwapSlots.class, PacketSwapSlots.class, disc++, Side.SERVER );
		NetworkHandler.INSTANCE.registerMessage( PacketSwitchGuis.class, PacketSwitchGuis.class, disc++, Side.SERVER );
		NetworkHandler.INSTANCE.registerMessage( PacketSwitchGuis.class, PacketSwitchGuis.class, disc++, Side.CLIENT );
		NetworkHandler.INSTANCE.registerMessage( PacketTransitionEffect.class, PacketTransitionEffect.class, disc++, Side.CLIENT );
		NetworkHandler.INSTANCE.registerMessage( PacketValueConfig.class, PacketValueConfig.class, disc++, Side.SERVER );
		NetworkHandler.INSTANCE.registerMessage( PacketValueConfig.class, PacketValueConfig.class, disc++, Side.CLIENT );
	}
}
