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

package appeng.container.interfaces;

import appeng.client.gui.widgets.ProgressBar;

/**
 * This interface provides the data for anything simulating a progress.
 * <p>
 * Its main use is in combination with the {@link ProgressBar}, which ensures to scale it to a percentage of 0 to 100.
 */
public interface IProgressProvider {

    /**
     * The current value of the progress. It should cover a range from 0 to the max progress
     *
     * @return An int representing the current progress
     */
    int getCurrentProgress();

    /**
     * The max value the progress.
     * <p>
     * It is not limited to a value of 100 and can be scaled to fit the current needs. For example scaled down to
     * decrease or scaled up to increase the precision.
     *
     * @return An int representing the max progress
     */
    int getMaxProgress();
}
