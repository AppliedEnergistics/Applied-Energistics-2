package appeng.server.testplots;

import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;

import appeng.core.AppEng;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;

public class TestPlotCollection {
    private final Map<ResourceLocation, Consumer<PlotBuilder>> plots;

    public TestPlotCollection(Map<ResourceLocation, Consumer<PlotBuilder>> plots) {
        this.plots = plots;
    }

    public void add(String id, Consumer<PlotBuilder> builder) {
        add(AppEng.makeId(id), builder);
    }

    public void add(String id, Consumer<PlotBuilder> builder, Consumer<PlotTestHelper> test) {
        add(AppEng.makeId(id), builder, test);
    }

    public void add(ResourceLocation id, Consumer<PlotBuilder> builder) {
        plots.put(id, builder);
    }

    public void add(ResourceLocation id, Consumer<PlotBuilder> builder, Consumer<PlotTestHelper> test) {
        plots.put(id, actualBuilder -> {
            builder.accept(actualBuilder);
            actualBuilder.test(test);
        });
    }
}
