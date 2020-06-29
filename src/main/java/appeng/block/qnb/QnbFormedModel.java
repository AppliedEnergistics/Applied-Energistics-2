
package appeng.block.qnb;

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

import appeng.core.AppEng;

public class QnbFormedModel implements IModelGeometry<QnbFormedModel> {

    private static final Identifier MODEL_RING = new Identifier(AppEng.MOD_ID, "block/qnb/ring");

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelLoader bakery,
                           Function<SpriteIdentifier, Sprite> spriteGetter, IModelTransform modelTransform,
                           ModelOverrideList overrides, Identifier modelLocation) {
        BakedModel ringModel = bakery.getBakedModel(MODEL_RING, modelTransform, spriteGetter);
        return new QnbFormedBakedModel(ringModel, spriteGetter);
    }

    @Override
    public Collection<SpriteIdentifier> getTextures(IModelConfiguration owner,
                                                    Function<Identifier, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return QnbFormedBakedModel.getRequiredTextures();
    }

}
