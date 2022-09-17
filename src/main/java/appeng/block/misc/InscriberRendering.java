package appeng.block.misc;


import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.tesr.InscriberTESR;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class InscriberRendering extends BlockRenderingCustomizer {

    @SideOnly(Side.CLIENT)
    @Override
    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
        rendering.tesr(new InscriberTESR());
    }

}
