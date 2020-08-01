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

import net.minecraft.network.PacketByteBuf;

import appeng.core.sync.packets.*;

public class BasePacketHandler {
    private static final Map<Class<? extends BasePacket>, PacketTypes> REVERSE_LOOKUP = new HashMap<>();

    public enum PacketTypes {
        PACKET_COMPASS_REQUEST(CompassRequestPacket.class, CompassRequestPacket::new),

        PACKET_COMPASS_RESPONSE(CompassResponsePacket.class, CompassResponsePacket::new),

        PACKET_INVENTORY_ACTION(InventoryActionPacket.class, InventoryActionPacket::new),

        PACKET_ME_INVENTORY_UPDATE(MEInventoryUpdatePacket.class, MEInventoryUpdatePacket::new),

        PACKET_ME_FLUID_INVENTORY_UPDATE(MEFluidInventoryUpdatePacket.class, MEFluidInventoryUpdatePacket::new),

        PACKET_CONFIG_BUTTON(ConfigButtonPacket.class, ConfigButtonPacket::new),

        PACKET_PART_PLACEMENT(PartPlacementPacket.class, PartPlacementPacket::new),

        PACKET_LIGHTNING(LightningPacket.class, LightningPacket::new),

        PACKET_MATTER_CANNON(MatterCannonPacket.class, MatterCannonPacket::new),

        PACKET_VALUE_CONFIG(ConfigValuePacket.class, ConfigValuePacket::new),

        PACKET_ITEM_TRANSITION_EFFECT(ItemTransitionEffectPacket.class, ItemTransitionEffectPacket::new),

        PACKET_BLOCK_TRANSITION_EFFECT(BlockTransitionEffectPacket.class, BlockTransitionEffectPacket::new),

        PACKET_PROGRESS_VALUE(ProgressBarPacket.class, ProgressBarPacket::new),

        PACKET_CLICK(ClickPacket.class, ClickPacket::new),

        PACKET_SWITCH_GUIS(SwitchGuisPacket.class, SwitchGuisPacket::new),

        PACKET_SWAP_SLOTS(SwapSlotsPacket.class, SwapSlotsPacket::new),

        PACKET_PATTERN_SLOT(PatternSlotPacket.class, PatternSlotPacket::new),

        PACKET_RECIPE_JEI(JEIRecipePacket.class, JEIRecipePacket::new),

        PACKET_TARGET_ITEM(TargetItemStackPacket.class, TargetItemStackPacket::new),

        PACKET_TARGET_FLUID(TargetFluidStackPacket.class, TargetFluidStackPacket::new),

        PACKET_CRAFTING_REQUEST(CraftRequestPacket.class, CraftRequestPacket::new),

        PACKET_ASSEMBLER_ANIMATION(AssemblerAnimationPacket.class, AssemblerAnimationPacket::new),

        PACKET_COMPRESSED_NBT(CompressedNBTPacket.class, CompressedNBTPacket::new),

        PACKET_PAINTED_ENTITY(PaintedEntityPacket.class, PaintedEntityPacket::new),

        PACKET_FLUID_TANK(FluidSlotPacket.class, FluidSlotPacket::new),

        SPAWN_ENTITY(SpawnEntityPacket.class, SpawnEntityPacket::new);

        private final Function<PacketByteBuf, BasePacket> factory;

        PacketTypes(Class<? extends BasePacket> packetClass, Function<PacketByteBuf, BasePacket> factory) {
            this.factory = factory;

            REVERSE_LOOKUP.put(packetClass, this);
        }

        public static PacketTypes getPacket(final int id) {
            return (values())[id];
        }

        static PacketTypes getID(final Class<? extends BasePacket> c) {
            return REVERSE_LOOKUP.get(c);
        }

        public BasePacket parsePacket(final PacketByteBuf in) throws IllegalArgumentException {
            return this.factory.apply(in);
        }
    }
}
