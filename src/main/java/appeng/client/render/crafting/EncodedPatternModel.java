package appeng.client.render.crafting;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

/**
 * This model is used to provide the {@link EncodedPatternBakedModel}.
 */
public class EncodedPatternModel implements IModelGeometry<EncodedPatternModel> {

    private final ResourceLocation baseModel;

    public EncodedPatternModel(ResourceLocation baseModel) {
        this.baseModel = baseModel;
    }

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner,
            Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return modelGetter.apply(baseModel).getTextures(modelGetter, missingTextureErrors);
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
            Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
            ItemOverrideList overrides, ResourceLocation modelLocation) {
        IBakedModel baseModel = bakery.getBakedModel(this.baseModel, modelTransform, spriteGetter);
        return new EncodedPatternBakedModel(baseModel);
    }

}
