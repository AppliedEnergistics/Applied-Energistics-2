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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.api.config.Setting;
import appeng.api.util.IConfigurableObject;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.IMouseWheelItem;
import appeng.menu.implementations.CellWorkbenchMenu;
import appeng.menu.implementations.FluidStorageBusMenu;
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

    private final String name;
    private final String value;

    public ConfigValuePacket(final FriendlyByteBuf stream) {
        this.name = stream.readUtf();
        this.value = stream.readUtf();
    }

    // api
    public ConfigValuePacket(final String name, final String value) {
        this.name = name;
        this.value = value;

        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());

        data.writeUtf(name);
        data.writeUtf(value);

        this.configureWrite(data);
    }

    public <T extends Enum<T>> ConfigValuePacket(Setting<T> setting, T value) {
        this(setting.getName(), value.name());
        if (!setting.getValues().contains(value)) {
            throw new IllegalStateException(value + " not a valid value for " + setting);
        }
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final ServerPlayer player) {
        final AbstractContainerMenu c = player.containerMenu;

        if (this.name.equals("Item") && (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
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
            si.onWheel(is, this.value.equals("WheelUp"));
        } else if (this.name.equals("Terminal.Cpu") && c instanceof CraftingCPUCyclingMenu qk) {
            qk.cycleSelectedCPU(this.value.equals("Next"));
        } else if (this.name.equals("Terminal.Start") && c instanceof CraftConfirmMenu qk) {
            qk.startJob();
        } else if (this.name.equals("TileCrafting.Cancel") && c instanceof CraftingCPUMenu qk) {
            qk.cancelCrafting();
        } else if (this.name.equals("QuartzKnife.Name") && c instanceof QuartzKnifeMenu qk) {
            qk.setName(this.value);
        } else if (this.name.equals("TileSecurityStation.ToggleOption") && c instanceof SecurityStationMenu sc) {
            sc.toggleSetting(this.value, player);
        } else if (this.name.equals("PriorityHost.Priority") && c instanceof PriorityMenu pc) {
            pc.setPriority(Integer.parseInt(this.value));
        } else if (this.name.startsWith("PatternTerminal.") && c instanceof PatternTermMenu cpt) {
            if (this.name.equals("PatternTerminal.CraftMode")) {
                cpt.getPatternTerminal().setCraftingRecipe(this.value.equals("1"));
            } else if (this.name.equals("PatternTerminal.Encode")) {
                cpt.encode();
            } else if (this.name.equals("PatternTerminal.Clear")) {
                cpt.clear();
            } else if (this.name.equals("PatternTerminal.Substitute")) {
                cpt.getPatternTerminal().setSubstitution(this.value.equals("1"));
            }
        } else if (this.name.startsWith("StorageBus.")) {
            if (this.name.equals("StorageBus.Action")) {
                if (this.value.equals("Partition")) {
                    if (c instanceof ItemStorageBusMenu) {
                        ((ItemStorageBusMenu) c).partition();
                    } else if (c instanceof FluidStorageBusMenu) {
                        ((FluidStorageBusMenu) c).partition();
                    }
                } else if (this.value.equals("Clear")) {
                    if (c instanceof ItemStorageBusMenu) {
                        ((ItemStorageBusMenu) c).clear();
                    } else if (c instanceof FluidStorageBusMenu) {
                        ((FluidStorageBusMenu) c).clear();
                    }
                }
            }
        } else if (this.name.startsWith("CellWorkbench.") && c instanceof CellWorkbenchMenu ccw) {
            if (this.name.equals("CellWorkbench.Action")) {
                if (this.value.equals("CopyMode")) {
                    ccw.nextWorkBenchCopyMode();
                } else if (this.value.equals("Partition")) {
                    ccw.partition();
                } else if (this.value.equals("Clear")) {
                    ccw.clear();
                }
            } else if (this.name.equals("CellWorkbench.Fuzzy")) {
                ccw.setFuzzy(FuzzyMode.valueOf(this.value));
            }
        } else if (c instanceof NetworkToolMenu) {
            if (this.name.equals("NetworkTool") && this.value.equals("Toggle")) {
                ((NetworkToolMenu) c).toggleFacadeMode();
            }
        } else if (c instanceof IConfigurableObject configurableObject) {
            loadSetting(configurableObject);
        }
    }

    @Override
    public void clientPacketData(final INetworkInfo network, final Player player) {
        final AbstractContainerMenu c = player.containerMenu;

        if (c instanceof IConfigurableObject configurableObject) {
            loadSetting(configurableObject);
        }
    }

    private void loadSetting(IConfigurableObject configurableObject) {
        var cm = configurableObject.getConfigManager();

        for (var setting : cm.getSettings()) {
            if (setting.getName().equals(this.name)) {
                setting.setFromString(cm, value);
                break;
            }
        }
    }

}
