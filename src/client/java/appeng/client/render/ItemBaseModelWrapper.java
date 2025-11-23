package appeng.client.render;

import static net.minecraft.client.renderer.item.BlockModelWrapper.computeExtents;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;

public record ItemBaseModelWrapper(
        List<BakedQuad> quads,
        Supplier<Vector3fc[]> extents,
        ModelRenderProperties renderProperties,
        @Nullable RenderType renderType) {
    public static ItemBaseModelWrapper bake(ModelBaker modelBaker, Identifier id) {
        var baseModel = modelBaker.getModel(id);

        var baseModelTextures = baseModel.getTopTextureSlots();
        List<BakedQuad> baseModelQuads = baseModel
                .bakeTopGeometry(baseModelTextures, modelBaker, BlockModelRotation.IDENTITY).getAll();

        var modelRenderProperties = ModelRenderProperties.fromResolvedModel(modelBaker, baseModel, baseModelTextures);
        var renderTypeGroup = baseModel.getTopAdditionalProperties().getOptional(NeoForgeModelProperties.RENDER_TYPE);
        var renderType = renderTypeGroup == null ? null : renderTypeGroup.entityItem();
        var extents = Suppliers.memoize(() -> computeExtents(baseModelQuads));

        return new ItemBaseModelWrapper(baseModelQuads, extents, modelRenderProperties, renderType);
    }

    public void applyToLayer(ItemStackRenderState.LayerRenderState layer, ItemDisplayContext context) {
        layer.setExtents(extents);
        renderProperties.applyToLayer(layer, context);
        layer.prepareQuadList().addAll(quads);
        if (renderType != null) {
            layer.setRenderType(renderType);
        } else {
            layer.setRenderType(Sheets.translucentItemSheet());
        }
    }
}
