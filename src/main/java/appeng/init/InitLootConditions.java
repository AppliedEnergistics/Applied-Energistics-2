package appeng.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import appeng.core.AppEng;
import appeng.loot.NeedsPressCondition;

public final class InitLootConditions {

    public static final ResourceLocation NEEDS_PRESS = AppEng.makeId("needs_press");

    private InitLootConditions() {
    }

    public static void init() {
        Registry.register(Registry.LOOT_CONDITION_TYPE, NEEDS_PRESS, NeedsPressCondition.TYPE);
    }

}
