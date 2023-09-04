package appeng.init;

import java.util.List;

import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.TierSortingRegistry;

import appeng.core.AppEng;
import appeng.items.tools.fluix.FluixToolType;
import appeng.items.tools.quartz.QuartzToolType;

public final class InitTiers {
    private InitTiers() {
    }

    public static void init() {
        for (var quartz : QuartzToolType.values()) {
            TierSortingRegistry.registerTier(quartz.getToolTier(), AppEng.makeId(quartz.getName()),
                    List.of(Tiers.IRON), List.of(Tiers.DIAMOND));
        }

        TierSortingRegistry.registerTier(FluixToolType.FLUIX.getToolTier(),
                AppEng.makeId(FluixToolType.FLUIX.getName()),
                List.of(QuartzToolType.CERTUS.getToolTier(), QuartzToolType.NETHER.getToolTier()),
                List.of(Tiers.DIAMOND));
    }
}
