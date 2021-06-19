package appeng.init;

import net.minecraft.stats.IStatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import appeng.core.stats.AeStats;

public final class InitStats {

    private InitStats() {
    }

    public static void init() {
        for (AeStats stat : AeStats.values()) {
            // Compare with net.minecraft.stats.Stats#registerCustom
            ResourceLocation registryName = stat.getRegistryName();
            Registry.register(Registry.CUSTOM_STAT, registryName.getPath(), registryName);
            Stats.CUSTOM.get(registryName, IStatFormatter.DEFAULT);
        }
    }

}
