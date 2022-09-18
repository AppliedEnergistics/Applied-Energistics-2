package appeng.api.stacks;

import java.util.Comparator;

import it.unimi.dsi.fastutil.objects.Object2LongAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

/**
 * Custom extension to expose the increment function in a polymorphic way. We don't want to use
 * {@link Object2LongMap#mergeLong} because the fastutil maps don't override the naive implementation.
 */
interface AEKey2LongMap extends Object2LongMap<AEKey> {
    /**
     * Adds an increment to value currently associated with a key.
     *
     * @return the old value, or the {@linkplain #defaultReturnValue() default return value} if no value was present for
     *         the given key.
     */
    @SuppressWarnings("UnusedReturnValue")
    long addTo(AEKey k, long incr);

    final class OpenHashMap extends Object2LongOpenHashMap<AEKey> implements AEKey2LongMap {
    }

    final class AVLTreeMap extends Object2LongAVLTreeMap<AEKey> implements AEKey2LongMap {
        public AVLTreeMap(Comparator<? super AEKey> c) {
            super(c);
        }
    }
}
