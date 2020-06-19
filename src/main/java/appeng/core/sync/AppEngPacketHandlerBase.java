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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.network.PacketBuffer;

import appeng.core.sync.packets.*;

public class AppEngPacketHandlerBase {
    private static final Map<Class<? extends AppEngPacket>, PacketTypes> REVERSE_LOOKUP = new HashMap<>();

    public enum PacketTypes {
        PACKET_COMPASS_REQUEST(PacketCompassRequest.class, PacketCompassRequest::new),

        PACKET_COMPASS_RESPONSE(PacketCompassResponse.class, PacketCompassResponse::new),

        PACKET_INVENTORY_ACTION(PacketInventoryAction.class, PacketInventoryAction::new),

        PACKET_ME_INVENTORY_UPDATE(PacketMEInventoryUpdate.class, PacketMEInventoryUpdate::new),

        PACKET_ME_FLUID_INVENTORY_UPDATE(PacketMEFluidInventoryUpdate.class, PacketMEFluidInventoryUpdate::new),

        PACKET_CONFIG_BUTTON(PacketConfigButton.class, PacketConfigButton::new),

        PACKET_PART_PLACEMENT(PacketPartPlacement.class, PacketPartPlacement::new),

        PACKET_LIGHTNING(PacketLightning.class, PacketLightning::new),

        PACKET_MATTER_CANNON(PacketMatterCannon.class, PacketMatterCannon::new),

        PACKET_MOCK_EXPLOSION(PacketMockExplosion.class, PacketMockExplosion::new),

        PACKET_VALUE_CONFIG(PacketValueConfig.class, PacketValueConfig::new),

        PACKET_ITEM_TRANSITION_EFFECT(PacketItemTransitionEffect.class, PacketItemTransitionEffect::new),

        PACKET_BLOCK_TRANSITION_EFFECT(PacketBlockTransitionEffect.class, PacketBlockTransitionEffect::new),

        PACKET_PROGRESS_VALUE(PacketProgressBar.class, PacketProgressBar::new),

        PACKET_CLICK(PacketClick.class, PacketClick::new),

        PACKET_SWITCH_GUIS(PacketSwitchGuis.class, PacketSwitchGuis::new),

        PACKET_SWAP_SLOTS(PacketSwapSlots.class, PacketSwapSlots::new),

        PACKET_PATTERN_SLOT(PacketPatternSlot.class, PacketPatternSlot::new),

        PACKET_RECIPE_JEI(PacketJEIRecipe.class, PacketJEIRecipe::new),

        PACKET_TARGET_ITEM(PacketTargetItemStack.class, PacketTargetItemStack::new),

        PACKET_TARGET_FLUID(PacketTargetFluidStack.class, PacketTargetFluidStack::new),

        PACKET_CRAFTING_REQUEST(PacketCraftRequest.class, PacketCraftRequest::new),

        PACKET_ASSEMBLER_ANIMATION(PacketAssemblerAnimation.class, PacketAssemblerAnimation::new),

        PACKET_COMPRESSED_NBT(PacketCompressedNBT.class, PacketCompressedNBT::new),

        PACKET_PAINTED_ENTITY(PacketPaintedEntity.class, PacketPaintedEntity::new),

        PACKET_FLUID_TANK(PacketFluidSlot.class, PacketFluidSlot::new);

        private final Function<PacketBuffer, AppEngPacket> factory;

        PacketTypes(Class<? extends AppEngPacket> packetClass, Function<PacketBuffer, AppEngPacket> factory) {
            this.factory = factory;

            REVERSE_LOOKUP.put(packetClass, this);
        }

        public static PacketTypes getPacket(final int id) {
            return (values())[id];
        }

        static PacketTypes getID(final Class<? extends AppEngPacket> c) {
            return REVERSE_LOOKUP.get(c);
        }

        public AppEngPacket parsePacket(final PacketBuffer in) throws IllegalArgumentException {
            return this.factory.apply(in);
        }
    }
}
