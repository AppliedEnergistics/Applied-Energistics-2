package invtweaks.api.container;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker for containers that have a chest-like persistant storage component. Enables the Inventroy Tweaks sorting
 * buttons for this container.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ChestContainer {
    // Size of a chest row
    int rowSize() default 9;

    // Uses 'large chest' mode for sorting buttons
    // (Renders buttons vertically down the right side of the GUI)
    boolean isLargeChest() default false;

    // Annotation for method to get size of a chest row if it is not a fixed size for this container class
    // Signature int func()
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface RowSizeCallback {
    }
}
