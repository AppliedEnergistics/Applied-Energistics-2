package appeng.init.client;

import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.definitions.IItemDefinition;
import appeng.api.util.AEColor;
import appeng.block.AEBaseBlockItemChargeable;
import appeng.core.AppEng;
import appeng.core.api.definitions.ApiItems;
import appeng.items.misc.CrystalSeedItem;
import appeng.items.tools.powered.ColorApplicatorItem;

/**
 * Registers custom properties that can be used in item model JSON files.
 */
@OnlyIn(Dist.CLIENT)
public final class InitItemModelsProperties {

    private InitItemModelsProperties() {
    }

    public static void init() {
        ColorApplicatorItem colorApplicatorItem = (ColorApplicatorItem) ApiItems.colorApplicator().item();
        ItemModelsProperties.registerProperty(colorApplicatorItem, new ResourceLocation(AppEng.MOD_ID, "colored"),
                (itemStack, world, entity) -> {
                    // If the stack has no color, don't use the colored model since the impact of
                    // calling getColor for every quad is extremely high, if the stack tries to
                    // re-search its
                    // inventory for a new paintball everytime
                    AEColor col = colorApplicatorItem.getActiveColor(itemStack);
                    return col != null ? 1 : 0;
                });

        registerSeedGrowth(ApiItems.certusCrystalSeed());
        registerSeedGrowth(ApiItems.fluixCrystalSeed());
        registerSeedGrowth(ApiItems.netherQuartzSeed());

        // Register the client-only item model property for chargeable items
        ForgeRegistries.ITEMS.forEach(item -> {
            if (!(item instanceof AEBaseBlockItemChargeable)) {
                return;
            }

            AEBaseBlockItemChargeable chargeable = (AEBaseBlockItemChargeable) item;
            ItemModelsProperties.registerProperty(chargeable, new ResourceLocation("appliedenergistics2:fill_level"),
                    (is, world, entity) -> {
                        double curPower = chargeable.getAECurrentPower(is);
                        double maxPower = chargeable.getAEMaxPower(is);

                        return (int) Math.round(100 * curPower / maxPower);
                    });
        });
    }

    /**
     * Exposes a predicate "growth", which is used in the item model to differentiate the growth stages.
     */
    private static void registerSeedGrowth(IItemDefinition definition) {
        // Expose the growth of the seed to the model system
        ItemModelsProperties.registerProperty(definition.item(), new ResourceLocation("appliedenergistics2:growth"),
                (is, w, p) -> CrystalSeedItem.getGrowthTicks(is) / (float) CrystalSeedItem.GROWTH_TICKS_REQUIRED);
    }

}
