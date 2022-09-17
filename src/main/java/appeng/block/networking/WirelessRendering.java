package appeng.block.networking;


import appeng.api.util.AEColor;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.StaticBlockColor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class WirelessRendering extends BlockRenderingCustomizer {
    @Override
    @SideOnly(Side.CLIENT)
    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
        rendering.blockColor(new StaticBlockColor(AEColor.TRANSPARENT));
    }
}
