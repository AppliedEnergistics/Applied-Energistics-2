package appeng.client.renderer.blockentity;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.block.model.BlockModelPart;

public class MEChestRenderState extends ChestOrDriveRenderState {
    public @Nullable BlockModelPart cellModel;
    public int frontLightCoords;
}
