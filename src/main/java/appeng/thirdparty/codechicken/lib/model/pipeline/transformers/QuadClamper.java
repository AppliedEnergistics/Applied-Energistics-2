package appeng.thirdparty.codechicken.lib.model.pipeline.transformers;

import appeng.thirdparty.codechicken.lib.model.Quad.Vertex;
import appeng.thirdparty.codechicken.lib.model.pipeline.IPipelineElementFactory;
import appeng.thirdparty.codechicken.lib.model.pipeline.QuadTransformer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

/**
 * This transformer simply clamps the vertices inside the provided box.
 * You probably want to Re-Interpolate the UV's, Color, and Lmap, see {@link QuadReInterpolator}
 *
 * Created by covers1624 on 30/07/18.
 */
public class QuadClamper extends QuadTransformer {

    public static IPipelineElementFactory<QuadClamper> FACTORY = QuadClamper::new;

    private AxisAlignedBB clampBounds;

    QuadClamper() {
        super();
    }

    public QuadClamper(IVertexConsumer parent, AxisAlignedBB bounds) {
        super(parent);
        this.clampBounds = bounds;
    }

    public void setClampBounds(AxisAlignedBB bounds) {
        this.clampBounds = bounds;
    }

    @Override
    public boolean transform() {
        int s = quad.orientation.ordinal() >> 1;

        quad.clamp(clampBounds);

        //Check if the quad would be invisible and cull it.
        Vertex[] vertices = quad.vertices;
        float x1 = vertices[0].dx(s);
        float x2 = vertices[1].dx(s);
        float x3 = vertices[2].dx(s);
        float x4 = vertices[3].dx(s);

        float y1 = vertices[0].dy(s);
        float y2 = vertices[1].dy(s);
        float y3 = vertices[2].dy(s);
        float y4 = vertices[3].dy(s);

        //These comparisons are safe as we are comparing clamped values.
        boolean flag1 = x1 == x2 && x2 == x3 && x3 == x4;
        boolean flag2 = y1 == y2 && y2 == y3 && y3 == y4;
        return !flag1 && !flag2;
    }

}
