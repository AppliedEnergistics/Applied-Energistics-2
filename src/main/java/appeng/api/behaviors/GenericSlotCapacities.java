package appeng.api.behaviors;

import java.util.Map;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.Container;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKeyType;
import appeng.util.CowMap;

/**
 * Allows custom key types to define slot capacities for pattern providers and interfaces.
 */
@ApiStatus.Experimental
public class GenericSlotCapacities {
    private static final CowMap<AEKeyType, Long> map = CowMap.identityHashMap();

    static {
        register(AEKeyType.items(), (long) Container.LARGE_MAX_STACK_SIZE);
        register(AEKeyType.fluids(), 4L * AEFluidKey.AMOUNT_BUCKET);
    }

    public static void register(AEKeyType type, Long capacity) {
        Preconditions.checkArgument(capacity >= 0, "capacity >= 0");
        map.putIfAbsent(type, capacity);
    }

    public static Map<AEKeyType, Long> getMap() {
        return map.getMap();
    }

    private GenericSlotCapacities() {
    }
}
