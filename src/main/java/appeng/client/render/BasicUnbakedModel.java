package appeng.client.render;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.util.ResourceLocation;
import com.mojang.datafixers.util.Pair;

/**
 * An unbaked model that has standard models as a dependency and produces a custom baked model as a result.
 */
public interface BasicUnbakedModel extends IUnbakedModel {

    @Override
    default Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    default Stream<RenderMaterial> getAdditionalTextures() {
        return Stream.empty();
    }

    @Override
    default Collection<RenderMaterial> getTextures(Function<ResourceLocation, IUnbakedModel> unbakedModelGetter,
            Set<Pair<String, String>> unresolvedTextureReferences) {
        return Stream.concat(
                getDependencies().stream().map(unbakedModelGetter).flatMap(
                        ubm -> ubm.getTextures(unbakedModelGetter, unresolvedTextureReferences).stream()),
                getAdditionalTextures()).collect(Collectors.toList());
    }

}
