package appeng.client.render.model;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class AutoRotatingModel implements IModelGeometry<AutoRotatingModel> {

    private final BlockModel blockModel;

    public AutoRotatingModel(BlockModel blockModel) {
        this.blockModel = blockModel;
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        return new AutoRotatingBakedModel(
                this.blockModel.bakeModel(bakery, this.blockModel, spriteGetter, modelTransform, modelLocation, true)
        );
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return this.blockModel.getTextures(modelGetter, missingTextureErrors);
    }

}
