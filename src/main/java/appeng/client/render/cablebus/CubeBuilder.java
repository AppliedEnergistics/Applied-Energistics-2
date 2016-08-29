package appeng.client.render.cablebus;


import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import javax.vecmath.Vector4f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;


/**
 * Builds the quads for a cube.
 */
public class CubeBuilder {

    private VertexFormat format;

    private final List<BakedQuad> output;

    private final EnumMap<EnumFacing, TextureAtlasSprite> textures = new EnumMap<>(EnumFacing.class);

    private EnumSet<EnumFacing> drawFaces = EnumSet.allOf(EnumFacing.class);

    private final EnumMap<EnumFacing, Vector4f> customUv = new EnumMap<>(EnumFacing.class);

    private int color = 0xFFFFFFFF;

    private boolean renderFullBright;

    public CubeBuilder(VertexFormat format, List<BakedQuad> output) {
        this.output = output;
        this.format = format;
    }

    public CubeBuilder(VertexFormat format) {
        this(format, new ArrayList<>(6));
    }

    public void addCube(float x1, float y1, float z1, float x2, float y2, float z2) {
        x1 /= 16.0f;
        y1 /= 16.0f;
        z1 /= 16.0f;
        x2 /= 16.0f;
        y2 /= 16.0f;
        z2 /= 16.0f;

        // If brightness is forced to specific values, extend the vertex format to contain the multi-texturing lightmap offset
        VertexFormat savedFormat = null;
        if (renderFullBright) {
            savedFormat = format;
            format = new VertexFormat(savedFormat);
            if (!format.getElements().contains(DefaultVertexFormats.TEX_2S)) {
                format.addElement(DefaultVertexFormats.TEX_2S);
            }
        }

        for (EnumFacing face : drawFaces) {
            putFace(face, x1, y1, z1, x2, y2, z2);
        }

        // Restore old format
        if (savedFormat != null) {
            format = savedFormat;
        }
    }

    private void putFace(EnumFacing face,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2
    ) {

        TextureAtlasSprite texture = textures.get(face);

        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setTexture(texture);
        builder.setQuadOrientation(face);

        float u1 = 0;
        float v1 = 0;
        float u2 = 0;
        float v2 = 0;

        // The user might have set specific UV coordinates for this face
        Vector4f customUv = this.customUv.get( face );
        if( customUv != null )
        {
            u1 = texture.getInterpolatedU( customUv.x );
            v1 = texture.getInterpolatedV( customUv.y );
            u2 = texture.getInterpolatedU( customUv.z );
            v2 = texture.getInterpolatedV( customUv.w );
        }

        switch (face) {
            case DOWN:
                if (customUv == null)
                {
                    u1 = texture.getInterpolatedU( x1 * 16 );
                    v1 = texture.getInterpolatedV( z1 * 16 );
                    u2 = texture.getInterpolatedU( x2 * 16 );
                    v2 = texture.getInterpolatedV( z2 * 16 );
                }

                putVertex(builder, face, x2, y1, z1, u2, v1);
                putVertex(builder, face, x2, y1, z2, u2, v2);
                putVertex(builder, face, x1, y1, z2, u1, v2);
                putVertex(builder, face, x1, y1, z1, u1, v1);
                break;
            case UP:
                if (customUv == null)
                {
                    u1 = texture.getInterpolatedU( x1 * 16 );
                    v1 = texture.getInterpolatedV( z1 * 16 );
                    u2 = texture.getInterpolatedU( x2 * 16 );
                    v2 = texture.getInterpolatedV( z2 * 16 );
                }

                putVertex(builder, face, x1, y2, z1, u1, v1);
                putVertex(builder, face, x1, y2, z2, u1, v2);
                putVertex(builder, face, x2, y2, z2, u2, v2);
                putVertex(builder, face, x2, y2, z1, u2, v1);
                break;
            case NORTH:
                if (customUv == null)
                {
                    u1 = texture.getInterpolatedU( x1 * 16 );
                    v1 = texture.getInterpolatedV( 16 - y1 * 16 );
                    u2 = texture.getInterpolatedU( x2 * 16 );
                    v2 = texture.getInterpolatedV( 16 - y2 * 16 );
                }

                putVertex(builder, face, x2, y2, z1, u1, v2);
                putVertex(builder, face, x2, y1, z1, u1, v1);
                putVertex(builder, face, x1, y1, z1, u2, v1);
                putVertex(builder, face, x1, y2, z1, u2, v2);
                break;
            case SOUTH:
                if (customUv == null)
                {
                    u1 = texture.getInterpolatedU( x1 * 16 );
                    v1 = texture.getInterpolatedV( 16 - y1 * 16 );
                    u2 = texture.getInterpolatedU( x2 * 16 );
                    v2 = texture.getInterpolatedV( 16 - y2 * 16 );
                }

                putVertex(builder, face, x1, y2, z2, u1, v2);
                putVertex(builder, face, x1, y1, z2, u1, v1);
                putVertex(builder, face, x2, y1, z2, u2, v1);
                putVertex(builder, face, x2, y2, z2, u2, v2);
                break;
            case WEST:
                if (customUv == null)
                {
                    u1 = texture.getInterpolatedU( z1 * 16 );
                    v1 = texture.getInterpolatedV( 16 - y1 * 16 );
                    u2 = texture.getInterpolatedU( z2 * 16 );
                    v2 = texture.getInterpolatedV( 16 - y2 * 16 );
                }

                putVertex(builder, face, x1, y1, z1, u1, v1);
                putVertex(builder, face, x1, y1, z2, u2, v1);
                putVertex(builder, face, x1, y2, z2, u2, v2);
                putVertex(builder, face, x1, y2, z1, u1, v2);
                break;
            case EAST:
                if (customUv == null)
                {
                    u1 = texture.getInterpolatedU( z2 * 16 );
                    v1 = texture.getInterpolatedV( 16 - y1 * 16 );
                    u2 = texture.getInterpolatedU( z1 * 16 );
                    v2 = texture.getInterpolatedV( 16 - y2 * 16 );
                }

                putVertex(builder, face, x2, y2, z1, u1, v2);
                putVertex(builder, face, x2, y2, z2, u2, v2);
                putVertex(builder, face, x2, y1, z2, u2, v1);
                putVertex(builder, face, x2, y1, z1, u1, v1);
                break;
        }

        int[] vertexData = builder.build().getVertexData();
        output.add(new BakedQuad(vertexData, -1, face, texture, true, format));
    }

    private void putVertex(UnpackedBakedQuad.Builder builder, EnumFacing face, float x, float y, float z, float u, float v) {
        VertexFormat format = builder.getVertexFormat();

        for (int i = 0; i < format.getElementCount(); i++) {
            VertexFormatElement e = format.getElement(i);
            switch (e.getUsage()) {
                case POSITION:
                    builder.put(i, x, y, z);
                    break;
                case NORMAL:
                    builder.put(i,
                            face.getFrontOffsetX(),
                            face.getFrontOffsetY(),
                            face.getFrontOffsetZ());
                    break;
                case COLOR:
                    // Color format is RGBA
                    float r = (color >> 16 & 0xFF) / 255f;
                    float g = (color >> 8 & 0xFF) / 255f;
                    float b = (color & 0xFF) / 255f;
                    float a = (color >> 24 & 0xFF) / 255f;
                    builder.put(i, r, g, b, a);
                    break;
                case UV:
                    if (e.getIndex() == 0) {
                        builder.put(i, u, v);
                    } else {
                        // Force Brightness to 15, this is for full bright mode
                        // this vertex element will only be present in that case
                        final float lightMapU = (float) (15 * 0x20) / 0xFFFF;
                        final float lightMapV = (float) (15 * 0x20) / 0xFFFF;
                        builder.put(i, lightMapU, lightMapV);
                    }
                    break;
                default:
                    builder.put(i);
                    break;
            }
        }
    }

    public void setTexture(TextureAtlasSprite texture) {
        for (EnumFacing face : EnumFacing.values()) {
            textures.put(face, texture);
        }
    }

    public void setTextures(TextureAtlasSprite up, TextureAtlasSprite down, TextureAtlasSprite north, TextureAtlasSprite south, TextureAtlasSprite east, TextureAtlasSprite west) {
        textures.put(EnumFacing.UP, up);
        textures.put(EnumFacing.DOWN, down);
        textures.put(EnumFacing.NORTH, north);
        textures.put(EnumFacing.SOUTH, south);
        textures.put(EnumFacing.EAST, east);
        textures.put(EnumFacing.WEST, west);
    }

    public void setDrawFaces(EnumSet<EnumFacing> drawFaces) {
        this.drawFaces = drawFaces;
    }

    public void setColor(int color) {
        this.color = color;
    }

    /**
     * Sets the vertex color for future vertices to the given RGB value, and forces the alpha component to 255.
     */
    public void setColorRGB(int color) {
        setColor(color | 0xFF000000);
    }

    public void setRenderFullBright(boolean renderFullBright) {
        this.renderFullBright = renderFullBright;
    }

    public void setCustomUv( EnumFacing facing, float u1, float v1, float u2, float v2 )
    {
        customUv.put( facing, new Vector4f( u1, v1, u2, v2 ) );
    }

    public List<BakedQuad> getOutput() {
        return output;
    }
}
