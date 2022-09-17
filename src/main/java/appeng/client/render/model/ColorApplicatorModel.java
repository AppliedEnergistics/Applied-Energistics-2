package appeng.client.render.model;


import appeng.core.AppEng;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;


/**
 * A color applicator uses the base model, and extends it with additional layers that are colored according to the
 * selected color of the applicator.
 */
public class ColorApplicatorModel implements IModel {

    private static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "item/color_applicator_colored");

    private static final ResourceLocation TEXTURE_DARK = new ResourceLocation(AppEng.MOD_ID, "items/color_applicator_tip_dark");
    private static final ResourceLocation TEXTURE_MEDIUM = new ResourceLocation(AppEng.MOD_ID, "items/color_applicator_tip_medium");
    private static final ResourceLocation TEXTURE_BRIGHT = new ResourceLocation(AppEng.MOD_ID, "items/color_applicator_tip_bright");

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.singletonList(MODEL_BASE);
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return ImmutableList.of(
                TEXTURE_DARK,
                TEXTURE_MEDIUM,
                TEXTURE_BRIGHT);
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        IBakedModel baseModel = this.getBaseModel(state, format, bakedTextureGetter);

        TextureAtlasSprite texDark = bakedTextureGetter.apply(TEXTURE_DARK);
        TextureAtlasSprite texMedium = bakedTextureGetter.apply(TEXTURE_MEDIUM);
        TextureAtlasSprite texBright = bakedTextureGetter.apply(TEXTURE_BRIGHT);

        ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> map = PerspectiveMapWrapper.getTransforms(state);

        return new ColorApplicatorBakedModel(baseModel, map, texDark, texMedium, texBright);
    }

    private IBakedModel getBaseModel(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        // Load the base model
        try {
            return ModelLoaderRegistry.getModel(MODEL_BASE).bake(state, format, bakedTextureGetter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }
}
