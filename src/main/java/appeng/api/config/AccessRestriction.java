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

public enum AccessRestriction {
    NO_ACCESS(0), READ(1), WRITE(2), READ_WRITE(3);

    private final int permissionBit;

    AccessRestriction(final int v) {
        this.permissionBit = v;
    }

    public boolean hasPermission(final AccessRestriction ar) {
        return (this.permissionBit & ar.permissionBit) == ar.permissionBit;
    }

    public AccessRestriction restrictPermissions(final AccessRestriction ar) {
        return this.getPermByBit(this.permissionBit & ar.permissionBit);
    }

    private AccessRestriction getPermByBit(final int bit) {
        return switch (bit) {
            case 0 -> NO_ACCESS;
            case 1 -> READ;
            case 2 -> WRITE;
            case 3 -> READ_WRITE;
            default -> NO_ACCESS;
        };
    }

    public AccessRestriction addPermissions(final AccessRestriction ar) {
        return this.getPermByBit(this.permissionBit | ar.permissionBit);
    }

    public AccessRestriction removePermissions(final AccessRestriction ar) {
        return this.getPermByBit(this.permissionBit & ~ar.permissionBit);
    }
}
