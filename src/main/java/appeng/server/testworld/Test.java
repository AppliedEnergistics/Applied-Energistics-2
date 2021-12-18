package appeng.server.testworld;

import java.util.function.Consumer;

public final class Test {
    private final Consumer<PlotTestHelper> testFunction;

    /**
     * Uses 20 ticks for network boot and 1 tick for further setup.
     */
    public int setupTicks = 21;

    public int maxTicks = 100;

    public Test(Consumer<PlotTestHelper> testFunction) {
        this.testFunction = testFunction;
    }

    public Consumer<PlotTestHelper> getTestFunction() {
        return testFunction;
    }

    public Test setupTicks(int ticks) {
        this.setupTicks = ticks;
        return this;
    }

    public Test maxTicks(int maxTicks) {
        this.maxTicks = maxTicks;
        return this;
    }
}
