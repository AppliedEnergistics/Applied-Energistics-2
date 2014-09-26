package invtweaks.api.container;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker for containers that need special treatment, such as crafting inputs or alternate player inventory positions,
 * but do not have a chest-like component.
 * <p/>
 * Does not enable the Inventory Tweaks sorting buttons for this container.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InventoryContainer {
    // Set to true if the Inventory Tweaks options button should be shown for this container.
    // (For instance, if you are replacing a vanilla container such as the player's inventory)
    boolean showOptions() default true;
}
