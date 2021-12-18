package appeng.server.testworld;

import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.world.level.levelgen.structure.BoundingBox;

class TransformingPlotBuilder implements PlotBuilder {
    private final Plot plot;
    private final Function<BoundingBox, BoundingBox> transform;

    TransformingPlotBuilder(Plot plot, Function<BoundingBox, BoundingBox> transform) {
        this.plot = plot;
        this.transform = transform;
    }

    @Override
    public void addBuildAction(BuildAction action) {
        plot.addBuildAction(action);
    }

    @Override
    public BoundingBox bb(String def) {
        return transform.apply(plot.bb(def));
    }

    @Override
    public PlotBuilder transform(Function<BoundingBox, BoundingBox> transform) {
        return new TransformingPlotBuilder(this.plot, this.transform.andThen(transform));
    }

    @Override
    public Test test(Consumer<PlotTestHelper> assertion) {
        return plot.test(assertion);
    }
}
