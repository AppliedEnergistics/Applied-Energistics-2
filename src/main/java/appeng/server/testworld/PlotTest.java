package appeng.server.testworld;

import java.util.function.Consumer;

public record PlotTest(String name, Consumer<PlotTestHelper> assertions) {
}
