package appeng.mixins.structure;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import appeng.worldgen.meteorite.MeteoriteStructure;

/**
 * This Mixin will add the structure placement configuration for the meteorite structure to the static final immutable
 * map that contains them. There is currently no Fabric API for this, and registering them during the registry event is
 * already too late.
 * <p>
 * If this is not done, Meteorites spawn every chunk, since that is the default for missing entries.
 */
@Mixin(DimensionStructuresSettings.class)
public class StructuresConfigMixin {

    @Shadow
    @Mutable
    private static ImmutableMap<Structure<?>, StructureSeparationSettings> DEFAULT_STRUCTURES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addMeteoriteConfig(CallbackInfo ci) {
        DEFAULT_STRUCTURES = ImmutableMap.<Structure<?>, StructureSeparationSettings>builder().putAll(DEFAULT_STRUCTURES)
                .put(MeteoriteStructure.INSTANCE, MeteoriteStructure.PLACEMENT_CONFIG).build();
    }

}
