package appeng.mixins.structure;

import java.util.List;
import java.util.Map;

import net.minecraft.world.gen.feature.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.StructureFeature;

/**
 * Allows access to the copy of _all_ structures that is maintained within each
 * instance of {@link Biome}, in order to add our new structure to it. Note that
 * this does not mean the structure will start _generating_ in this biome, it
 * will only continue to generate if an adjacent biome has started the structure
 * and it extends into this one.
 */
@Mixin(Biome.class)
public interface BiomeAccessor {

    @Accessor
    Map<Integer, List<Structure<?>>> getField_242421_g();

}
