package appeng.client.render;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;

public record ItemBaseModelWrapper(
        List<BakedQuad> quads,
        ModelRenderProperties renderProperties,
        @Nullable RenderType renderType) {
    public static ItemBaseModelWrapper bake(ModelBaker modelBaker, ResourceLocation id) {
        var baseModel = modelBaker.getModel(id);

        var baseModelTextures = baseModel.getTopTextureSlots();
        List<BakedQuad> baseModelQuads = baseModel
                .bakeTopGeometry(baseModelTextures, modelBaker, BlockModelRotation.X0_Y0).getAll();

        var modelRenderProperties = ModelRenderProperties.fromResolvedModel(modelBaker, baseModel, baseModelTextures);
        var renderTypeGroup = baseModel.getTopAdditionalProperties().getOptional(NeoForgeModelProperties.RENDER_TYPE);
        var renderType = renderTypeGroup == null ? null : renderTypeGroup.entity();

        return new ItemBaseModelWrapper(baseModelQuads, modelRenderProperties, renderType);
    }

    public void applyToLayer(ItemStackRenderState.LayerRenderState layer, ItemDisplayContext context) {
        renderProperties.applyToLayer(layer, context);
        layer.prepareQuadList().addAll(quads);
        if (renderType != null) {
            layer.setRenderType(renderType);
        } else {
            layer.setRenderType(Sheets.translucentItemSheet());
        }
    }
}
