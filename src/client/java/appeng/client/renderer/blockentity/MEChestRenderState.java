package appeng.client.renderer.blockentity;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;

public class MEChestRenderState extends ChestOrDriveRenderState {
    public final BlockModelRenderState cellModel = new BlockModelRenderState();
    public int frontLightCoords;
}
