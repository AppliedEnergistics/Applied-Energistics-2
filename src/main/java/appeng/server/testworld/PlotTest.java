package appeng.server.testworld;

import net.minecraft.gametest.framework.GameTestHelper;

public record PlotTest(String name, PlotTestAssertions assertions) {
    @FunctionalInterface
    public interface PlotTestAssertions {
        void runAssertions(GameTestHelper helper);
    }
}
