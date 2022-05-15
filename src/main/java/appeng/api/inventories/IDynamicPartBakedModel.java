package appeng.api.inventories;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.parts.IPart;

/**
 * This interface can be implemented by baked models returned by {@link IPart#getStaticModels()} to indicate that they
 * would like to use the model data returned by {@link IPart#getRenderAttachmentData()}.
 */
public interface IDynamicPartBakedModel extends BakedModel {

    /**
     * See {@link net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel#emitBlockQuads} for context.
     * <p>
     * The given <code>context</code> will already have been transformed so that the model renders from the rotated
     * location the part is attached to.
     *
     * @param partSide  The side of the cable bus that the part is attached to.
     * @param modelData The model data returned by {@link IPart#getRenderAttachmentData()}
     */
    void emitQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier,
            RenderContext context, Direction partSide, @Nullable Object modelData);

    /**
     * Unless you use your dynamic model for other purposes, this method will not be called.
     */
    @Override
    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random) {
        return Collections.emptyList();
    }

}
