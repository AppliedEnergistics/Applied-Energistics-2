package appeng.server.testplots;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

import appeng.core.AppEng;
import appeng.server.testworld.Plot;
import appeng.server.testworld.PlotBuilder;
import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
class EmiTestPlotsTest {

    @Test
    void plotsCanBeBuilt() {
        var plots = new HashMap<ResourceLocation, Consumer<PlotBuilder>>();
        EmiTestPlots.generateNetworkInventoryPlots(new TestPlotCollection(plots));

        assertThat(plots).containsKey(AppEng.makeId("emi_network_inventory_all_items"));

        for (var entry : plots.entrySet()) {
            // Building the plot also populates the storage cells, which throws if a key doesn't fit
            var plot = new Plot(entry.getKey());
            entry.getValue().accept(plot);
        }
    }
}
