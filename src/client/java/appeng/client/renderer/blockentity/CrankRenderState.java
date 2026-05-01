package appeng.client.renderer.blockentity;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

import appeng.api.orientation.BlockOrientation;

public class CrankRenderState extends BlockEntityRenderState {
    BlockModelRenderState modelRenderState = new BlockModelRenderState();
    BlockOrientation orientation;
    float visibleRotation;
}
