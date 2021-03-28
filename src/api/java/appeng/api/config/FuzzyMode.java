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

package appeng.api.config;

public enum FuzzyMode {
    /**
     * Matches between 0% and 100% durability.
     */
    IGNORE_ALL(-1),
    /**
     * Matches items that have less than 100% durability (that is, at least 1 damage point) if a damaged item is used as
     * the filter, or undamaged items otherwise.
     */
    PERCENT_99(0),
    /**
     * If an item with less than 75% durability is used as the filter, items with less than 75% durability are matched.
     * Otherwise items with 75% durability or more are matched.
     */
    PERCENT_75(25),
    /**
     * If an item with less than 50% durability is used as the filter, items with less than 50% durability are matched.
     * Otherwise items with 50% durability or more are matched.
     */
    PERCENT_50(50),
    /**
     * If an item with less than 25% durability is used as the filter, items with less than 50% durability are matched.
     * Otherwise items with 25% durability or more are matched.
     */
    PERCENT_25(75);

    // Note that percentage damaged, is the inverse of percentage durability.
    public final float breakPoint;
    public final float percentage;

    FuzzyMode(final float p) {
        this.percentage = p;
        this.breakPoint = p / 100.0f;
    }

    public int calculateBreakPoint(final int maxDamage) {
        return (int) ((this.percentage * maxDamage) / 100.0f);
    }

}