
package appeng.block.qnb;

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

import appeng.core.AppEng;

public class QnbFormedModel implements IModelGeometry<QnbFormedModel> {

    private static final Identifier MODEL_RING = new Identifier(AppEng.MOD_ID, "block/qnb/ring");

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
            ItemOverrideList overrides, Identifier modelLocation) {
        IBakedModel ringModel = bakery.getBakedModel(MODEL_RING, modelTransform, spriteGetter);
        return new QnbFormedBakedModel(ringModel, spriteGetter);
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner,
                                            Function<Identifier, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return QnbFormedBakedModel.getRequiredTextures();
    }

}
