package appeng.mixins;

import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Structure.class)
public interface StructureAccessor {

    @Invoker("func_236394_a_")
    static <F extends Structure<?>> F register(String id, F feature, GenerationStage.Decoration defaultStage) {
        throw new AssertionError();
    }

}
