package appeng.integration.modules.jei.throwinginwater;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;

/**
 * A renderer that cycles through a list of item stacks representing the growth stages of crystals.
 */
public class GrowingSeedIconRenderer implements IDrawable {
    private final List<IDrawable> stages;
    private long nextFrame;
    private int currentStage;

    public GrowingSeedIconRenderer(IGuiHelper guiHelper, List<ItemStack> stages) {
        this.stages = stages.stream().map(guiHelper::createDrawableIngredient).toList();
    }

    @Override
    public void draw(PoseStack stack, int xOffset, int yOffset) {
        var now = Util.getMillis();
        if (now > nextFrame + 2000) {
            currentStage++;
            nextFrame = now;
        }

        if (currentStage >= stages.size()) {
            currentStage = 0;
        }

        stages.get(currentStage).draw(stack, xOffset, yOffset);
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }
}
