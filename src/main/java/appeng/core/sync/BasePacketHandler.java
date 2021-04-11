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

import appeng.core.sync.packets.MEInteractionPacket;
import net.minecraft.network.PacketBuffer;

import appeng.core.sync.packets.AssemblerAnimationPacket;
import appeng.core.sync.packets.BlockTransitionEffectPacket;
import appeng.core.sync.packets.ClickPacket;
import appeng.core.sync.packets.CompassRequestPacket;
import appeng.core.sync.packets.CompassResponsePacket;
import appeng.core.sync.packets.ConfigButtonPacket;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.core.sync.packets.ConfirmAutoCraftPacket;
import appeng.core.sync.packets.CraftConfirmPlanPacket;
import appeng.core.sync.packets.CraftingStatusPacket;
import appeng.core.sync.packets.FluidSlotPacket;
import appeng.core.sync.packets.InterfaceTerminalPacket;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.core.sync.packets.ItemTransitionEffectPacket;
import appeng.core.sync.packets.JEIRecipePacket;
import appeng.core.sync.packets.LightningPacket;
import appeng.core.sync.packets.MEInventoryUpdatePacket;
import appeng.core.sync.packets.MatterCannonPacket;
import appeng.core.sync.packets.MockExplosionPacket;
import appeng.core.sync.packets.NetworkStatusPacket;
import appeng.core.sync.packets.PaintedEntityPacket;
import appeng.core.sync.packets.PartPlacementPacket;
import appeng.core.sync.packets.PatternSlotPacket;
import appeng.core.sync.packets.ProgressBarPacket;
import appeng.core.sync.packets.SwapSlotsPacket;
import appeng.core.sync.packets.SwitchGuisPacket;

public class BasePacketHandler {
    private static final Map<Class<? extends BasePacket>, PacketTypes> REVERSE_LOOKUP = new HashMap<>();

    public enum PacketTypes {
        COMPASS_REQUEST(CompassRequestPacket.class, CompassRequestPacket::new),

        COMPASS_RESPONSE(CompassResponsePacket.class, CompassResponsePacket::new),

        INVENTORY_ACTION(InventoryActionPacket.class, InventoryActionPacket::new),

        ME_INVENTORY_UPDATE(MEInventoryUpdatePacket.class, MEInventoryUpdatePacket::new),

        ME_INTERACTION(MEInteractionPacket.class, MEInteractionPacket::new),

        CONFIG_BUTTON(ConfigButtonPacket.class, ConfigButtonPacket::new),

        PART_PLACEMENT(PartPlacementPacket.class, PartPlacementPacket::new),

        LIGHTNING(LightningPacket.class, LightningPacket::new),

        MATTER_CANNON(MatterCannonPacket.class, MatterCannonPacket::new),

        MOCK_EXPLOSION(MockExplosionPacket.class, MockExplosionPacket::new),

        VALUE_CONFIG(ConfigValuePacket.class, ConfigValuePacket::new),

        ITEM_TRANSITION_EFFECT(ItemTransitionEffectPacket.class, ItemTransitionEffectPacket::new),

        BLOCK_TRANSITION_EFFECT(BlockTransitionEffectPacket.class, BlockTransitionEffectPacket::new),

        PROGRESS_VALUE(ProgressBarPacket.class, ProgressBarPacket::new),

        CLICK(ClickPacket.class, ClickPacket::new),

        SWITCH_GUIS(SwitchGuisPacket.class, SwitchGuisPacket::new),

        SWAP_SLOTS(SwapSlotsPacket.class, SwapSlotsPacket::new),

        PATTERN_SLOT(PatternSlotPacket.class, PatternSlotPacket::new),

        RECIPE_JEI(JEIRecipePacket.class, JEIRecipePacket::new),

        CONFIRM_AUTO_CRAFT(ConfirmAutoCraftPacket.class, ConfirmAutoCraftPacket::new),

        ASSEMBLER_ANIMATION(AssemblerAnimationPacket.class, AssemblerAnimationPacket::new),

        ME_INTERFACE_UPDATE(InterfaceTerminalPacket.class, InterfaceTerminalPacket::new),

        PAINTED_ENTITY(PaintedEntityPacket.class, PaintedEntityPacket::new),

        FLUID_TANK(FluidSlotPacket.class, FluidSlotPacket::new),

        NETWORK_STATUS(NetworkStatusPacket.class, NetworkStatusPacket::new),

        CRAFT_CONFIRM_PLAN(CraftConfirmPlanPacket.class, CraftConfirmPlanPacket::new),

        CRAFTING_STATUS(CraftingStatusPacket.class, CraftingStatusPacket::new);

        private final Function<PacketBuffer, BasePacket> factory;

        PacketTypes(Class<? extends BasePacket> packetClass, Function<PacketBuffer, BasePacket> factory) {
            this.factory = factory;

            REVERSE_LOOKUP.put(packetClass, this);
        }

        public static PacketTypes getPacket(final int id) {
            return (values())[id];
        }

        public int getPacketId() {
            return ordinal();
        }

        static PacketTypes getID(final Class<? extends BasePacket> c) {
            return REVERSE_LOOKUP.get(c);
        }

        public BasePacket parsePacket(final PacketBuffer in) throws IllegalArgumentException {
            return this.factory.apply(in);
        }
    }
}
