package appeng.client.render.cablebus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import com.mojang.blaze3d.platform.Transparency;
import com.mojang.blaze3d.systems.RenderSystem;

import org.joml.Vector3fc;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;

import appeng.client.render.CubeBuilder;
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
            var cb = new CubeBuilder(output::add);
            var mockedSprite = mock(TextureAtlasSprite.class);
            var mockedSpriteContents = mock(SpriteContents.class);
            when(mockedSpriteContents.transparency()).thenReturn(Transparency.NONE);
            when(mockedSprite.atlasLocation()).thenReturn(TextureAtlas.LOCATION_BLOCKS);
            when(mockedSprite.contents()).thenReturn(mockedSpriteContents);
            cb.setTexture(mockedSprite);
            cb.addQuad(side, 0, 0, 0, 1, 1, 1);

            assertEquals(1, output.size());
            var quad = output.get(0);
            assertEquals(side, quad.direction());

            var positions = new Vector3fc[] {
                    quad.position0(),
                    quad.position1(),
                    quad.position2(),
                    quad.position3()
            };
            var uvs = new long[] {
                    quad.packedUV0(),
                    quad.packedUV1(),
                    quad.packedUV2(),
                    quad.packedUV3()
            };
            var originalPositions = positions.clone();

            FaceBakery.recalculateWinding(positions, uvs, side);

            assertThat(getVertexOrder(originalPositions, positions))
                    .containsExactly(0, 1, 2, 3);
        }
    }

    // Get the order of vertices compared to the original array
    private int[] getVertexOrder(Vector3fc[] originalPositions, Vector3fc[] positions) {
        int[] result = new int[originalPositions.length];

        for (var i = 0; i < originalPositions.length; i++) {
            result[i] = -1;
            for (int j = 0; j < originalPositions.length; j++) {
                if (originalPositions[i].equals(positions[j])) {
                    result[i] = j;
                    break;
                }
            }
        }

        return result;
    }
}
