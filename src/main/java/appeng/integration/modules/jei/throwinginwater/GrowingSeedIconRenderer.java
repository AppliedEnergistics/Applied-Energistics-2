package appeng.integration.modules.jei.throwinginwater;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;

/**
 * A renderer that cycles through a list of item stacks representing the growth stages of crystals.
 */
public class GrowingSeedIconRenderer implements Renderer {
    private final List<EntryStack<ItemStack>> stages;
    private int blitOffset;
    private long nextFrame;
    private int currentStage;

    public GrowingSeedIconRenderer(List<ItemStack> stages) {
        this.stages = stages.stream().map(EntryStacks::of).toList();
    }

    @Override
    public int getZ() {
        return blitOffset;
    }

    @Override
    public void setZ(int z) {
        blitOffset = z;
    }

    @Override
    public void render(PoseStack poseStack, Rectangle rectangle, int mouseX, int mouseY, float delta) {
        var now = Util.getMillis();
        if (now > nextFrame + 2000) {
            currentStage++;
            nextFrame = now;
        }

        if (currentStage >= stages.size()) {
            currentStage = 0;
        }

        stages.get(currentStage).render(poseStack, rectangle, mouseX, mouseY, delta);
    }
}
