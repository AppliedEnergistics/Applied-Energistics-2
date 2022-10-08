package appeng.hooks;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.Level;

import appeng.util.Platform;

/**
 * Under certain circumstances we want to save the state that the client received from the server in block entity NBT
 * data. This is the case when the block entity is being rendered in a client-only fake world (such as Creates Ponder
 * worlds).
 * <p>
 * When these worlds are used, the server-side logic will never run, and the state would otherwise not be computed. This
 * mostly affects anything grid related (cable connections, power/channel states).
 *
 * @see appeng.mixins.StructureTemplateMixin
 */
public final class VisualStateSaving {

    private static final ThreadLocal<Boolean> SAVE_CLIENT_SIDE_STATE = new ThreadLocal<>();

    private VisualStateSaving() {
    }

    public static void setEnabled(boolean enabled) {
        SAVE_CLIENT_SIDE_STATE.set(enabled);
    }

    public static boolean isEnabled(@Nullable Level level) {
        return Boolean.TRUE.equals(SAVE_CLIENT_SIDE_STATE.get())
                || level != null && Platform.isPonderLevel(level);
    }

}
