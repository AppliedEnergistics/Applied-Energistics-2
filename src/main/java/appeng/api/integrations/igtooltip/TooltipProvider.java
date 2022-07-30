/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 TeamAppliedEnergistics
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

package appeng.api.integrations.igtooltip;

import org.jetbrains.annotations.ApiStatus;

/**
 * Implement this in your addon to register additional block entity tooltips using AE2's abstraction over
 * Jade/WTHIT/TOP.
 * <p/>
 * AE2 uses the Java Service Loader mechanism to find your implementations.
 * <p/>
 * In Short: Name a text-file <code>META-INF/services/appeng.api.integrations.igtooltip.TooltipProvider</code> and place
 * a line in it that has the fully qualified name of your implementation class.
 */
@ApiStatus.Experimental
@ApiStatus.OverrideOnly
public interface TooltipProvider {
    int DEFAULT_PRIORITY = 1000;
    int DEBUG_PRIORITY = 5000;

    /**
     * Called on both dedicated servers and clients to register providers for server-data.
     */
    default void registerCommon(CommonRegistration registration) {
    }

    /**
     * Called on clients to register providers that supply tooltip data.
     */
    default void registerClient(ClientRegistration registration) {
    }

    /**
     * Allows an addon to register additional block entity base-classes that benefit from default AE2 tooltip providers.
     */
    default void registerBlockEntityBaseClasses(BaseClassRegistration registration) {
    }
}
