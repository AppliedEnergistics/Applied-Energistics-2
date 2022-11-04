package appeng.integration.modules.jei;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.world.level.ItemLike;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;

/**
 * A renderer that cycles through a list of item stacks.
 */
public class CyclingDrawable implements IDrawable {
    private final List<IDrawable> stages;
    private long nextFrame;
    private int currentStage;

    public CyclingDrawable(List<IDrawable> stages) {
        this.stages = stages;
    }

    public static CyclingDrawable forItems(IGuiHelper guiHelper, ItemLike... items) {
        return new CyclingDrawable(Arrays.stream(items)
                .map(i -> i.asItem().getDefaultInstance())
                .map(s -> guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, s))
                .toList());
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
