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


import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.util.iterators.ChainedIterator;
import appeng.util.iterators.ProxyNodeIterator;
import net.minecraft.item.ItemStack;

import java.util.Iterator;


public class AENetworkProxyMultiblock extends AENetworkProxy implements IGridMultiblock {

    public AENetworkProxyMultiblock(final IGridProxyable te, final String nbtName, final ItemStack itemStack, final boolean inWorld) {
        super(te, nbtName, itemStack, inWorld);
    }

    @Override
    public Iterator<IGridNode> getMultiblockNodes() {
        if (this.getCluster() == null) {
            return new ChainedIterator<>();
        }

        return new ProxyNodeIterator(this.getCluster().getTiles());
    }

    private IAECluster getCluster() {
        return ((IAEMultiBlock) this.getMachine()).getCluster();
    }
}
