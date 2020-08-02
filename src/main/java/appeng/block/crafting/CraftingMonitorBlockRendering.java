package appeng.block.crafting;

import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.crafting.MonitorBakedModel;
import appeng.client.render.model.AutoRotatingBakedModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.Identifier;

public class CraftingMonitorBlockRendering extends BlockRenderingCustomizer {

    @Override
    @Environment(EnvType.CLIENT)
    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
        rendering.renderType(RenderLayer.getCutout());
        rendering.modelCustomizer(CraftingMonitorBlockRendering::customizeModel);
    }

    @Environment(EnvType.CLIENT)
    private static BakedModel customizeModel(Identifier path, BakedModel model) {
        // The formed model handles rotations itself, the unformed one does not
        if (model instanceof MonitorBakedModel) {
            return model;
        }
        return new AutoRotatingBakedModel(model);
    }

}
