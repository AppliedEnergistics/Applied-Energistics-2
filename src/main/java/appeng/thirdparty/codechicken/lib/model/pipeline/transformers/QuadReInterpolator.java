package appeng.thirdparty.codechicken.lib.model.pipeline.transformers;

import appeng.thirdparty.codechicken.lib.math.InterpHelper;
import appeng.thirdparty.codechicken.lib.model.CachedFormat;
import appeng.thirdparty.codechicken.lib.model.Quad;
import appeng.thirdparty.codechicken.lib.model.Quad.Vertex;
import appeng.thirdparty.codechicken.lib.model.pipeline.IPipelineElementFactory;
import appeng.thirdparty.codechicken.lib.model.pipeline.QuadTransformer;

/**
 * This transformer Re-Interpolates the Color, UV's and LightMaps.
 * Use this after all transformations that translate vertices in the pipeline.
 *
 * This Transformation can only be used in the BakedPipeline.
 *
 * Created by covers1624 on 30/07/18.
 */
public class QuadReInterpolator extends QuadTransformer {

    public static final IPipelineElementFactory<QuadReInterpolator> FACTORY = QuadReInterpolator::new;

    private Quad interpCache = new Quad();
    private InterpHelper interpHelper = new InterpHelper();

    QuadReInterpolator() {
        super();
    }

    @Override
    public void reset(CachedFormat format) {
        super.reset(format);
        interpCache.reset(format);
    }

    @Override
    public void setInputQuad(Quad quad) {
        super.setInputQuad(quad);
        quad.resetInterp(interpHelper, quad.orientation.ordinal() >> 1);
    }

    @Override
    public boolean transform() {
        int s = quad.orientation.ordinal() >> 1;
        if (format.hasColor || format.hasUV || format.hasLightMap) {
            interpCache.copyFrom(quad);
            interpHelper.setup();
            for (Vertex v : quad.vertices) {
                interpHelper.locate(v.dx(s), v.dy(s));
                if (format.hasColor) {
                    v.interpColorFrom(interpHelper, interpCache.vertices);
                }
                if (format.hasUV) {
                    v.interpUVFrom(interpHelper, interpCache.vertices);
                }
                if (format.hasLightMap) {
                    v.interpLightMapFrom(interpHelper, interpCache.vertices);
                }
            }
        }
        return true;
    }
}
