package appeng.items.tools.powered;


import appeng.api.util.AEColor;
import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.client.render.model.ColorApplicatorModel;
import appeng.core.AppEng;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class ToolColorApplicatorRendering extends ItemRenderingCustomizer {

    private static final ModelResourceLocation MODEL_COLORED = new ModelResourceLocation(new ResourceLocation(AppEng.MOD_ID, "builtin/color_applicator_colored"), "inventory");
    private static final ModelResourceLocation MODEL_UNCOLORED = new ModelResourceLocation(new ResourceLocation(AppEng.MOD_ID, "color_applicator_uncolored"), "inventory");

    @Override
    @SideOnly(Side.CLIENT)
    public void customize(IItemRendering rendering) {
        rendering.builtInModel("models/item/builtin/color_applicator_colored", new ColorApplicatorModel());
        rendering.variants(MODEL_COLORED, MODEL_UNCOLORED);
        rendering.color(this::getColor);
        rendering.meshDefinition(this::getMesh);
    }

    private ModelResourceLocation getMesh(ItemStack itemStack) {
        // If the stack has no color, don't use the colored model since the impact of calling getColor for every quad is
        // extremely high,
        // if the stack tries to re-search its inventory for a new paintball everytime
        AEColor col = ((ToolColorApplicator) itemStack.getItem()).getActiveColor(itemStack);
        return (col != null) ? MODEL_COLORED : MODEL_UNCOLORED;
    }

    private int getColor(ItemStack itemStack, int idx) {
        if (idx == 0) {
            return -1;
        }

        final AEColor col = ((ToolColorApplicator) itemStack.getItem()).getActiveColor(itemStack);

        if (col == null) {
            return -1;
        }

        switch (idx) {
            case 1:
                return col.blackVariant;
            case 2:
                return col.mediumVariant;
            case 3:
                return col.whiteVariant;
            default:
                return -1;
        }
    }
}
