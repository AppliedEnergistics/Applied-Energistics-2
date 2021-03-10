package appeng.client.render;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

/**
 * An unbaked model that has standard models as a dependency and produces a custom baked model as a result.
 */
public interface BasicUnbakedModel<T extends IModelGeometry<T>> extends IModelGeometry<T> {

    default Collection<ResourceLocation> getModelDependencies() {
        return Collections.emptyList();
    }

    default Stream<RenderMaterial> getAdditionalTextures() {
        return Stream.empty();
    }

    @Override
    default Collection<RenderMaterial> getTextures(IModelConfiguration owner,
            Function<ResourceLocation, IUnbakedModel> unbakedModelGetter,
            Set<Pair<String, String>> unresolvedTextureReferences) {
        return Stream.concat(
                getModelDependencies().stream().map(unbakedModelGetter)
                        .flatMap(ubm -> ubm.getMaterials(unbakedModelGetter, unresolvedTextureReferences).stream()),
                getAdditionalTextures()).collect(Collectors.toList());
    }

}
