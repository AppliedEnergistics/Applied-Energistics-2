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

package appeng.api.networking.pathing;

import appeng.api.networking.IGridService;

/**
 * Provides services related to channel-allocation and a grid's controller.
 * <p/>
 * Pathing / Path-finding refers to finding a path from a node to the grid's controller for the purposes of allocating
 * channels.
 */
public interface IPathingService extends IGridService {

    /**
     * When the structure of a grid is changed in any way, the grid will reboot. While it is booting, the path from
     * every grid node to the controller, and the number of channels will be re-calculated.
     * <p/>
     * The start and end of the boot process is signaled by the
     * {@link appeng.api.networking.events.GridBootingStatusChange} event.
     *
     * @return true if the network is in its booting stage
     */
    boolean isNetworkBooting();

    /**
     * @return the controller state of the network, useful if you want to require a controller for a feature.
     */
    ControllerState getControllerState();

    /**
     * trigger a network reset, booting, path-finding and all.
     */
    void repath();

    /**
     * @return The current mode used for channel calculations.
     */
    ChannelMode getChannelMode();

    /**
     * @return The total number of channels currently used by this network.
     */
    int getUsedChannels();
}
