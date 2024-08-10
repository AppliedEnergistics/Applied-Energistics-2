package appeng.client.gui.assets;

import appeng.core.AEConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.ResourceLocation;

/**
 * Asset management
 */
public final class GuiAssets {
    /**
     * @see net.minecraft.client.gui.GuiSpriteManager
     */
    public static final ResourceLocation GUI_SPRITE_ATLAS = ResourceLocation.withDefaultNamespace("textures/atlas/gui.png");

    private GuiAssets() {
    }

    public static NineSliceSprite getNineSliceSprite(ResourceLocation id) {
        var guiSprites = Minecraft.getInstance().getGuiSprites();
        var sprite = guiSprites.getSprite(id);
        if (!(guiSprites.getSpriteScaling(sprite) instanceof GuiSpriteScaling.NineSlice nineSlice)) {
            throw new IllegalStateException("Expected sprite " + id + " to be a nine-slice sprite!");
        }

        var border = nineSlice.border();
        // Compute the delimiting U values *in the atlas* for the three slices.
        var u0 = sprite.getU0();
        var u1 = sprite.getU(border.left() / (float) nineSlice.width());
        var u2 = sprite.getU(1 - border.right() / (float) nineSlice.width());
        var u3 = sprite.getU1();
        // Compute the delimiting V values *in the atlas* for the three slices.
        var v0 = sprite.getV0();
        var v1 = sprite.getV(border.top() / (float) nineSlice.height());
        var v2 = sprite.getV(1 - border.bottom() / (float) nineSlice.height());
        var v3 = sprite.getV1();

        return new NineSliceSprite(
                sprite.atlasLocation(),
                border,
                new float[]{u0, u1, u2, u3, v0, v1, v2, v3});
    }

    /**
     * @param uv First 4 U values delimiting the horizontal slices, then 4 V values delimiting the vertical slices.
     *           These values refer to the atlas.
     */
    public record NineSliceSprite(ResourceLocation atlasLocation,
                                  GuiSpriteScaling.NineSlice.Border border,
                                  float[] uv) {
    }
}
