package appeng.client.renderer.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class MolecularAssemblerRenderState extends BlockEntityRenderState {
    ItemStackRenderState item = new ItemStackRenderState();
    boolean blockItem;
}
