package appeng.server.testplots;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestPlot {
    String value();

    boolean gameTest() default true;

    int maxTicks() default DEFAULT_MAX_TICKS;

    /**
     * Uses 20 ticks for network boot and 1 tick for further setup.
     */
    int setupTicks() default DEFAULT_SETUP_TICKS;

    boolean skyAccess() default DEFAULT_SKY_ACCESS;

    int padding() default DEFAULT_PADDING;

    int DEFAULT_MAX_TICKS = 150;

    int DEFAULT_SETUP_TICKS = 21;

    boolean DEFAULT_SKY_ACCESS = false;

    int DEFAULT_PADDING = 0;
}
