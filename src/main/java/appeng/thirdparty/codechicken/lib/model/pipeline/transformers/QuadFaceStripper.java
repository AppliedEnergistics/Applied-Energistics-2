package appeng.thirdparty.codechicken.lib.model.pipeline.transformers;

import appeng.thirdparty.codechicken.lib.model.Quad.Vertex;
import appeng.thirdparty.codechicken.lib.model.pipeline.IPipelineElementFactory;
import appeng.thirdparty.codechicken.lib.model.pipeline.QuadTransformer;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

import static net.minecraft.util.EnumFacing.AxisDirection.POSITIVE;

/**
 * This transformer strips quads that are on faces.
 * Simply set the bounds for the faces, and the strip mask.
 * Created by covers1624 on 30/07/18.
 */
public class QuadFaceStripper extends QuadTransformer {

    public static final IPipelineElementFactory<QuadFaceStripper> FACTORY = QuadFaceStripper::new;

    private AxisAlignedBB bounds;
    private int mask;

    QuadFaceStripper() {
        super();
    }

    public QuadFaceStripper(IVertexConsumer parent, AxisAlignedBB bounds, int mask) {
        super(parent);
        this.bounds = bounds;
        this.mask = mask;
    }

    /**
     * The bounds of the faces,
     * used as the .. bounds, if all vertices of a quad
     * lay on the bounds, it is up for stripping.
     *
     * @param bounds The bounds.
     */
    public void setBounds(AxisAlignedBB bounds) {
        this.bounds = bounds;
    }

    /**
     * The mask to strip edges.
     * This is an opt in system,
     * the mask is simple 'mask = (1 << side)'.
     *
     * @param mask The mask.
     */
    public void setMask(int mask) {
        this.mask = mask;
    }

    @Override
    public boolean transform() {
        if (mask == 0) {
            return true;//No mask, nothing changes.
        }
        //If the bit for this quad is set, then check if we should strip.
        if ((mask & (1 << quad.orientation.ordinal())) != 0) {
            AxisDirection dir = quad.orientation.getAxisDirection();
            Vertex[] vertices = quad.vertices;
            switch (quad.orientation.getAxis()) {
                case X: {
                    float bound = (float) (dir == POSITIVE ? bounds.maxX : bounds.minX);
                    float x1 = vertices[0].vec[0];
                    float x2 = vertices[1].vec[0];
                    float x3 = vertices[2].vec[0];
                    float x4 = vertices[3].vec[0];
                    return x1 != x2 || x2 != x3 || x3 != x4 || x4 != bound;
                }
                case Y: {
                    float bound = (float) (dir == POSITIVE ? bounds.maxY : bounds.minY);
                    float y1 = vertices[0].vec[1];
                    float y2 = vertices[1].vec[1];
                    float y3 = vertices[2].vec[1];
                    float y4 = vertices[3].vec[1];
                    return y1 != y2 || y2 != y3 || y3 != y4 || y4 != bound;
                }
                case Z: {
                    float bound = (float) (dir == POSITIVE ? bounds.maxZ : bounds.minZ);
                    float z1 = vertices[0].vec[2];
                    float z2 = vertices[1].vec[2];
                    float z3 = vertices[2].vec[2];
                    float z4 = vertices[3].vec[2];
                    return z1 != z2 || z2 != z3 || z3 != z4 || z4 != bound;
                }
            }
        }
        return true;
    }
}
