package appeng.server.testplots;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotate methods of the form: {@code public static void testPlot(TestPlotCollection plots)}
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TestPlotGenerator {
}
