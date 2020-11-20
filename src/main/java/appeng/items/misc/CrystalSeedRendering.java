package appeng.items.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.item.Item;

import appeng.bootstrap.components.IClientSetupComponent;
import appeng.core.AppEng;

/**
 * Exposes a predicate "growth", which is used in the item model to differentiate the growth stages.
 */
public class CrystalSeedRendering implements IClientSetupComponent {
    private final Item item;

    public CrystalSeedRendering(Item item) {
        this.item = item;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void setup() {
        // Expose the growth of the seed to the model system
        FabricModelPredicateProviderRegistry.register(item, AppEng.makeId("growth"),
                (is, w, p) -> CrystalSeedItem.getGrowthTicks(is) / (float) CrystalSeedItem.GROWTH_TICKS_REQUIRED);
    }
}
