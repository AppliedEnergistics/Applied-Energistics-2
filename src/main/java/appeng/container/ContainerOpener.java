package appeng.container;

import appeng.core.AELog;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows opening containers generically.
 */
public final class ContainerOpener {

    private ContainerOpener() {
    }

    private static final Map<ContainerType<? extends AEBaseContainer>, Opener<?>> registry = new HashMap<>();

    public static <T extends AEBaseContainer> void addOpener(ContainerType<T> type, Opener<T> opener) {
        registry.put(type, opener);
    }

    public static boolean openContainer(ContainerType<?> type, PlayerEntity player, ContainerLocator locator) {
        Opener<?> opener = registry.get(type);
        if (opener == null) {
            AELog.warn("Trying to open container for unknown container type {}", type);
            return false;
        }

        return opener.open(player, locator);
    }

    @FunctionalInterface
    public interface Opener<T extends AEBaseContainer> {

        boolean open(PlayerEntity player, ContainerLocator locator);

    }

}
