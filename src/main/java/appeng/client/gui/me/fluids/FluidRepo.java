/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.client.gui.me.fluids;

import java.util.Comparator;
import java.util.regex.Pattern;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.storage.data.AEFluidKey;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.util.Platform;

class FluidRepo extends Repo<AEFluidKey> {

    public FluidRepo(IScrollSource src, ISortSource sortSrc) {
        super(src, sortSrc);
    }

    @Override
    protected boolean matchesSearch(SearchMode searchMode, Pattern searchPattern, AEFluidKey what) {
        if (searchMode == SearchMode.MOD) {
            return searchPattern.matcher(what.getModId()).find();
        }

        String displayName = Platform.getFluidDisplayName(what).getString();
        return searchPattern.matcher(displayName).find();
    }

    @Override
    protected Comparator<? super AEFluidKey> getKeyComparator(SortOrder sortBy, SortDir sortDir) {
        return FluidSorters.getComparator(sortBy, sortDir);
    }

}
