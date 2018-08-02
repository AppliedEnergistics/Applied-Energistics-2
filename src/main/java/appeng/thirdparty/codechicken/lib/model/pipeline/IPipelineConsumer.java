package appeng.thirdparty.codechicken.lib.model.pipeline;

import appeng.thirdparty.codechicken.lib.model.CachedFormat;
import appeng.thirdparty.codechicken.lib.model.ISmartVertexConsumer;
import appeng.thirdparty.codechicken.lib.model.Quad;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadReInterpolator;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

/**
 * Anything implementing this may be used in the BakedPipeline.
 * Created by covers1624 on 9/07/18.
 */
public interface IPipelineConsumer extends ISmartVertexConsumer {

    /**
     * The quad at the start of the transformation.
     * This is useful for obtaining the vertex data before any transformations have been applied,
     * such as interpolation, See {@link QuadReInterpolator}
     * When overriding this make sure you call setInputQuad on your parent consumer too.
     *
     * @param quad The quad.
     */
    void setInputQuad(Quad quad);

    /**
     * Resets the Consumer to the new format.
     * This should resize any internal arrays if needed,
     * ready for the new vertex data.
     *
     * @param format The format to reset to.
     */
    void reset(CachedFormat format);

    /**
     * Sets the parent consumer.
     * This consumer may choose to not pipe any data, that's fine,
     * but if it does, it MUST pipe the data to the one provided here.
     *
     * @param parent The parent.
     */
    void setParent(IVertexConsumer parent);
}
