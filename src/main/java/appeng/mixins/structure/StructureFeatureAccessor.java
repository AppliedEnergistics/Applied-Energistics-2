package appeng.mixins.structure;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.structure.Structure;

@Mixin(Structure.class)
public interface StructureFeatureAccessor {

    @Invoker("func_236394_a_")
    static <F extends Structure<?>> F register(String id, F structureFeature, GenerationStage.Decoration step) {
        throw new AssertionError();
    }

}
