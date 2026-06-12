package appeng.client.render;

import static net.minecraft.client.renderer.item.CuboidItemModelWrapper.computeExtents;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;

public record ItemBaseModelWrapper(
        List<BakedQuad> quads,
        Supplier<Vector3fc[]> extents,
        ModelRenderProperties renderProperties,
        Matrix4fc transformation) {

    private static final Matrix4fc IDENTITY = new Matrix4f();

    public static ItemBaseModelWrapper bake(ModelBaker modelBaker, Identifier id) {
        return bake(modelBaker, id, IDENTITY);
    }

    public static ItemBaseModelWrapper bake(ModelBaker modelBaker, Identifier id, Matrix4fc transform) {
        var baseModel = modelBaker.getModel(id);

        var baseModelTextures = baseModel.getTopTextureSlots();
        List<BakedQuad> baseModelQuads = baseModel
                .bakeTopGeometry(baseModelTextures, modelBaker, BlockModelRotation.IDENTITY).getAll();

        var modelRenderProperties = ModelRenderProperties.fromResolvedModel(modelBaker, baseModel, baseModelTextures);
        var extents = Suppliers.memoize(() -> computeExtents(baseModelQuads));

        return new ItemBaseModelWrapper(baseModelQuads, extents, modelRenderProperties, transform);
    }

    public void applyToLayer(ItemStackRenderState.LayerRenderState layer, ItemDisplayContext context) {
        layer.setExtents(extents);
        renderProperties.applyToLayer(layer, context);
        layer.prepareQuadList().addAll(quads);
    }
}
