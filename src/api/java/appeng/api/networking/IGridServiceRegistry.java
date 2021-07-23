/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
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

package appeng.api.networking;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A registry of grid services to extend grid functionality.
 */
@ThreadSafe
public interface IGridServiceRegistry {

    /**
     * Register a new grid service for use during operation, must be called during the loading phase.
     * <p/>
     * AE will automatically construct instances of the given implementation class by looking up a constructor. There
     * must be a single constructor.
     * <p/>
     * The following constructor parameter types are allowed:
     * <ul>
     * <li>Other grid services public interfaces (see interfaces extending {@link IGridService}).</li>
     * <li>{@link IGrid}, which will be the grid that the service is being constructed for.</li>
     * </ul>
     *
     * @param publicInterface The public facing interface of the grid service you want to register. This class or
     *                        interface will also be used to query the service from any grid via
     *                        {@link IGrid#getService(Class)}.
     * @param implClass       The class used to construct the grid service for each grid. Must have a single
     *                        constructor.
     */
    <T extends IGridServiceProvider> void register(Class<? super T> publicInterface, Class<T> implClass);

}
