package appeng.client.gui.widgets;

import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import appeng.client.gui.Icon;
import appeng.hooks.HighlightHandler;

public class HighlightButton extends IconButton {

    private float multiplier;
    private Runnable successJob;
    private BlockPos pos;
    private ResourceKey<Level> dim;

    public HighlightButton() {
        super(HighlightButton::highlight);
    }

    public void setMultiplier(double val) {
        this.multiplier = (float) Math.min(10, Math.max(1, val));
    }

    public void setSuccessJob(Runnable process) {
        this.successJob = process;
    }

    public void setTarget(BlockPos pos, ResourceKey<Level> world) {
        this.pos = pos;
        this.dim = world;
    }

    private static void highlight(Button btn) {
        if (btn instanceof HighlightButton hb) {
            if (hb.dim != null && hb.pos != null) {
                HighlightHandler.highlight(hb.pos, hb.dim, System.currentTimeMillis() + (long) (600 * hb.multiplier));
                if (hb.successJob != null) {
                    hb.successJob.run();
                }
            }
        }
    }

    @Override
    protected Icon getIcon() {
        return Icon.HIGHLIGHT_BLOCK;
    }
}
