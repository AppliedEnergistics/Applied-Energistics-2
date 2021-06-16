package appeng.init.worldgen;

import net.minecraft.util.ResourceLocation;

import appeng.core.AppEng;

/**
 * IDS used for world generation features.
 */
final class WorldgenIds {

    private WorldgenIds() {
    }

    /**
     * ID of the {@link net.minecraft.world.gen.feature.ConfiguredFeature} that generates quartz ore.
     */
    public static final ResourceLocation QUARTZ_ORE = AppEng.makeId("quartz_ore");

    /**
     * ID of the {@link net.minecraft.world.gen.feature.ConfiguredFeature} and
     * {@link net.minecraft.world.gen.feature.Feature} that generate charged quartz ore.
     */
    public static final ResourceLocation CHARGED_QUARTZ_ORE = AppEng.makeId("charged_quartz_ore");

}
