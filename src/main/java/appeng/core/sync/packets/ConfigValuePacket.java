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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.IMouseWheelItem;
import appeng.menu.implementations.CellWorkbenchMenu;
import appeng.menu.implementations.FluidLevelEmitterMenu;
import appeng.menu.implementations.FluidStorageBusMenu;
import appeng.menu.implementations.ItemLevelEmitterMenu;
import appeng.menu.implementations.ItemStorageBusMenu;
import appeng.menu.implementations.PriorityMenu;
import appeng.menu.implementations.QuartzKnifeMenu;
import appeng.menu.implementations.SecurityStationMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingCPUCyclingMenu;
import appeng.menu.me.crafting.CraftingCPUMenu;
import appeng.menu.me.items.PatternTermMenu;
import appeng.menu.me.networktool.NetworkToolMenu;

public class ConfigValuePacket extends BasePacket {

    private final String Name;
    private final String Value;

    public ConfigValuePacket(final FriendlyByteBuf stream) {
        this.Name = stream.readUtf();
        this.Value = stream.readUtf();
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
        } else if (this.Name.equals("Terminal.Cpu") && c instanceof CraftingCPUCyclingMenu) {
            final CraftingCPUCyclingMenu qk = (CraftingCPUCyclingMenu) c;
            qk.cycleSelectedCPU(this.Value.equals("Next"));
        } else if (this.Name.equals("Terminal.Start") && c instanceof CraftConfirmMenu) {
            final CraftConfirmMenu qk = (CraftConfirmMenu) c;
            qk.startJob();
        } else if (this.Name.equals("TileCrafting.Cancel") && c instanceof CraftingCPUMenu) {
            final CraftingCPUMenu qk = (CraftingCPUMenu) c;
            qk.cancelCrafting();
        } else if (this.Name.equals("QuartzKnife.Name") && c instanceof QuartzKnifeMenu) {
            final QuartzKnifeMenu qk = (QuartzKnifeMenu) c;
            qk.setName(this.Value);
        } else if (this.Name.equals("TileSecurityStation.ToggleOption") && c instanceof SecurityStationMenu) {
            final SecurityStationMenu sc = (SecurityStationMenu) c;
            sc.toggleSetting(this.Value, player);
        } else if (this.Name.equals("PriorityHost.Priority") && c instanceof PriorityMenu) {
            final PriorityMenu pc = (PriorityMenu) c;
            pc.setPriority(Integer.parseInt(this.Value));
        } else if (this.Name.equals("LevelEmitter.Value") && c instanceof ItemLevelEmitterMenu) {
            final ItemLevelEmitterMenu lvc = (ItemLevelEmitterMenu) c;
            lvc.setReportingValue(Long.parseLong(this.Value));
        } else if (this.Name.equals("FluidLevelEmitter.Value") && c instanceof FluidLevelEmitterMenu) {
            final FluidLevelEmitterMenu lvc = (FluidLevelEmitterMenu) c;
            lvc.setReportingValue(Long.parseLong(this.Value));
        } else if (this.Name.startsWith("PatternTerminal.") && c instanceof PatternTermMenu) {
            final PatternTermMenu cpt = (PatternTermMenu) c;
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
                    if (c instanceof ItemStorageBusMenu) {
                        ((ItemStorageBusMenu) c).partition();
                    } else if (c instanceof FluidStorageBusMenu) {
                        ((FluidStorageBusMenu) c).partition();
                    }
                } else if (this.Value.equals("Clear")) {
                    if (c instanceof ItemStorageBusMenu) {
                        ((ItemStorageBusMenu) c).clear();
                    } else if (c instanceof FluidStorageBusMenu) {
                        ((FluidStorageBusMenu) c).clear();
                    }
                }
            }
        } else if (this.Name.startsWith("CellWorkbench.") && c instanceof CellWorkbenchMenu) {
            final CellWorkbenchMenu ccw = (CellWorkbenchMenu) c;
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
        } else if (c instanceof NetworkToolMenu) {
            if (this.Name.equals("NetworkTool") && this.Value.equals("Toggle")) {
                ((NetworkToolMenu) c).toggleFacadeMode();
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
