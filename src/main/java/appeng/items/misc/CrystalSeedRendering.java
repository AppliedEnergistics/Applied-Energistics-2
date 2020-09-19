package appeng.items.misc;

import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.bootstrap.components.IClientSetupComponent;

/**
 * Exposes a predicate "growth", which is used in the item model to
 * differentiate the growth stages.
 */
public class CrystalSeedRendering implements IClientSetupComponent {
    private final Item item;

    public CrystalSeedRendering(Item item) {
        this.item = item;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setup() {
        // Expose the growth of the seed to the model system
        ItemModelsProperties.registerProperty(item, new ResourceLocation("appliedenergistics2:growth"),
                (is, w, p) -> CrystalSeedItem.getGrowthTicks(is) / (float) CrystalSeedItem.GROWTH_TICKS_REQUIRED);
    }
}
