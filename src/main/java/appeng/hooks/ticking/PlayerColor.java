/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.hooks.ticking;

import appeng.api.util.AEColor;
import appeng.core.sync.packets.PaintedEntityPacket;

/**
 * Handles how long the color overlay for a player is valid
 */
public class PlayerColor {

    private final AEColor myColor;
    private final int myEntity;
    private int ticksLeft;

    public PlayerColor(final int id, final AEColor col, final int ticks) {
        this.myEntity = id;
        this.myColor = col;
        this.ticksLeft = ticks;
    }

    public PaintedEntityPacket getPacket() {
        return new PaintedEntityPacket(this.myEntity, this.myColor, this.ticksLeft);
    }

    public AEColor getColor() {
        return myColor;
    }

    /**
     * Tick this player color once.
     */
    void tick() {
        this.ticksLeft--;
    }

    /**
     * Indicates that this color is done and can be removed.
     *
     * @return true once done.
     */
    boolean isDone() {
        return this.ticksLeft <= 0;
    }

}