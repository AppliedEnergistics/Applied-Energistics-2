package appeng.thirdparty.codechicken.lib.model;

import net.minecraftforge.client.model.pipeline.IVertexConsumer;

/**
 * Marks a standard IVertexConsumer as compatible with {@link Quad}.
 *
 *
 * Created by covers1624 on 9/07/18.
 */
public interface ISmartVertexConsumer extends IVertexConsumer {

    /**
     * Assumes the data is already completely unpacked.
     * You must always copy the data from the quad provided to an internal cache.
     * basically: this.quad.put(quad);
     * @param quad The quad to copy data from.
     */
    void put(Quad quad);
}
