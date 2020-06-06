
package appeng.block.qnb;


import appeng.core.AppEng;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;


public class QnbFormedModel implements IModelGeometry<QnbFormedModel>
{

	private static final ResourceLocation MODEL_RING = new ResourceLocation( AppEng.MOD_ID, "block/qnb/ring" );

	@Override
	public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
		IBakedModel ringModel = bakery.getBakedModel(MODEL_RING, modelTransform, spriteGetter);
		return new QnbFormedBakedModel( ringModel, spriteGetter );
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		return QnbFormedBakedModel.getRequiredTextures();
	}

}
