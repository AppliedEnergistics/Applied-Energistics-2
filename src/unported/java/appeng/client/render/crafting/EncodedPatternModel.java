package appeng.client.render.crafting;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.IModelTransform;
import net.minecraft.client.render.model.IUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
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
    public Collection<SpriteIdentifier> getTextures(IModelConfiguration owner,
                                                    Function<Identifier, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return modelGetter.apply(baseModel).getTextures(modelGetter, missingTextureErrors);
    }

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelLoader bakery,
                           Function<SpriteIdentifier, Sprite> spriteGetter, IModelTransform modelTransform,
                           ModelOverrideList overrides, Identifier modelLocation) {
        BakedModel baseModel = bakery.getBakedModel(this.baseModel, modelTransform, spriteGetter);
        return new EncodedPatternBakedModel(baseModel);
    }

}
