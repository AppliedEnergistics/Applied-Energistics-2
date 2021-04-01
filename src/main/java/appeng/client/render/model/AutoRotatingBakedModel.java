package appeng.client.render.model;

import java.util.Random;
import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

import appeng.client.render.cablebus.QuadRotator;

@Environment(EnvType.CLIENT)
public class AutoRotatingBakedModel extends ForwardingBakedModel implements FabricBakedModel {

    public AutoRotatingBakedModel(IBakedModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    public IBakedModel getWrapped() {
        return wrapped;
    }

    @Override
    public void emitBlockQuads(IBlockDisplayReader blockView, BlockState state, BlockPos pos,
            Supplier<Random> randomSupplier, RenderContext context) {
        RenderContext.QuadTransform transform = getTransform(blockView, pos);

        if (transform != null) {
            context.pushTransform(transform);
        }

        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);

        if (transform != null) {
            context.popTransform();
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        super.emitItemQuads(stack, randomSupplier, context);
    }

    private RenderContext.QuadTransform getTransform(IBlockDisplayReader view, BlockPos pos) {
        if (!(view instanceof RenderAttachedBlockView)) {
            return null;
        }

        Object data = ((RenderAttachedBlockView) view).getBlockEntityRenderAttachment(pos);
        if (!(data instanceof AEModelData)) {
            return null;
        }

        AEModelData aeModelData = (AEModelData) data;
        RenderContext.QuadTransform transform = QuadRotator.get(aeModelData.getForward(), aeModelData.getUp());
        if (transform == QuadRotator.NULL_TRANSFORM) {
            return null;
        }
        return transform;
    }

}
