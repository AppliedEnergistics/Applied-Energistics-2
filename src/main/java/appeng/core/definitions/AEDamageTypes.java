package appeng.core.definitions;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

import appeng.core.AppEng;

public class AEDamageTypes {
    public static final ResourceKey<DamageType> MATTER_CANNON = ResourceKey.create(Registries.DAMAGE_TYPE,
            AppEng.makeId("matter_cannon"));

    public static void init(BootstrapContext<DamageType> context) {
        context.register(MATTER_CANNON, new DamageType("matter_cannon", 0.1F));
    }
}
