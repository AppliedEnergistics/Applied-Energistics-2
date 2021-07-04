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

package appeng.crafting;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeHost;
import appeng.me.cache.CraftingGridCache;

public class CraftingLinkNexus {

    private final String craftID;
    private boolean canceled = false;
    private boolean done = false;
    private int tickOfDeath = 0;
    private CraftingLink req;
    private CraftingLink cpu;

    public CraftingLinkNexus(final String craftID) {
        this.craftID = craftID;
    }

    public boolean isDead(final IGrid g, final CraftingGridCache craftingGridCache) {
        if (this.canceled || this.done) {
            return true;
        }

        if (this.getRequest() == null || this.cpu == null) {
            this.tickOfDeath++;
        } else {
            final boolean hasCpu = craftingGridCache.hasCpu(this.cpu.getCpu());
            final boolean hasMachine = this.getRequest().getRequester().getActionableNode().getGrid() == g;

            if (hasCpu && hasMachine) {
                this.tickOfDeath = 0;
            } else {
                this.tickOfDeath += 60;
            }
        }

        if (this.tickOfDeath > 60) {
            this.cancel();
            return true;
        }

        return false;
    }

    void cancel() {
        this.canceled = true;

        if (this.getRequest() != null) {
            this.getRequest().setCanceled(true);
            if (this.getRequest().getRequester() != null) {
                this.getRequest().getRequester().jobStateChange(this.getRequest());
            }
        }

        if (this.cpu != null) {
            this.cpu.setCanceled(true);
        }
    }

    void remove(final CraftingLink craftingLink) {
        if (this.getRequest() == craftingLink) {
            this.setRequest(null);
        } else if (this.cpu == craftingLink) {
            this.cpu = null;
        }
    }

    void add(final CraftingLink craftingLink) {
        if (craftingLink.getCpu() != null) {
            this.cpu = craftingLink;
        } else if (craftingLink.getRequester() != null) {
            this.setRequest(craftingLink);
        }
    }

    boolean isCanceled() {
        return this.canceled;
    }

    boolean isDone() {
        return this.done;
    }

    void markDone() {
        this.done = true;

        if (this.getRequest() != null) {
            this.getRequest().setDone(true);
            if (this.getRequest().getRequester() != null) {
                this.getRequest().getRequester().jobStateChange(this.getRequest());
            }
        }

        if (this.cpu != null) {
            this.cpu.setDone(true);
        }
    }

    public boolean isMachine(final IGridNodeHost machine) {
        return this.getRequest() == machine;
    }

    public void removeNode() {
        if (this.getRequest() != null) {
            this.getRequest().setNexus(null);
        }

        this.setRequest(null);
        this.tickOfDeath = 0;
    }

    public CraftingLink getRequest() {
        return this.req;
    }

    public void setRequest(final CraftingLink req) {
        this.req = req;
    }
}
