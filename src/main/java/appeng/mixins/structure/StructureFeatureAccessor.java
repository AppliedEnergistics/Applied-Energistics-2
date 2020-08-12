package appeng.mixins.structure;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.StructureFeature;

@Mixin(StructureFeature.class)
public interface StructureFeatureAccessor {

    @Invoker("register")
    static <F extends StructureFeature<?>> F register(String id, F structureFeature, GenerationStep.Feature step) {
        throw new AssertionError();
    }

}
