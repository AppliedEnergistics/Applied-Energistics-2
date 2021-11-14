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

package appeng.blockentity.misc;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;

class CondenserVoidInventory<T extends AEKey> implements IMEMonitor<T> {

    private final CondenserBlockEntity target;
    private final IStorageChannel<T> channel;

    CondenserVoidInventory(final CondenserBlockEntity te, final IStorageChannel<T> channel) {
        this.target = te;
        this.channel = channel;
    }

    @Override
    public long insert(T what, long amount, Actionable mode, IActionSource source) {
        if (mode == Actionable.MODULATE) {
            this.target.addPower(amount / (double) this.channel.transferFactor());
        }
        return amount;
    }

    @Override
    public void getAvailableStacks(KeyCounter<T> out) {
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return this.channel;
    }

    @Override
    public void addListener(IMEMonitorListener<T> l, Object verificationToken) {
        // Not implemented since the Condenser automatically voids everything, and there
        // are no updates
    }

    @Override
    public void removeListener(IMEMonitorListener<T> l) {
        // Not implemented since we don't remember registered listeners anyway
    }
}
