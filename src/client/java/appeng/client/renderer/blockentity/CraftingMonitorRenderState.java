package appeng.client.renderer.blockentity;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

import appeng.api.orientation.BlockOrientation;
import appeng.api.stacks.GenericStack;

public class CraftingMonitorRenderState extends BlockEntityRenderState {
    public BlockOrientation orientation;
    public @Nullable GenericStack jobProgress;
    public int textColor;
}
