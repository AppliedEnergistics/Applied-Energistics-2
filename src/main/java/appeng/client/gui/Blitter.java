package appeng.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;

import appeng.core.AppEng;

/**
 * Utility class for drawing rectangular textures in the UI.
 */
public final class Blitter {

    private static final ResourceLocation ICON_SPRITESHEET = new ResourceLocation(AppEng.MOD_ID,
            "textures/guis/states.png");

    // This assumption is obviously bogus, but currently all textures are this size,
    // and it's impossible to get the texture size from an already loaded texture.
    // The coordinates will still be correct when a resource pack provides bigger textures as long
    // as each texture element is still positioned at the same relative position
    public static final int DEFAULT_TEXTURE_WIDTH = 256;
    public static final int DEFAULT_TEXTURE_HEIGHT = 256;

    private final ResourceLocation texture;
    // This texture size is only used to convert the source rectangle into uv coordinates (which are [0,1] and work
    // with textures of any size at runtime).
    private final int referenceWidth;
    private final int referenceHeight;
    private int r = 255;
    private int g = 255;
    private int b = 255;
    private int a = 255;
    private Rectangle2d srcRect;
    private Rectangle2d destRect = new Rectangle2d(0, 0, 0, 0);

    private Blitter(ResourceLocation texture, int referenceWidth, int referenceHeight) {
        this.texture = texture;
        this.referenceWidth = referenceWidth;
        this.referenceHeight = referenceHeight;
    }

    /**
     * Creates a blitter where the source rectangle is in relation to a 256x256 pixel texture.
     */
    public static Blitter texture(String file) {
        return texture(file, DEFAULT_TEXTURE_WIDTH, DEFAULT_TEXTURE_HEIGHT);
    }

    /**
     * Creates a blitter where the source rectangle is in relation to a texture of the given size.
     */
    public static Blitter texture(String file, int referenceWidth, int referenceHeight) {
        return new Blitter(new ResourceLocation(AppEng.MOD_ID, "textures/" + file), referenceWidth, referenceHeight);
    }

    /**
     * Blits an icon from the icon sprite-sheet
     *
     * @param iconIndex The index of the icon in the sprite-sheet, starting at 0, in right-then-down order.
     */
    public static Blitter icon(int iconIndex) {
        // The icon sprite-sheet is 16x16 icons of 16x16 pixels
        int iconY = iconIndex / 16;
        int iconX = iconIndex - iconY * 16;

        return new Blitter(ICON_SPRITESHEET, DEFAULT_TEXTURE_WIDTH, DEFAULT_TEXTURE_HEIGHT)
                .src(iconX * 16, iconY * 16, 16, 16);
    }

    public Blitter copy() {
        Blitter result = new Blitter(texture, referenceWidth, referenceHeight);
        result.srcRect = srcRect;
        result.destRect = destRect;
        result.r = r;
        result.g = g;
        result.b = b;
        result.a = a;
        return result;
    }

    public int getSrcWidth() {
        return srcRect == null ? destRect.getWidth() : srcRect.getWidth();
    }

    public int getSrcHeight() {
        return srcRect == null ? destRect.getHeight() : srcRect.getHeight();
    }

    /**
     * Use the given rectangle from the texture (in pixels assuming a 256x256 texture size).
     */
    public Blitter src(int x, int y, int w, int h) {
        this.srcRect = new Rectangle2d(x, y, w, h);
        return this;
    }

    /**
     * Use the given rectangle from the texture (in pixels assuming a 256x256 texture size).
     */
    public Blitter src(Rectangle2d rect) {
        return src(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    /**
     * Draw into the rectangle defined by the given coordinates.
     */
    public Blitter dest(int x, int y, int w, int h) {
        this.destRect = new Rectangle2d(x, y, w, h);
        return this;
    }

    /**
     * Draw at the given x,y coordinate and use the source rectangle size as the destination rectangle size.
     */
    public Blitter dest(int x, int y) {
        return dest(x, y, 0, 0);
    }

    /**
     * Draw into the given rectangle.
     */
    public Blitter dest(Rectangle2d rect) {
        return dest(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public Rectangle2d getDestRect() {
        int x = destRect.getX();
        int y = destRect.getY();
        int w = 0, h = 0;
        if (destRect.getWidth() != 0 && destRect.getHeight() != 0) {
            w = destRect.getWidth();
            h = destRect.getHeight();
        } else if (srcRect != null) {
            w = srcRect.getWidth();
            h = srcRect.getHeight();
        }
        return new Rectangle2d(x, y, w, h);
    }

    public Blitter color(float r, float g, float b) {
        this.r = (int) (MathHelper.clamp(r, 0, 1) * 255);
        this.g = (int) (MathHelper.clamp(g, 0, 1) * 255);
        this.b = (int) (MathHelper.clamp(b, 0, 1) * 255);
        return this;
    }

    public Blitter opacity(float a) {
        this.a = (int) (MathHelper.clamp(a, 0, 1) * 255);
        return this;
    }

    public Blitter color(float r, float g, float b, float a) {
        return color(r, g, b).opacity(a);
    }

    public void blit(MatrixStack matrices, int zIndex) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        textureManager.bindTexture(this.texture);

        // With no source rectangle, we'll use the entirety of the texture. This happens rarely though.
        float minU, minV, maxU, maxV;
        if (srcRect == null) {
            minU = minV = 0;
            maxU = maxV = 1;
        } else {
            minU = srcRect.getX() / (float) referenceWidth;
            minV = srcRect.getY() / (float) referenceHeight;
            maxU = (srcRect.getX() + srcRect.getWidth()) / (float) referenceWidth;
            maxV = (srcRect.getY() + srcRect.getHeight()) / (float) referenceHeight;
        }

        // It's possible to not set a destination rectangle size, in which case the
        // source rectangle size will be used
        float x1 = destRect.getX();
        float y1 = destRect.getY();
        float x2 = x1, y2 = y1;
        if (destRect.getWidth() != 0 && destRect.getHeight() != 0) {
            x2 += destRect.getWidth();
            y2 += destRect.getHeight();
        } else if (srcRect != null) {
            x2 += srcRect.getWidth();
            y2 += srcRect.getHeight();
        }

        Matrix4f matrix = matrices.getLast().getMatrix();

        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
        bufferbuilder.pos(matrix, x1, y2, (float) zIndex)
                .color(r, g, b, a)
                .tex(minU, maxV).endVertex();
        bufferbuilder.pos(matrix, x2, y2, (float) zIndex)
                .color(r, g, b, a)
                .tex(maxU, maxV).endVertex();
        bufferbuilder.pos(matrix, x2, y1, (float) zIndex)
                .color(r, g, b, a)
                .tex(maxU, minV).endVertex();
        bufferbuilder.pos(matrix, x1, y1, (float) zIndex)
                .color(r, g, b, a)
                .tex(minU, minV).endVertex();
        bufferbuilder.finishDrawing();

        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        WorldVertexBufferUploader.draw(bufferbuilder);
    }

}
