package appeng.mixins;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(Biome.class)
public interface BiomeAccessor {

    @Accessor
    Map<Integer, List<StructureFeature<?>>> getField_26634();

}
