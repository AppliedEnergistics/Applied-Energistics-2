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

package appeng.api.networking.ticking;

import appeng.core.settings.TickRates;

/**
 * Describes how your grid node ticking is executed.
 *
 * @param minTickRate the minimum number of ticks that must pass between ticks. Valid Values are : 1+ Suggested is 5-20
 * @param maxTickRate the maximum number of ticks that can pass between ticks, if this value is exceeded the grid node
 *                    must tick. Valid Values are 1+ Suggested is 20-40
 * @param isSleeping  Determines the current expected state of your node, if your node expects to be sleeping, then
 *                    return true.
 */
public record TickingRequest(
        int minTickRate,
        int maxTickRate,
        boolean isSleeping,
        int initialTickRate) {

    public TickingRequest(int minTickRate, int maxTickRate, boolean isSleeping) {
        this(
                minTickRate,
                maxTickRate,
                isSleeping,
                getInitialTickDelay(minTickRate, maxTickRate));
    }

    public TickingRequest(TickRates tickRates, boolean isSleeping) {
        this(
                tickRates.getMin(),
                tickRates.getMax(),
                isSleeping);
    }

    private static int getInitialTickDelay(int min, int max) {
        return (min + max) / 2;
    }
}
