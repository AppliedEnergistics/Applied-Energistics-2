package appeng.client.renderer.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SkyStoneTankRenderState extends BlockEntityRenderState {
    TextureAtlasSprite sprite;
    int color;
    boolean lighterThanAir;
    float fill;
}
