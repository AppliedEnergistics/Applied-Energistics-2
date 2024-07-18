/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.me.energy;

import java.util.Collection;

import org.jetbrains.annotations.ApiStatus;

import appeng.api.networking.IGridNodeService;
import appeng.api.networking.energy.IEnergyService;
import appeng.me.service.EnergyService;

/**
 * internal use only.
 * <p/>
 * This interface is implemented by nodes that provides connections to {@link EnergyService} that are connected via the
 * {@link appeng.me.service.EnergyOverlayGrid}.
 */
@FunctionalInterface
@ApiStatus.Internal
public interface IEnergyOverlayGridConnection extends IGridNodeService {
    /**
     * internal use only
     * <p>
     * Can return a list of providers behind the current.
     * <p>
     * An example would be something acting as proxy between different {@link IEnergyService}s.
     * <p>
     * This can contain duplicate entries, AE will ensure that each provider is only visited once.
     * <p>
     * Call {@link EnergyService#invalidateOverlayEnergyGrid()} if this changes.
     * <p>
     * internal use only
     */
    Collection<EnergyService> connectedEnergyServices();
}
