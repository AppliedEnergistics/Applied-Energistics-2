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

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.stacks.AEKey;

public class CraftingLink implements ICraftingLink {

    private final ICraftingRequester req;
    private final ICraftingCPU cpu;
    private final UUID craftId;
    private final boolean standalone;
    private boolean canceled = false;
    private boolean done = false;
    private CraftingLinkNexus tie;

    public CraftingLink(ValueInput data, ICraftingRequester req) {
        this.craftId = data.read("craftId", UUIDUtil.CODEC).orElseThrow();
        this.setCanceled(data.getBooleanOr("canceled", false));
        this.setDone(data.getBooleanOr("done", false));
        this.standalone = data.getBooleanOr("standalone", false);

        if (!data.getBooleanOr("req", false)) {
            throw new IllegalStateException("Invalid Crafting Link for Object");
        }

        this.req = req;
        this.cpu = null;
    }

    public CraftingLink(UUID craftId, boolean standalone, ICraftingRequester requester) {
        this.craftId = craftId;
        this.standalone = standalone;
        this.req = requester;
        this.cpu = null;
    }

    public CraftingLink(ValueInput data, ICraftingCPU cpu) {
        this.craftId = data.read("craftId", UUIDUtil.CODEC).orElseThrow();
        this.setCanceled(data.getBooleanOr("canceled", false));
        this.setDone(data.getBooleanOr("done", false));
        this.standalone = data.getBooleanOr("standalone", false);

        if (data.getBooleanOr("req", false)) {
            throw new IllegalStateException("Invalid Crafting Link for Object");
        }

        this.cpu = cpu;
        this.req = null;
    }

    public CraftingLink(UUID craftId, boolean standalone, ICraftingCPU cpu) {
        this.craftId = craftId;
        this.standalone = standalone;
        this.req = null;
        this.cpu = cpu;
    }

    @Override
    public boolean isCanceled() {
        if (this.canceled) {
            return true;
        }

        if (this.done) {
            return false;
        }

        if (this.tie == null) {
            return false;
        }

        return this.tie.isCanceled();
    }

    @Override
    public boolean isDone() {
        if (this.done) {
            return true;
        }

        if (this.canceled) {
            return false;
        }

        if (this.tie == null) {
            return false;
        }

        return this.tie.isDone();
    }

    @Override
    public void cancel() {
        if (this.done) {
            return;
        }

        this.setCanceled(true);

        if (this.tie != null) {
            this.tie.cancel();
        }

        this.tie = null;
    }

    @Override
    public boolean isStandalone() {
        return this.standalone;
    }

    @Override
    public void writeToNBT(ValueOutput output) {
        output.store("craftId", UUIDUtil.CODEC, this.craftId);
        output.putBoolean("canceled", this.isCanceled());
        output.putBoolean("done", this.isDone());
        output.putBoolean("standalone", this.standalone);
        output.putBoolean("req", this.getRequester() != null);
    }

    @Override
    public UUID getCraftingID() {
        return this.craftId;
    }

    public void setNexus(CraftingLinkNexus n) {
        if (this.tie != null) {
            this.tie.remove(this);
        }

        if (this.isCanceled() && n != null) {
            n.cancel();
            this.tie = null;
            return;
        }

        this.tie = n;

        if (n != null) {
            n.add(this);
        }
    }

    public long insert(AEKey what, long amount, Actionable mode) {
        if (this.tie == null || this.tie.getRequest() == null || this.tie.getRequest().getRequester() == null) {
            return 0;
        }

        // The job was canceled. Don't insert more items. See issue #5919
        if (tie.isCanceled()) {
            return 0;
        }

        return this.tie.getRequest().getRequester().insertCraftedItems(this.tie.getRequest(), what, amount, mode);
    }

    public void markDone() {
        if (this.tie != null) {
            this.tie.markDone();
        }
    }

    void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    ICraftingRequester getRequester() {
        return this.req;
    }

    ICraftingCPU getCpu() {
        return this.cpu;
    }

    void setDone(boolean done) {
        this.done = done;
    }
}
