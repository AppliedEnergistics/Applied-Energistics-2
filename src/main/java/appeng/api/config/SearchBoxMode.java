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

public enum SearchBoxMode {
    DEFAULT(false, false, false),
    REMEMBER_SEARCH(true, false, false),
    AUTO_FOCUS(false, true, false),
    AUTO_FOCUS_AND_REMEMBER_SEARCH(true, true, false),
    JEI(false, false, true),
    JEI_AUTO_CLEAR(false, false, true),
    REI(false, false, true),
    REI_AUTO_CLEAR(false, false, true);

    private final boolean rememberSearch;

    private final boolean autoFocus;

    private final boolean useExternalSearchBox;

    SearchBoxMode(boolean rememberSearch, boolean autoFocus, boolean useExternalSearchBox) {
        this.rememberSearch = rememberSearch;
        this.autoFocus = autoFocus;
        this.useExternalSearchBox = useExternalSearchBox;
    }

    public boolean shouldUseExternalSearchBox() {
        return useExternalSearchBox;
    }

    public boolean isRememberSearch() {
        return rememberSearch;
    }

    public boolean isAutoFocus() {
        return autoFocus;
    }
}
