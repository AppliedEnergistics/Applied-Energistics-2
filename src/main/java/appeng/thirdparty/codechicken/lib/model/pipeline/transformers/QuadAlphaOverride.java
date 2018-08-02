package appeng.thirdparty.codechicken.lib.model.pipeline.transformers;

import appeng.thirdparty.codechicken.lib.model.Quad.Vertex;
import appeng.thirdparty.codechicken.lib.model.pipeline.IPipelineElementFactory;
import appeng.thirdparty.codechicken.lib.model.pipeline.QuadTransformer;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

/**
 * This transformer simply overrides the alpha of the quad.
 * Only operates if the format has color.
 *
 * Created by covers1624 on 9/07/18.
 */
public class QuadAlphaOverride extends QuadTransformer {

    public static final IPipelineElementFactory<QuadAlphaOverride> FACTORY = QuadAlphaOverride::new;

    private float alphaOverride;

    QuadAlphaOverride() {
        super();
    }

    public QuadAlphaOverride(IVertexConsumer consumer, float alphaOverride) {
        super(consumer);
        this.alphaOverride = alphaOverride;
    }

    public QuadAlphaOverride setAlphaOverride(float alphaOverride) {
        this.alphaOverride = alphaOverride;
        return this;
    }

    @Override
    public boolean transform() {
        if (format.hasColor) {
            for (Vertex v : quad.vertices) {
                v.color[3] = alphaOverride;
            }
        }
        return true;
    }
}
