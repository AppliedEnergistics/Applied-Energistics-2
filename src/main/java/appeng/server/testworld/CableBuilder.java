package appeng.server.testworld;

import java.util.function.Consumer;

import net.minecraft.core.Direction;

import appeng.api.parts.IPart;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;

public class CableBuilder {
    private final PlotBuilder plotBuilder;
    private final String bb;

    public CableBuilder(PlotBuilder plotBuilder, String bb) {
        this.plotBuilder = plotBuilder;
        this.bb = bb;
    }

    public void part(Direction side, ItemDefinition<? extends PartItem<?>> part) {
        plotBuilder.part(bb, side, part);
    }

    public <T extends IPart> void part(Direction side,
            ItemDefinition<? extends PartItem<T>> part,
            Consumer<T> partCustomizer) {
        plotBuilder.part(bb, side, part, partCustomizer);
    }
}
