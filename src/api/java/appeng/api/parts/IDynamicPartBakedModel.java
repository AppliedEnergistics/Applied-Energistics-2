package appeng.api.parts;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import appeng.api.util.AEPartLocation;

/**
 * This interface can be implemented by baked models returned by
 * {@link IPart#getStaticModels()} to indicate that they would like to use the
 * model data returned by {@link IPart#getModelData()}.
 */
public interface IDynamicPartBakedModel extends BakedModel {

    /**
     * See
     * {@link net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel#emitBlockQuads(BlockRenderView, BlockState, BlockPos, Supplier, RenderContext)}
     * for context.
     * <p>
     * The given <code>context</code> will already have been transformed so that the
     * model renders from the rotated location the part is attached to.
     *
     * @param partSide  The side of the cable bus that the part is attached to.
     * @param modelData The model data returned by {@link IPart#getModelData()}
     */
    void emitQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier,
            RenderContext context, Direction partSide, @Nullable Object modelData);

    /**
     * Unless you use your dynamic model for other purposes, this method will not be
     * called.
     */
    @Override
    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return Collections.emptyList();
    }

}
