package appeng.client.render.crafting;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.render.model.IBakedModel;
import net.minecraft.client.render.model.IModelTransform;
import net.minecraft.client.render.model.IUnbakedModel;
import net.minecraft.client.render.model.ItemOverrideList;
import net.minecraft.client.render.model.Material;
import net.minecraft.client.render.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

/**
 * This model is used to provide the {@link EncodedPatternBakedModel}.
 */
public class EncodedPatternModel implements IModelGeometry<EncodedPatternModel> {

    private final Identifier baseModel;

    public EncodedPatternModel(Identifier baseModel) {
        this.baseModel = baseModel;
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner,
                                            Function<Identifier, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return modelGetter.apply(baseModel).getTextures(modelGetter, missingTextureErrors);
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
            ItemOverrideList overrides, Identifier modelLocation) {
        IBakedModel baseModel = bakery.getBakedModel(this.baseModel, modelTransform, spriteGetter);
        return new EncodedPatternBakedModel(baseModel);
    }

}
