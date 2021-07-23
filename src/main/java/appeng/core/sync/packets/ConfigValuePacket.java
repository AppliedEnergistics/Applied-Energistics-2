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

package appeng.core.sync.packets;

import io.netty.buffer.Unpooled;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;

import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.container.implementations.CellWorkbenchContainer;
import appeng.container.implementations.LevelEmitterContainer;
import appeng.container.implementations.PriorityContainer;
import appeng.container.implementations.QuartzKnifeContainer;
import appeng.container.implementations.SecurityStationContainer;
import appeng.container.implementations.StorageBusContainer;
import appeng.container.me.crafting.CraftConfirmContainer;
import appeng.container.me.crafting.CraftingCPUContainer;
import appeng.container.me.crafting.CraftingCPUCyclingContainer;
import appeng.container.me.items.PatternTermContainer;
import appeng.container.me.networktool.NetworkToolContainer;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.fluids.container.FluidLevelEmitterContainer;
import appeng.fluids.container.FluidStorageBusContainer;
import appeng.helpers.IMouseWheelItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ConfigValuePacket extends BasePacket {

    private final String Name;
    private final String Value;

    public ConfigValuePacket(final FriendlyByteBuf stream) {
        this.Name = stream.readUtf(MAX_STRING_LENGTH);
        this.Value = stream.readUtf(MAX_STRING_LENGTH);
    }

    // api
    public ConfigValuePacket(final String name, final String value) {
        this.Name = name;
        this.Value = value;

        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());

        data.writeUtf(name);
        data.writeUtf(value);

        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final Player player) {
        final AbstractContainerMenu c = player.containerMenu;

        if (this.Name.equals("Item") && (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
                && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof IMouseWheelItem
                || !player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()
                        && player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof IMouseWheelItem)) {
            final InteractionHand hand;
            if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
                    && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof IMouseWheelItem) {
                hand = InteractionHand.MAIN_HAND;
            } else if (!player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()
                    && player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof IMouseWheelItem) {
                hand = InteractionHand.OFF_HAND;
            } else {
                return;
            }

            final ItemStack is = player.getItemInHand(hand);
            final IMouseWheelItem si = (IMouseWheelItem) is.getItem();
            si.onWheel(is, this.Value.equals("WheelUp"));
        } else if (this.Name.equals("Terminal.Cpu") && c instanceof CraftingCPUCyclingContainer) {
            final CraftingCPUCyclingContainer qk = (CraftingCPUCyclingContainer) c;
            qk.cycleSelectedCPU(this.Value.equals("Next"));
        } else if (this.Name.equals("Terminal.Start") && c instanceof CraftConfirmContainer) {
            final CraftConfirmContainer qk = (CraftConfirmContainer) c;
            qk.startJob();
        } else if (this.Name.equals("TileCrafting.Cancel") && c instanceof CraftingCPUContainer) {
            final CraftingCPUContainer qk = (CraftingCPUContainer) c;
            qk.cancelCrafting();
        } else if (this.Name.equals("QuartzKnife.Name") && c instanceof QuartzKnifeContainer) {
            final QuartzKnifeContainer qk = (QuartzKnifeContainer) c;
            qk.setName(this.Value);
        } else if (this.Name.equals("TileSecurityStation.ToggleOption") && c instanceof SecurityStationContainer) {
            final SecurityStationContainer sc = (SecurityStationContainer) c;
            sc.toggleSetting(this.Value, player);
        } else if (this.Name.equals("PriorityHost.Priority") && c instanceof PriorityContainer) {
            final PriorityContainer pc = (PriorityContainer) c;
            pc.setPriority(Integer.parseInt(this.Value));
        } else if (this.Name.equals("LevelEmitter.Value") && c instanceof LevelEmitterContainer) {
            final LevelEmitterContainer lvc = (LevelEmitterContainer) c;
            lvc.setReportingValue(Long.parseLong(this.Value));
        } else if (this.Name.equals("FluidLevelEmitter.Value") && c instanceof FluidLevelEmitterContainer) {
            final FluidLevelEmitterContainer lvc = (FluidLevelEmitterContainer) c;
            lvc.setReportingValue(Long.parseLong(this.Value));
        } else if (this.Name.startsWith("PatternTerminal.") && c instanceof PatternTermContainer) {
            final PatternTermContainer cpt = (PatternTermContainer) c;
            if (this.Name.equals("PatternTerminal.CraftMode")) {
                cpt.getPatternTerminal().setCraftingRecipe(this.Value.equals("1"));
            } else if (this.Name.equals("PatternTerminal.Encode")) {
                cpt.encode();
            } else if (this.Name.equals("PatternTerminal.Clear")) {
                cpt.clear();
            } else if (this.Name.equals("PatternTerminal.Substitute")) {
                cpt.getPatternTerminal().setSubstitution(this.Value.equals("1"));
            }
        } else if (this.Name.startsWith("StorageBus.")) {
            if (this.Name.equals("StorageBus.Action")) {
                if (this.Value.equals("Partition")) {
                    if (c instanceof StorageBusContainer) {
                        ((StorageBusContainer) c).partition();
                    } else if (c instanceof FluidStorageBusContainer) {
                        ((FluidStorageBusContainer) c).partition();
                    }
                } else if (this.Value.equals("Clear")) {
                    if (c instanceof StorageBusContainer) {
                        ((StorageBusContainer) c).clear();
                    } else if (c instanceof FluidStorageBusContainer) {
                        ((FluidStorageBusContainer) c).clear();
                    }
                }
            }
        } else if (this.Name.startsWith("CellWorkbench.") && c instanceof CellWorkbenchContainer) {
            final CellWorkbenchContainer ccw = (CellWorkbenchContainer) c;
            if (this.Name.equals("CellWorkbench.Action")) {
                if (this.Value.equals("CopyMode")) {
                    ccw.nextWorkBenchCopyMode();
                } else if (this.Value.equals("Partition")) {
                    ccw.partition();
                } else if (this.Value.equals("Clear")) {
                    ccw.clear();
                }
            } else if (this.Name.equals("CellWorkbench.Fuzzy")) {
                ccw.setFuzzy(FuzzyMode.valueOf(this.Value));
            }
        } else if (c instanceof NetworkToolContainer) {
            if (this.Name.equals("NetworkTool") && this.Value.equals("Toggle")) {
                ((NetworkToolContainer) c).toggleFacadeMode();
            }
        } else if (c instanceof IConfigurableObject) {
            final IConfigManager cm = ((IConfigurableObject) c).getConfigManager();

            for (final Settings e : cm.getSettings()) {
                if (e.name().equals(this.Name)) {
                    final Enum<?> def = cm.getSetting(e);

                    try {
                        cm.putSetting(e, Enum.valueOf(def.getClass(), this.Value));
                    } catch (final IllegalArgumentException err) {
                        // :P
                    }

                    break;
                }
            }
        }
    }

    @Override
    public void clientPacketData(final INetworkInfo network, final Player player) {
        final AbstractContainerMenu c = player.containerMenu;

        if (c instanceof IConfigurableObject) {
            final IConfigManager cm = ((IConfigurableObject) c).getConfigManager();

            for (final Settings e : cm.getSettings()) {
                if (e.name().equals(this.Name)) {
                    final Enum<?> def = cm.getSetting(e);

                    try {
                        cm.putSetting(e, Enum.valueOf(def.getClass(), this.Value));
                    } catch (final IllegalArgumentException err) {
                        // :P
                    }

                    break;
                }
            }
        }
    }
}
