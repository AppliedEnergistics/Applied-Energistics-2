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

import java.io.IOException;

import io.netty.buffer.Unpooled;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;

import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.implementations.GuiCraftingCPU;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCellWorkbench;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.container.implementations.ContainerCraftingCPU;
import appeng.container.implementations.ContainerCraftingStatus;
import appeng.container.implementations.ContainerLevelEmitter;
import appeng.container.implementations.ContainerNetworkTool;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.implementations.ContainerPriority;
import appeng.container.implementations.ContainerQuartzKnife;
import appeng.container.implementations.ContainerSecurityStation;
import appeng.container.implementations.ContainerStorageBus;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.fluids.container.ContainerFluidLevelEmitter;
import appeng.fluids.container.ContainerFluidStorageBus;
import appeng.helpers.IMouseWheelItem;

public class PacketValueConfig extends AppEngPacket {

    private final String Name;
    private final String Value;

    public PacketValueConfig(final PacketBuffer stream) {
        this.Name = stream.readString();
        this.Value = stream.readString();
        // dis.close();
    }

    // api
    public PacketValueConfig(final String name, final String value) {
        this.Name = name;
        this.Value = value;

        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());

        data.writeInt(this.getPacketID());

        data.writeString(name);
        data.writeString(value);

        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final PlayerEntity player) {
        final Container c = player.openContainer;

        if (this.Name.equals("Item") && ((!player.getHeldItem(Hand.MAIN_HAND).isEmpty()
                && player.getHeldItem(Hand.MAIN_HAND).getItem() instanceof IMouseWheelItem)
                || (!player.getHeldItem(Hand.OFF_HAND).isEmpty()
                        && player.getHeldItem(Hand.OFF_HAND).getItem() instanceof IMouseWheelItem))) {
            final Hand hand;
            if (!player.getHeldItem(Hand.MAIN_HAND).isEmpty()
                    && player.getHeldItem(Hand.MAIN_HAND).getItem() instanceof IMouseWheelItem) {
                hand = Hand.MAIN_HAND;
            } else if (!player.getHeldItem(Hand.OFF_HAND).isEmpty()
                    && player.getHeldItem(Hand.OFF_HAND).getItem() instanceof IMouseWheelItem) {
                hand = Hand.OFF_HAND;
            } else {
                return;
            }

            final ItemStack is = player.getHeldItem(hand);
            final IMouseWheelItem si = (IMouseWheelItem) is.getItem();
            si.onWheel(is, this.Value.equals("WheelUp"));
        } else if (this.Name.equals("Terminal.Cpu") && c instanceof ContainerCraftingStatus) {
            final ContainerCraftingStatus qk = (ContainerCraftingStatus) c;
            qk.cycleCpu(this.Value.equals("Next"));
        } else if (this.Name.equals("Terminal.Cpu") && c instanceof ContainerCraftConfirm) {
            final ContainerCraftConfirm qk = (ContainerCraftConfirm) c;
            qk.cycleCpu(this.Value.equals("Next"));
        } else if (this.Name.equals("Terminal.Start") && c instanceof ContainerCraftConfirm) {
            final ContainerCraftConfirm qk = (ContainerCraftConfirm) c;
            qk.startJob();
        } else if (this.Name.equals("TileCrafting.Cancel") && c instanceof ContainerCraftingCPU) {
            final ContainerCraftingCPU qk = (ContainerCraftingCPU) c;
            qk.cancelCrafting();
        } else if (this.Name.equals("QuartzKnife.Name") && c instanceof ContainerQuartzKnife) {
            final ContainerQuartzKnife qk = (ContainerQuartzKnife) c;
            qk.setName(this.Value);
        } else if (this.Name.equals("TileSecurityStation.ToggleOption") && c instanceof ContainerSecurityStation) {
            final ContainerSecurityStation sc = (ContainerSecurityStation) c;
            sc.toggleSetting(this.Value, player);
        } else if (this.Name.equals("PriorityHost.Priority") && c instanceof ContainerPriority) {
            final ContainerPriority pc = (ContainerPriority) c;
            pc.setPriority(Integer.parseInt(this.Value), player);
        } else if (this.Name.equals("LevelEmitter.Value") && c instanceof ContainerLevelEmitter) {
            final ContainerLevelEmitter lvc = (ContainerLevelEmitter) c;
            lvc.setLevel(Long.parseLong(this.Value), player);
        } else if (this.Name.equals("FluidLevelEmitter.Value") && c instanceof ContainerFluidLevelEmitter) {
            final ContainerFluidLevelEmitter lvc = (ContainerFluidLevelEmitter) c;
            lvc.setLevel(Long.parseLong(this.Value), player);
        } else if (this.Name.startsWith("PatternTerminal.") && c instanceof ContainerPatternTerm) {
            final ContainerPatternTerm cpt = (ContainerPatternTerm) c;
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
                    if (c instanceof ContainerStorageBus) {
                        ((ContainerStorageBus) c).partition();
                    } else if (c instanceof ContainerFluidStorageBus) {
                        ((ContainerFluidStorageBus) c).partition();
                    }
                } else if (this.Value.equals("Clear")) {
                    if (c instanceof ContainerStorageBus) {
                        ((ContainerStorageBus) c).clear();
                    } else if (c instanceof ContainerFluidStorageBus) {
                        ((ContainerFluidStorageBus) c).clear();
                    }
                }
            }
        } else if (this.Name.startsWith("CellWorkbench.") && c instanceof ContainerCellWorkbench) {
            final ContainerCellWorkbench ccw = (ContainerCellWorkbench) c;
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
        } else if (c instanceof ContainerNetworkTool) {
            if (this.Name.equals("NetworkTool") && this.Value.equals("Toggle")) {
                ((ContainerNetworkTool) c).toggleFacadeMode();
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
    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        final Container c = player.openContainer;

        if (this.Name.equals("CustomName") && c instanceof AEBaseContainer) {
            ((AEBaseContainer) c).setCustomName(this.Value);
        } else if (this.Name.startsWith("SyncDat.")) {
            ((AEBaseContainer) c).stringSync(Integer.parseInt(this.Name.substring(8)), this.Value);
        } else if (this.Name.equals("CraftingStatus") && this.Value.equals("Clear")) {
            final Screen gs = Minecraft.getInstance().currentScreen;
            if (gs instanceof GuiCraftingCPU) {
                ((GuiCraftingCPU) gs).clearItems();
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
}
