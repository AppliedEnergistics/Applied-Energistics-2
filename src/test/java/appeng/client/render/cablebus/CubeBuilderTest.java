package appeng.client.render.cablebus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
class CubeBuilderTest {

    /**
     * Tests that the cube builder creates faces that adhere to the vertex order assumed by Vanilla. Vanilla expects for
     * a given side that the vertices are in a specific order for the min/max of the relevant axes and uses this
     * assumption to determine which side to retrieve block lighting from when it applies shading to vertices.
     * <p>
     * To test this, we simply create a quad for a given side, then apply the vanilla vertex reordering, and test that
     * the order hasn't changed.
     */
    @ParameterizedTest
    @EnumSource(Direction.class)
    void testFaceVertexOrdering(Direction side) {
        try (var mockedStatic = Mockito.mockStatic(RenderSystem.class)) {
            var output = new ArrayList<BakedQuad>();
            var cb = new CubeBuilder(output);
            cb.setTexture(mock(TextureAtlasSprite.class));
            cb.addQuad(side, 0, 0, 0, 1, 1, 1);

            assertEquals(1, output.size());
            var quad = output.get(0);
            assertEquals(side, quad.getDirection());

            var fb = new FaceBakery();
            var rewinded = quad.getVertices().clone();
            fb.recalculateWinding(rewinded, side);

            assertThat(getVertexOrder(rewinded, quad.getVertices()))
                    .containsExactly(0, 1, 2, 3);
        }
    }

    // Get the order of vertices compared to the original array
    private int[] getVertexOrder(int[] originalData, int[] data) {
        var vertexInts = DefaultVertexFormat.BLOCK.getIntegerSize();
        var orgVertexCount = originalData.length / vertexInts;
        var vertexCount = data.length / vertexInts;
        int[] result = new int[vertexCount];

        for (var i = 0; i < vertexCount; i++) {
            result[i] = -1;
            for (int j = 0; j < orgVertexCount; j++) {
                if (Arrays.equals(
                        data, i * vertexInts, (i + 1) * vertexInts,
                        originalData, j * vertexInts, (j + 1) * vertexInts)) {
                    result[i] = j;
                    break;
                }
            }
        }

        return result;
    }
}
