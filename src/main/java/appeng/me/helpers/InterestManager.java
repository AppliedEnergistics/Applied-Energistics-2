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

package appeng.me.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Multimap;

import appeng.api.storage.data.AEKey;

public class InterestManager<T> {

    private final Multimap<AEKey, T> container;
    private List<SavedTransactions> transactions = null;
    private int transDepth = 0;

    public InterestManager(Multimap<AEKey, T> interests) {
        this.container = interests;
    }

    public void enableTransactions() {
        if (this.transDepth == 0) {
            this.transactions = new ArrayList<>();
        }

        this.transDepth++;
    }

    public void disableTransactions() {
        this.transDepth--;

        if (this.transDepth == 0) {
            final List<SavedTransactions> myActions = this.transactions;
            this.transactions = null;

            for (final SavedTransactions t : myActions) {
                if (t.put) {
                    this.put(t.stack, t.iw);
                } else {
                    this.remove(t.stack, t.iw);
                }
            }
        }
    }

    public boolean put(final AEKey stack, final T iw) {
        if (this.transactions != null) {
            this.transactions.add(new SavedTransactions(true, stack, iw));
            return true;
        } else {
            return this.container.put(stack, iw);
        }
    }

    public boolean remove(final AEKey stack, final T iw) {
        if (this.transactions != null) {
            this.transactions.add(new SavedTransactions(false, stack, iw));
            return true;
        } else {
            return this.container.remove(stack, iw);
        }
    }

    public boolean containsKey(final AEKey stack) {
        return this.container.containsKey(stack);
    }

    public Collection<T> get(final AEKey stack) {
        return this.container.get(stack);
    }

    private class SavedTransactions {

        private final boolean put;
        private final AEKey stack;
        private final T iw;

        public SavedTransactions(final boolean putOperation, final AEKey myStack, final T watcher) {
            this.put = putOperation;
            this.stack = myStack;
            this.iw = watcher;
        }
    }
}
