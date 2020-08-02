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

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class OptionalFakeSlot extends FakeSlot implements IOptionalSlot {

    private final int srcX;
    private final int srcY;
    private final int groupNum;
    private final IOptionalSlotHost host;
    private boolean renderDisabled = true;

    public OptionalFakeSlot(final IItemHandler inv, final IOptionalSlotHost containerBus, final int invSlot,
            final int x, final int y, final int offX, final int offY, final int groupNum) {
        super(inv, invSlot, x + offX * 18, y + offY * 18);
        this.srcX = x;
        this.srcY = y;
        this.groupNum = groupNum;
        this.host = containerBus;
    }

    @Override
    @Nonnull
    public ItemStack getStack() {
        if (!this.isSlotEnabled()) {
            if (!this.getDisplayStack().isEmpty()) {
                this.clearStack();
            }
        }

        return super.getStack();
    }

    @Override
    public boolean isSlotEnabled() {
        if (this.host == null) {
            return false;
        }

        return this.host.isSlotEnabled(this.groupNum);
    }

    @Override
    public boolean isRenderDisabled() {
        return this.renderDisabled;
    }

    public void setRenderDisabled(final boolean renderDisabled) {
        this.renderDisabled = renderDisabled;
    }

    @Override
    public int getSourceX() {
        return this.srcX;
    }

    @Override
    public int getSourceY() {
        return this.srcY;
    }
}
