package appeng.block.qnb;


import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class QuantumBridgeRendering extends BlockRenderingCustomizer {

    @Override
    @SideOnly(Side.CLIENT)
    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
        rendering.builtInModel("models/block/qnb/qnb_formed", new QnbFormedModel());
        // Disable auto rotation
        rendering.modelCustomizer((location, model) -> model);
    }
}
