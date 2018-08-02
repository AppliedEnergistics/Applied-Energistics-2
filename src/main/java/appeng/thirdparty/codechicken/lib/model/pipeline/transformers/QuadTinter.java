package appeng.thirdparty.codechicken.lib.model.pipeline.transformers;

import appeng.thirdparty.codechicken.lib.model.Quad.Vertex;
import appeng.thirdparty.codechicken.lib.model.pipeline.IPipelineElementFactory;
import appeng.thirdparty.codechicken.lib.model.pipeline.QuadTransformer;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

/**
 * This transformer tints quads..
 * Feed it the output of BlockColors.colorMultiplier.
 *
 * Created by covers1624 on 9/07/18.
 */
public class QuadTinter extends QuadTransformer {

    public static final IPipelineElementFactory<QuadTinter> FACTORY = QuadTinter::new;

    private int tint;

    QuadTinter() {
        super();
    }

    public QuadTinter(IVertexConsumer consumer, int tint) {
        super(consumer);
        this.tint = tint;
    }

    public QuadTinter setTint(int tint) {
        this.tint = tint;
        return this;
    }

    @Override
    public boolean transform() {
        //Nuke tintIndex.
        quad.tintIndex = -1;
        if (format.hasColor) {
            float r = (float) (tint >> 0x10 & 0xFF) / 255F;
            float g = (float) (tint >> 0x08 & 0xFF) / 255F;
            float b = (float) (tint & 0xFF) / 255F;
            for (Vertex v : quad.vertices) {
                v.color[0] *= r;
                v.color[1] *= g;
                v.color[2] *= b;
            }
        }
        return true;
    }
}
