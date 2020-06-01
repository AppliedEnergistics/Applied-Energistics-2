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

package appeng.core.sync;


import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.jodah.typetools.TypeResolver;
import net.minecraft.network.PacketBuffer;

import appeng.core.sync.packets.PacketAssemblerAnimation;
import appeng.core.sync.packets.PacketClick;
import appeng.core.sync.packets.PacketCompassRequest;
import appeng.core.sync.packets.PacketCompassResponse;
import appeng.core.sync.packets.PacketCompressedNBT;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketCraftRequest;
import appeng.core.sync.packets.PacketFluidSlot;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.core.sync.packets.PacketJEIRecipe;
import appeng.core.sync.packets.PacketLightning;
import appeng.core.sync.packets.PacketMEFluidInventoryUpdate;
import appeng.core.sync.packets.PacketMEInventoryUpdate;
import appeng.core.sync.packets.PacketMatterCannon;
import appeng.core.sync.packets.PacketMockExplosion;
import appeng.core.sync.packets.PacketPaintedEntity;
import appeng.core.sync.packets.PacketPartPlacement;
import appeng.core.sync.packets.PacketPatternSlot;
import appeng.core.sync.packets.PacketProgressBar;
import appeng.core.sync.packets.PacketSwapSlots;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketTargetFluidStack;
import appeng.core.sync.packets.PacketTargetItemStack;
import appeng.core.sync.packets.PacketTransitionEffect;
import appeng.core.sync.packets.PacketValueConfig;


public class AppEngPacketHandlerBase
{
	private static final Map<Class<? extends AppEngPacket>, PacketTypes> REVERSE_LOOKUP = new HashMap<>();


	public enum PacketTypes
	{
		PACKET_COMPASS_REQUEST( PacketCompassRequest::new ),

		PACKET_COMPASS_RESPONSE( PacketCompassResponse::new ),

		PACKET_INVENTORY_ACTION( PacketInventoryAction::new ),

		PACKET_ME_INVENTORY_UPDATE( PacketMEInventoryUpdate::new ),

		PACKET_ME_FLUID_INVENTORY_UPDATE( PacketMEFluidInventoryUpdate::new ),

		PACKET_CONFIG_BUTTON( PacketConfigButton::new ),

		PACKET_PART_PLACEMENT( PacketPartPlacement::new ),

		PACKET_LIGHTNING( PacketLightning::new ),

		PACKET_MATTER_CANNON( PacketMatterCannon::new ),

		PACKET_MOCK_EXPLOSION( PacketMockExplosion::new ),

		PACKET_VALUE_CONFIG( PacketValueConfig::new ),

		PACKET_TRANSITION_EFFECT( PacketTransitionEffect::new ),

		PACKET_PROGRESS_VALUE( PacketProgressBar::new ),

		PACKET_CLICK( PacketClick::new ),

		PACKET_SWITCH_GUIS( PacketSwitchGuis::new ),

		PACKET_SWAP_SLOTS( PacketSwapSlots::new ),

		PACKET_PATTERN_SLOT( PacketPatternSlot::new ),

		PACKET_RECIPE_JEI( PacketJEIRecipe::new ),

		PACKET_TARGET_ITEM( PacketTargetItemStack::new ),

		PACKET_TARGET_FLUID( PacketTargetFluidStack::new ),

		PACKET_CRAFTING_REQUEST( PacketCraftRequest::new ),

		PACKET_ASSEMBLER_ANIMATION( PacketAssemblerAnimation::new ),

		PACKET_COMPRESSED_NBT( PacketCompressedNBT::new ),

		PACKET_PAINTED_ENTITY( PacketPaintedEntity::new ),

		PACKET_FLUID_TANK( PacketFluidSlot::new );

		private final Class<? extends AppEngPacket> packetClass;
		private final Function<PacketBuffer, AppEngPacket> factory;

		PacketTypes( Function<PacketBuffer, AppEngPacket> factory )
		{
			Type c = TypeResolver.resolveGenericType( Function.class, factory.getClass() );
			if( c == TypeResolver.Unknown.class )
			{
				throw new IllegalStateException("Failed to resolve type for AE packet type: " + factory.toString());
			}
			this.packetClass = (Class<? extends AppEngPacket>) c;
			this.factory = factory;

			REVERSE_LOOKUP.put( this.packetClass, this );
		}

		public static PacketTypes getPacket( final int id )
		{
			return ( values() )[id];
		}

		static PacketTypes getID( final Class<? extends AppEngPacket> c )
		{
			return REVERSE_LOOKUP.get( c );
		}

		public AppEngPacket parsePacket( final PacketBuffer in ) throws IllegalArgumentException
		{
			return this.factory.apply( in );
		}
	}
}
