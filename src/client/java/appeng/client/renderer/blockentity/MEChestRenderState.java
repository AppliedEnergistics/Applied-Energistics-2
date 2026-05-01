package appeng.client.renderer.blockentity;

import net.minecraft.client.renderer.block.BlockModelRenderState;

public class MEChestRenderState extends ChestOrDriveRenderState {
    public final BlockModelRenderState cellModel = new BlockModelRenderState();
    public int frontLightCoords;
}
