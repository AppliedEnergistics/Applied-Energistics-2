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

package appeng.container.implementations;


import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.container.AEBaseContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketCompressedNBT;
import appeng.core.sync.packets.PacketTargetFluidStack;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.fluids.parts.PartFluidInterface;
import appeng.fluids.tile.TileFluidInterface;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidTank;
import appeng.helpers.InventoryAction;
import appeng.parts.misc.PartInterface;
import appeng.parts.reporting.PartFluidInterfaceConfigurationTerminal;
import appeng.tile.misc.TileInterface;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.WrapperRangeItemHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public final class ContainerFluidInterfaceConfigurationTerminal extends AEBaseContainer {

    /**
     * this stuff is all server side..
     */

    private static long autoBase = Long.MIN_VALUE;
    private final Map<IFluidInterfaceHost, FluidConfigTracker> diList = new HashMap<>();
    private final Map<Long, FluidConfigTracker> byId = new HashMap<>();
    private IGrid grid;
    private NBTTagCompound data = new NBTTagCompound();
    private IAEFluidStack clientRequestedTargetFluid;

    public ContainerFluidInterfaceConfigurationTerminal(final InventoryPlayer ip, final PartFluidInterfaceConfigurationTerminal anchor) {
        super(ip, anchor);

        if (Platform.isServer()) {
            this.grid = anchor.getActionableNode().getGrid();
        }

        this.bindPlayerInventory(ip, 14, 235 - /* height of player inventory */82);
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isClient()) {
            return;
        }

        super.detectAndSendChanges();

        if (this.grid == null) {
            return;
        }

        int total = 0;
        boolean missing = false;

        final IActionHost host = this.getActionHost();
        if (host != null) {
            final IGridNode agn = host.getActionableNode();
            if (agn != null && agn.isActive()) {
                for (final IGridNode gn : this.grid.getMachines(TileFluidInterface.class)) {
                    if (gn.isActive()) {
                        final IFluidInterfaceHost ih = (IFluidInterfaceHost) gn.getMachine();
                        if (ih.getDualityFluidInterface().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.NO) {
                            continue;
                        }

                        final FluidConfigTracker t = this.diList.get(ih);

                        if (t == null) {
                            missing = true;
                        } else {
                            final DualityFluidInterface dual = ih.getDualityFluidInterface();
                            if (!t.unlocalizedName.equals(dual.getTermName())) {
                                missing = true;
                            }
                        }

                        total++;
                    }
                }

                for (final IGridNode gn : this.grid.getMachines(PartFluidInterface.class)) {
                    if (gn.isActive()) {
                        final IFluidInterfaceHost ih = (IFluidInterfaceHost) gn.getMachine();
                        if (ih.getDualityFluidInterface().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.NO) {
                            continue;
                        }

                        final FluidConfigTracker t = this.diList.get(ih);

                        if (t == null) {
                            missing = true;
                        } else {
                            final DualityFluidInterface dual = ih.getDualityFluidInterface();
                            if (!t.unlocalizedName.equals(dual.getTermName())) {
                                missing = true;
                            }
                        }

                        total++;
                    }
                }
            }
        }

        if (total != this.diList.size() || missing) {
            this.regenList(this.data);
        } else {
            for (final Entry<IFluidInterfaceHost, FluidConfigTracker> en : this.diList.entrySet()) {
                final FluidConfigTracker inv = en.getValue();
                for (int x = 0; x < inv.server.getSlots(); x++) {
                    if ((inv.server.getFluidInSlot(x) == null && inv.client.getFluidInSlot(x) != null) ||
                            (inv.server.getFluidInSlot(x) != null && !inv.server.getFluidInSlot(x).equals(inv.client.getFluidInSlot(x)))) {
                        this.addFluids(this.data, inv, x, 1);
                    }
                }
            }
        }

        if (!this.data.hasNoTags()) {
            try {
                NetworkHandler.instance().sendTo(new PacketCompressedNBT(this.data), (EntityPlayerMP) this.getPlayerInv().player);
            } catch (final IOException e) {
                // :P
            }

            this.data = new NBTTagCompound();
        }
    }

    public FluidConfigTracker getSlotByID(long id) {
        return this.byId.get(id);
    }

    @Override
    public void doAction(final EntityPlayerMP player, final InventoryAction action, final int slot, final long id) {
        final FluidConfigTracker inv = this.byId.get(id);
        if (inv != null) {
            ItemStack itemInHand = player.inventory.getItemStack();
            IFluidHandlerItem c = itemInHand.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (c != null) {
                FluidStack fs = c.drain(Integer.MAX_VALUE, false);
                if (fs != null) {
                    inv.server.setFluidInSlot(slot, AEFluidStack.fromFluidStack(fs));
                    return;
                }
                return;
            }
            inv.server.setFluidInSlot(slot, null);

            this.updateHeld(player);
        }
    }

    public void setTargetStack(final IAEFluidStack stack) {
        if (Platform.isClient()) {
            if (stack == null && this.clientRequestedTargetFluid == null) {
                return;
            }
            if (stack != null && this.clientRequestedTargetFluid != null && stack.getFluidStack().isFluidEqual(this.clientRequestedTargetFluid.getFluidStack())) {
                return;
            }
            NetworkHandler.instance().sendToServer(new PacketTargetFluidStack((AEFluidStack) stack));
        }

        this.clientRequestedTargetFluid = stack == null ? null : stack.copy();
    }

    private void regenList(final NBTTagCompound data) {
        this.byId.clear();
        this.diList.clear();

        final IActionHost host = this.getActionHost();
        if (host != null) {
            final IGridNode agn = host.getActionableNode();
            if (agn != null && agn.isActive()) {
                for (final IGridNode gn : this.grid.getMachines(TileFluidInterface.class)) {
                    final IFluidInterfaceHost ih = (IFluidInterfaceHost) gn.getMachine();
                    final DualityFluidInterface dual = ih.getDualityFluidInterface();
                    if (gn.isActive() && dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES) {
                        this.diList.put(ih, new FluidConfigTracker(dual, (AEFluidInventory) dual.getConfig(), dual.getTermName()));
                    }
                }

                for (final IGridNode gn : this.grid.getMachines(PartFluidInterface.class)) {
                    final IFluidInterfaceHost ih = (IFluidInterfaceHost) gn.getMachine();
                    final DualityFluidInterface dual = ih.getDualityFluidInterface();
                    if (gn.isActive() && dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES) {
                        this.diList.put(ih, new FluidConfigTracker(dual, (AEFluidInventory) dual.getConfig(), dual.getTermName()));
                    }
                }
            }
        }

        data.setBoolean("clear", true);

        for (final Entry<IFluidInterfaceHost, FluidConfigTracker> en : this.diList.entrySet()) {
            final FluidConfigTracker inv = en.getValue();
            this.byId.put(inv.which, inv);
            this.addFluids(data, inv, 0, inv.server.getSlots());
        }
    }

    private void addFluids(final NBTTagCompound data, final FluidConfigTracker inv, final int offset, final int length) {
        final String name = '=' + Long.toString(inv.which, Character.MAX_RADIX);
        final NBTTagCompound tag = data.getCompoundTag(name);

        if (tag.hasNoTags()) {
            tag.setLong("sortBy", inv.sortBy);
            tag.setString("un", inv.unlocalizedName);
            tag.setTag("pos", NBTUtil.createPosTag(inv.pos));
            tag.setInteger("dim", inv.dim);
        }

        for (int x = 0; x < length; x++) {
            final NBTTagCompound fluidNBT = new NBTTagCompound();

            final IAEFluidStack iaeFluidStack = inv.server.getFluidInSlot(x + offset);

            // "update" client side.
            inv.client.setFluidInSlot(x + offset, iaeFluidStack == null ? null : iaeFluidStack.copy());

            if (iaeFluidStack != null) {
                iaeFluidStack.writeToNBT(fluidNBT);
            }

            tag.setTag(Integer.toString(x + offset), fluidNBT);
        }

        data.setTag(name, tag);
    }

    public static class FluidConfigTracker {

        private final long sortBy;
        private final long which = autoBase++;
        private final String unlocalizedName;
        private final IAEFluidTank client;
        private final IAEFluidTank server;
        private final BlockPos pos;
        private final int dim;

        public FluidConfigTracker(final DualityFluidInterface dual, final AEFluidInventory configSlots, final String unlocalizedName) {
            this.server = configSlots;
            this.client = new AEFluidInventory(null, this.server.getSlots());
            this.unlocalizedName = unlocalizedName;
            this.sortBy = dual.getSortValue();
            this.pos = dual.getLocation().getPos();
            this.dim = dual.getLocation().getWorld().provider.getDimension();
        }

        public IAEFluidTank getServer() {
            return server;
        }
    }
}
