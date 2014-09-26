package invtweaks.api.container;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker for a method to call which returns the set of ContainerSections for this container.
 * <p/>
 * Signature of the method should be Map<ContainerSection, List<Slot>> func()
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ContainerSectionCallback {
}
