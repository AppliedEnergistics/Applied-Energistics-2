package appeng.server.testplots;

import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.resources.Identifier;

import appeng.core.AppEng;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;

public class TestPlotCollection {
    private final Map<Identifier, TestPlots.PlotInfo> plots;

    public TestPlotCollection(Map<Identifier, TestPlots.PlotInfo> plots) {
        this.plots = plots;
    }

    public void add(String id, Consumer<PlotBuilder> builder) {
        add(AppEng.makeId(id), builder);
    }

    public void add(String id, Consumer<PlotBuilder> builder, Consumer<PlotTestHelper> test) {
        add(AppEng.makeId(id), builder, test);
    }

    public void add(Identifier id, Consumer<PlotBuilder> builder) {
        add(id, new TestPlots.PlotInfo(id, builder, new TestPlots.PlotTestInfo()));
    }

    public void add(Identifier id, TestPlots.PlotInfo info) {
        plots.put(id, info);
    }

    public void add(Identifier id, Consumer<PlotBuilder> builder, Consumer<PlotTestHelper> test) {
        add(id, actualBuilder -> {
            builder.accept(actualBuilder);
            actualBuilder.test(test);
        });
    }
}
