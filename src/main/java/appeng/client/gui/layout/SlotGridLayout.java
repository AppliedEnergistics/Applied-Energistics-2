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

package appeng.client.gui.layout;

import appeng.client.Point;

public enum SlotGridLayout {

    /**
     * This layout is used for the peculiar grid of slots used in
     * {@link appeng.container.implementations.IOBusContainer}.
     */
    IO_BUS_CONFIG {

        /**
         * Slots are laid out around the center slot 0. Slots 1-4 in the cardinal directions. Slots 5-8 in the diagonal
         * directions.
         */
        private final Point[] OFFSETS = new Point[] {
                new Point(0, 0),
                new Point(-18, 0),
                new Point(18, 0),
                new Point(0, -18),
                new Point(0, 18),
                new Point(-18, -18),
                new Point(18, -18),
                new Point(-18, 18),
                new Point(18, 18)
        };

        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return OFFSETS[Math.max(semanticIdx, 0) % OFFSETS.length].move(x, y);
        }

    },

    BREAK_AFTER_9COLS {
        public Point getPosition(int x, int y, int semanticIdx) {
            return SlotGridLayout.getRowBreakPosition(x, y, semanticIdx, 9);
        }
    },

    BREAK_AFTER_3COLS {
        public Point getPosition(int x, int y, int semanticIdx) {
            return SlotGridLayout.getRowBreakPosition(x, y, semanticIdx, 3);
        }
    },

    BREAK_AFTER_2COLS {
        public Point getPosition(int x, int y, int semanticIdx) {
            return SlotGridLayout.getRowBreakPosition(x, y, semanticIdx, 2);
        }
    },

    HORIZONTAL {
        public Point getPosition(int x, int y, int semanticIdx) {
            return new Point(x, y).move(semanticIdx * 18, 0);
        }
    },

    VERTICAL {
        public Point getPosition(int x, int y, int semanticIdx) {
            return new Point(x, y).move(0, semanticIdx * 18);
        }
    };

    private static Point getRowBreakPosition(int x, int y, int semanticIdx, int cols) {
        int row = semanticIdx / cols;
        int col = semanticIdx % cols;
        return new Point(x, y).move(col * 18, row * 18);
    }

    public abstract Point getPosition(int x, int y, int semanticIdx);

}
