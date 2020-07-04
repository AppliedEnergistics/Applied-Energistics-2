package appeng.container;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandlerType;

import appeng.core.AELog;

/**
 * Allows opening containers generically.
 */
public final class ContainerOpener {

    private ContainerOpener() {
    }

    private static final Map<ScreenHandlerType<? extends AEBaseContainer>, Opener<?>> registry = new HashMap<>();

    public static <T extends AEBaseContainer> void addOpener(ScreenHandlerType<T> type, Opener<T> opener) {
        registry.put(type, opener);
    }

    public static boolean openContainer(ScreenHandlerType<?> type, PlayerEntity player, ContainerLocator locator) {
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
