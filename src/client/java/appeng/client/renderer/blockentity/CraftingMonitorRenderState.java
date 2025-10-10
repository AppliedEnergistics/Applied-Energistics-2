package appeng.client.renderer.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.util.FormattedCharSequence;

import appeng.api.orientation.BlockOrientation;
import appeng.client.api.AEKeyRenderState;

public class CraftingMonitorRenderState extends BlockEntityRenderState {
    public BlockOrientation orientation;
    public final AEKeyRenderState what = new AEKeyRenderState();
    public FormattedCharSequence text;
    public int textColor;
    public int textWidth;
}
