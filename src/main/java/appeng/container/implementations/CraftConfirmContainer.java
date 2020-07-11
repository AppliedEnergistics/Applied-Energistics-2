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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.guisync.GuiSync;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.MEInventoryUpdatePacket;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.me.helpers.PlayerSource;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.parts.reporting.PatternTerminalPart;
import appeng.parts.reporting.TerminalPart;
import appeng.util.Platform;

public class CraftConfirmContainer extends AEBaseContainer {

    public static ContainerType<CraftConfirmContainer> TYPE;

    private static final ContainerHelper<CraftConfirmContainer, ITerminalHost> helper = new ContainerHelper<>(
            CraftConfirmContainer::new, ITerminalHost.class, SecurityPermissions.CRAFT);

    public static CraftConfirmContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final ArrayList<CraftingCPURecord> cpus = new ArrayList<>();
    private Future<ICraftingJob> job;
    private ICraftingJob result;
    @GuiSync(0)
    public long bytesUsed;
    @GuiSync(1)
    public long cpuBytesAvail;
    @GuiSync(2)
    public int cpuCoProcessors;
    @GuiSync(3)
    public boolean autoStart = false;
    @GuiSync(4)
    public boolean simulation = true;
    @GuiSync(5)
    public int selectedCpu = -1;
    @GuiSync(6)
    public boolean noCPU = true;
    @GuiSync(7)
    public ITextComponent myName;

    public CraftConfirmContainer(int id, PlayerInventory ip, ITerminalHost te) {
        super(TYPE, id, ip, te);
    }

    public void cycleCpu(final boolean next) {
        if (next) {
            this.setSelectedCpu(this.getSelectedCpu() + 1);
        } else {
            this.setSelectedCpu(this.getSelectedCpu() - 1);
        }

        if (this.getSelectedCpu() < -1) {
            this.setSelectedCpu(this.cpus.size() - 1);
        } else if (this.getSelectedCpu() >= this.cpus.size()) {
            this.setSelectedCpu(-1);
        }

        if (this.getSelectedCpu() == -1) {
            this.setCpuAvailableBytes(0);
            this.setCpuCoProcessors(0);
            this.setName(null);
        } else {
            CraftingCPURecord cpu = this.cpus.get(this.getSelectedCpu());
            this.setName(cpu.getName());
            this.setCpuAvailableBytes(cpu.getSize());
            this.setCpuCoProcessors(cpu.getProcessors());
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isClient()) {
            return;
        }

        final ICraftingGrid cc = this.getGrid().getCache(ICraftingGrid.class);
        final ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();

        int matches = 0;
        boolean changed = false;
        for (final ICraftingCPU c : cpuSet) {
            boolean found = false;
            for (final CraftingCPURecord ccr : this.cpus) {
                if (ccr.getCpu() == c) {
                    found = true;
                    break;
                }
            }

            final boolean matched = this.cpuMatches(c);

            if (matched) {
                matches++;
            }

            if (found == !matched) {
                changed = true;
            }
        }

        if (changed || this.cpus.size() != matches) {
            this.cpus.clear();
            for (final ICraftingCPU c : cpuSet) {
                if (this.cpuMatches(c)) {
                    this.cpus.add(new CraftingCPURecord(c.getAvailableStorage(), c.getCoProcessors(), c));
                }
            }

            this.sendCPUs();
        }

        this.setNoCPU(this.cpus.isEmpty());

        super.detectAndSendChanges();

        if (this.getJob() != null && this.getJob().isDone()) {
            try {
                this.result = this.getJob().get();

                if (!this.result.isSimulation()) {
                    this.setSimulation(false);
                    if (this.isAutoStart()) {
                        this.startJob();
                        return;
                    }
                } else {
                    this.setSimulation(true);
                }

                try {
                    final MEInventoryUpdatePacket a = new MEInventoryUpdatePacket((byte) 0);
                    final MEInventoryUpdatePacket b = new MEInventoryUpdatePacket((byte) 1);
                    final MEInventoryUpdatePacket c = this.result.isSimulation() ? new MEInventoryUpdatePacket((byte) 2)
                            : null;

                    final IItemList<IAEItemStack> plan = Api.instance().storage()
                            .getStorageChannel(IItemStorageChannel.class).createList();
                    this.result.populatePlan(plan);

                    this.setUsedBytes(this.result.getByteTotal());

                    for (final IAEItemStack out : plan) {

                        IAEItemStack o = out.copy();
                        o.reset();
                        o.setStackSize(out.getStackSize());

                        final IAEItemStack p = out.copy();
                        p.reset();
                        p.setStackSize(out.getCountRequestable());

                        final IStorageGrid sg = this.getGrid().getCache(IStorageGrid.class);
                        final IMEInventory<IAEItemStack> items = sg
                                .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));

                        IAEItemStack m = null;
                        if (c != null && this.result.isSimulation()) {
                            m = o.copy();
                            o = items.extractItems(o, Actionable.SIMULATE, this.getActionSource());

                            if (o == null) {
                                o = m.copy();
                                o.setStackSize(0);
                            }

                            m.setStackSize(m.getStackSize() - o.getStackSize());
                        }

                        if (o.getStackSize() > 0) {
                            a.appendItem(o);
                        }

                        if (p.getStackSize() > 0) {
                            b.appendItem(p);
                        }

                        if (c != null && m != null && m.getStackSize() > 0) {
                            c.appendItem(m);
                        }
                    }

                    for (final Object g : this.listeners) {
                        if (g instanceof PlayerEntity) {
                            NetworkHandler.instance().sendTo(a, (ServerPlayerEntity) g);
                            NetworkHandler.instance().sendTo(b, (ServerPlayerEntity) g);
                            if (c != null) {
                                NetworkHandler.instance().sendTo(c, (ServerPlayerEntity) g);
                            }
                        }
                    }
                } catch (final IOException e) {
                    // :P
                }
            } catch (final Throwable e) {
                this.getPlayerInv().player.sendMessage(new StringTextComponent("Error: " + e.toString()));
                AELog.debug(e);
                this.setValidContainer(false);
                this.result = null;
            }

            this.setJob(null);
        }
        this.verifyPermissions(SecurityPermissions.CRAFT, false);
    }

    private IGrid getGrid() {
        final IActionHost h = ((IActionHost) this.getTarget());
        return h.getActionableNode().getGrid();
    }

    private boolean cpuMatches(final ICraftingCPU c) {
        return c.getAvailableStorage() >= this.getUsedBytes() && !c.isBusy();
    }

    private void sendCPUs() {
        Collections.sort(this.cpus);

        if (this.getSelectedCpu() >= this.cpus.size()) {
            this.setSelectedCpu(-1);
            this.setCpuAvailableBytes(0);
            this.setCpuCoProcessors(0);
            this.setName(null);
        } else if (this.getSelectedCpu() != -1) {
            CraftingCPURecord cpu = this.cpus.get(this.getSelectedCpu());
            this.setName(cpu.getName());
            this.setCpuAvailableBytes(cpu.getSize());
            this.setCpuCoProcessors(cpu.getProcessors());
        }
    }

    public void startJob() {
        ContainerType<?> originalGui = null;

        final IActionHost ah = this.getActionHost();
        if (ah instanceof WirelessTerminalGuiObject) {
            originalGui = WirelessTermContainer.TYPE;
        }

        if (ah instanceof TerminalPart) {
            originalGui = MEMonitorableContainer.TYPE;
        }

        if (ah instanceof CraftingTerminalPart) {
            originalGui = CraftingTermContainer.TYPE;
        }

        if (ah instanceof PatternTerminalPart) {
            originalGui = PatternTermContainer.TYPE;
        }

        if (this.result != null && !this.isSimulation()) {
            final ICraftingGrid cc = this.getGrid().getCache(ICraftingGrid.class);
            final ICraftingLink g = cc.submitJob(this.result, null,
                    this.getSelectedCpu() == -1 ? null : this.cpus.get(this.getSelectedCpu()).getCpu(), true,
                    this.getActionSrc());
            this.setAutoStart(false);
            if (g != null && originalGui != null && this.getLocator() != null) {
                ContainerOpener.openContainer(originalGui, getPlayerInventory().player, getLocator());
            }
        }
    }

    private IActionSource getActionSrc() {
        return new PlayerSource(this.getPlayerInv().player, (IActionHost) this.getTarget());
    }

    @Override
    public void removeListener(final IContainerListener c) {
        super.removeListener(c);
        if (this.getJob() != null) {
            this.getJob().cancel(true);
            this.setJob(null);
        }
    }

    @Override
    public void onContainerClosed(final PlayerEntity par1PlayerEntity) {
        super.onContainerClosed(par1PlayerEntity);
        if (this.getJob() != null) {
            this.getJob().cancel(true);
            this.setJob(null);
        }
    }

    public World getWorld() {
        return this.getPlayerInv().player.world;
    }

    public boolean isAutoStart() {
        return this.autoStart;
    }

    public void setAutoStart(final boolean autoStart) {
        this.autoStart = autoStart;
    }

    public long getUsedBytes() {
        return this.bytesUsed;
    }

    private void setUsedBytes(final long bytesUsed) {
        this.bytesUsed = bytesUsed;
    }

    public long getCpuAvailableBytes() {
        return this.cpuBytesAvail;
    }

    private void setCpuAvailableBytes(final long cpuBytesAvail) {
        this.cpuBytesAvail = cpuBytesAvail;
    }

    public int getCpuCoProcessors() {
        return this.cpuCoProcessors;
    }

    private void setCpuCoProcessors(final int cpuCoProcessors) {
        this.cpuCoProcessors = cpuCoProcessors;
    }

    public int getSelectedCpu() {
        return this.selectedCpu;
    }

    private void setSelectedCpu(final int selectedCpu) {
        this.selectedCpu = selectedCpu;
    }

    public ITextComponent getName() {
        return this.myName;
    }

    private void setName(@Nullable final ITextComponent myName) {
        this.myName = myName;
    }

    public boolean hasNoCPU() {
        return this.noCPU;
    }

    private void setNoCPU(final boolean noCPU) {
        this.noCPU = noCPU;
    }

    public boolean isSimulation() {
        return this.simulation;
    }

    private void setSimulation(final boolean simulation) {
        this.simulation = simulation;
    }

    private Future<ICraftingJob> getJob() {
        return this.job;
    }

    public void setJob(final Future<ICraftingJob> job) {
        this.job = job;
    }
}
