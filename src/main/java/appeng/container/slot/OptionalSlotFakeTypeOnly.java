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

package appeng.container.slot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class OptionalSlotFakeTypeOnly extends OptionalSlotFake {

    public OptionalSlotFakeTypeOnly(final IItemHandler inv, final IOptionalSlotHost containerBus, final int idx,
            final int x, final int y, final int offX, final int offY, final int groupNum) {
        super(inv, containerBus, idx, x, y, offX, offY, groupNum);
    }

    @Override
    public void putStack(ItemStack is) {
        if (!is.isEmpty()) {
            is = is.copy();
            if (is.getCount() > 1) {
                is.setCount(1);
            } else if (is.getCount() < -1) {
                is.setCount(-1);
            }
        }

        super.putStack(is);
    }
}
