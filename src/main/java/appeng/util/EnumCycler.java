package appeng.util;

import java.util.EnumSet;

/**
 * Simple utility class to help with select the "next" or "previous" value in a list of options represented by an
 * enumeration.
 */
public final class EnumCycler {

    private EnumCycler() {
    }

    public static <T extends Enum<T>> T rotateEnum(T ce, final boolean backwards, final EnumSet<T> validOptions) {
        do {
            if (backwards) {
                ce = prevEnum(ce);
            } else {
                ce = next(ce);
            }
        } while (!validOptions.contains(ce));

        return ce;
    }

    /*
     * Simple way to cycle an enum...
     */
    public static <T extends Enum<T>> T prevEnum(final T ce) {
        T[] values = ce.getDeclaringClass().getEnumConstants();

        int pLoc = ce.ordinal() - 1;
        if (pLoc < 0) {
            pLoc = values.length - 1;
        }

        if (pLoc < 0 || pLoc >= values.length) {
            pLoc = 0;
        }

        return values[pLoc];
    }

    /*
     * Simple way to cycle an enum...
     */
    public static <T extends Enum<T>> T next(final T ce) {
        T[] values = ce.getDeclaringClass().getEnumConstants();

        int pLoc = ce.ordinal() + 1;
        if (pLoc >= values.length) {
            pLoc = 0;
        }

        if (pLoc < 0 || pLoc >= values.length) {
            pLoc = 0;
        }

        return values[pLoc];
    }

}
