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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.storage.ITerminalHost;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.util.Platform;

public class CraftingStatusContainer extends CraftingCPUContainer {

    public static ScreenHandlerType<CraftingStatusContainer> TYPE;

    private static final ContainerHelper<CraftingStatusContainer, ITerminalHost> helper = new ContainerHelper<>(
            CraftingStatusContainer::new, ITerminalHost.class, SecurityPermissions.CRAFT);

    public static CraftingStatusContainer fromNetwork(int windowId, PlayerInventory inv, PacketByteBuf buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final List<CraftingCPURecord> cpus = new ArrayList<>();
    @GuiSync(5)
    public int selectedCpu = -1;
    @GuiSync(6)
    public boolean noCPU = true;
    @GuiSync(7)
    public Text myName;

    public CraftingStatusContainer(int id, final PlayerInventory ip, final ITerminalHost te) {
        super(TYPE, id, ip, te);
    }

    @Override
    public void sendContentUpdates() {
        if (Platform.isServer() && this.getNetwork() != null) {
            final ICraftingGrid cc = this.getNetwork().getCache(ICraftingGrid.class);
            final ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();

            int matches = 0;
            boolean changed = false;
            for (final ICraftingCPU c : cpuSet) {
                boolean found = false;
                for (final CraftingCPURecord ccr : this.cpus) {
                    if (ccr.getCpu() == c) {
                        found = true;
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

            this.noCPU = this.cpus.isEmpty();
        }

        super.sendContentUpdates();
    }

    private boolean cpuMatches(final ICraftingCPU c) {
        return c.isBusy();
    }

    private void sendCPUs() {
        Collections.sort(this.cpus);

        if (this.selectedCpu >= this.cpus.size()) {
            this.selectedCpu = -1;
            this.myName = null;
        } else if (this.selectedCpu != -1) {
            this.myName = this.cpus.get(this.selectedCpu).getName();
        }

        if (this.selectedCpu == -1 && this.cpus.size() > 0) {
            this.selectedCpu = 0;
        }

        if (this.selectedCpu != -1) {
            if (this.cpus.get(this.selectedCpu).getCpu() != this.getMonitor()) {
                this.setCPU(this.cpus.get(this.selectedCpu).getCpu());
            }
        } else {
            this.setCPU(null);
        }
    }

    public void cycleCpu(final boolean next) {
        if (next) {
            this.selectedCpu++;
        } else {
            this.selectedCpu--;
        }

        if (this.selectedCpu < -1) {
            this.selectedCpu = this.cpus.size() - 1;
        } else if (this.selectedCpu >= this.cpus.size()) {
            this.selectedCpu = -1;
        }

        if (this.selectedCpu == -1 && this.cpus.size() > 0) {
            this.selectedCpu = 0;
        }

        if (this.selectedCpu == -1) {
            this.myName = null;
            this.setCPU(null);
        } else {
            this.myName = this.cpus.get(this.selectedCpu).getName();
            this.setCPU(this.cpus.get(this.selectedCpu).getCpu());
        }
    }
}
