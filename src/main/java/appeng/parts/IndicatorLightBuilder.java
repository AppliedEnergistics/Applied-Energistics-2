package appeng.parts;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.core.Direction;

/**
 * Used to add visual indicators for rendering purposes to parts.
 */
public class IndicatorLightBuilder {
    private final FloatArrayList vertices = new FloatArrayList();

    /**
     * Adds an indicator light that should be shown when the part represented by the given part item is rendered.
     * <p/>
     * It uses the same orientation as the default part models, which is oriented north.
     * <p/>
     * Parameters are defined in virtual pixels that assume a block spans from [0,0,0] to [16,16,16].
     */
    public void addFace(Direction face, float x1, float y1, float z1, float x2, float y2, float z2) {
        vertices.ensureCapacity(vertices.size() + 12);
        writeQuad(face, x1 / 16.f, y1 / 16.f, z1 / 16.f, x2 / 16.f, y2 / 16.f, z2 / 16.f, vertices);
    }

    /**
     * @return The array of vertex positions that were added.
     */
    public float[] getVertices() {
        return vertices.toFloatArray();
    }

    // Writes the face of a cube defined by [x1,y1,z1]/[x2,y2,z2] where [0,0,0] is the block's origin.
    private static void writeQuad(Direction face, float x1, float y1, float z1, float x2, float y2, float z2, FloatList buffer) {
        final float epsilon = 0.001f; // Try to avoid z-fighting

        switch (face) {
            case DOWN -> {
                addVertex(buffer, x2, y1 - epsilon, z1);
                addVertex(buffer, x2, y1 - epsilon, z2);
                addVertex(buffer, x1, y1 - epsilon, z2);
                addVertex(buffer, x1, y1 - epsilon, z1);
            }
            case UP -> {
                addVertex(buffer, x1, y2 + epsilon, z1);
                addVertex(buffer, x1, y2 + epsilon, z2);
                addVertex(buffer, x2, y2 + epsilon, z2);
                addVertex(buffer, x2, y2 + epsilon, z1);
            }
            case NORTH -> {
                addVertex(buffer, x2, y2, z1 - epsilon);
                addVertex(buffer, x2, y1, z1 - epsilon);
                addVertex(buffer, x1, y1, z1 - epsilon);
                addVertex(buffer, x1, y2, z1 - epsilon);
            }
            case SOUTH -> {
                addVertex(buffer, x1, y2, z2 + epsilon);
                addVertex(buffer, x1, y1, z2 + epsilon);
                addVertex(buffer, x2, y1, z2 + epsilon);
                addVertex(buffer, x2, y2, z2 + epsilon);
            }
            case WEST -> {
                addVertex(buffer, x1 - epsilon, y1, z1);
                addVertex(buffer, x1 - epsilon, y1, z2);
                addVertex(buffer, x1 - epsilon, y2, z2);
                addVertex(buffer, x1 - epsilon, y2, z1);
            }
            case EAST -> {
                addVertex(buffer, x2 + epsilon, y2, z1);
                addVertex(buffer, x2 + epsilon, y2, z2);
                addVertex(buffer, x2 + epsilon, y1, z2);
                addVertex(buffer, x2 + epsilon, y1, z1);
            }
        }
    }

    private static void addVertex(FloatList buffer, float x, float y, float z) {
        buffer.add(x);
        buffer.add(y);
        buffer.add(z);
    }
}
