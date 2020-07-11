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

import appeng.core.sync.packets.AssemblerAnimationPacket;
import appeng.core.sync.packets.BlockTransitionEffectPacket;
import appeng.core.sync.packets.ClickPacket;
import appeng.core.sync.packets.CompassRequestPacket;
import appeng.core.sync.packets.CompassResponsePacket;
import appeng.core.sync.packets.CompressedNBTPacket;
import appeng.core.sync.packets.ConfigButtonPacket;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.core.sync.packets.CraftRequestPacket;
import appeng.core.sync.packets.FluidSlotPacket;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.core.sync.packets.ItemTransitionEffectPacket;
import appeng.core.sync.packets.JEIRecipePacket;
import appeng.core.sync.packets.LightningPacket;
import appeng.core.sync.packets.MEFluidInventoryUpdatePacket;
import appeng.core.sync.packets.MEInventoryUpdatePacket;
import appeng.core.sync.packets.MatterCannonPacket;
import appeng.core.sync.packets.MockExplosionPacket;
import appeng.core.sync.packets.PaintedEntityPacket;
import appeng.core.sync.packets.PartPlacementPacket;
import appeng.core.sync.packets.PatternSlotPacket;
import appeng.core.sync.packets.ProgressBarPacket;
import appeng.core.sync.packets.SwapSlotsPacket;
import appeng.core.sync.packets.SwitchGuisPacket;
import appeng.core.sync.packets.TargetFluidStackPacket;
import appeng.core.sync.packets.TargetItemStackPacket;

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

        PACKET_MOCK_EXPLOSION(MockExplosionPacket.class, MockExplosionPacket::new),

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

        PACKET_FLUID_TANK(FluidSlotPacket.class, FluidSlotPacket::new);

        private final Function<PacketBuffer, BasePacket> factory;

        PacketTypes(Class<? extends BasePacket> packetClass, Function<PacketBuffer, BasePacket> factory) {
            this.factory = factory;

            REVERSE_LOOKUP.put(packetClass, this);
        }

        public static PacketTypes getPacket(final int id) {
            return (values())[id];
        }

        static PacketTypes getID(final Class<? extends BasePacket> c) {
            return REVERSE_LOOKUP.get(c);
        }

        public BasePacket parsePacket(final PacketBuffer in) throws IllegalArgumentException {
            return this.factory.apply(in);
        }
    }
}
