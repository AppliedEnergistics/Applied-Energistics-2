package appeng.init;

import appeng.core.stats.AdvancementTriggers;
import net.minecraft.advancements.CriteriaTriggers;

public final class InitAdvancementTriggers {

    public static void init() {
        CriteriaTriggers.register(AdvancementTriggers.NETWORK_APPRENTICE);
        CriteriaTriggers.register(AdvancementTriggers.NETWORK_ENGINEER);
        CriteriaTriggers.register(AdvancementTriggers.NETWORK_ADMIN);
        CriteriaTriggers.register(AdvancementTriggers.SPATIAL_EXPLORER);
    }

}
