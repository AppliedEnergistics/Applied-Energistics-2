package appeng.client.guidebook.document.block;

import appeng.client.gui.Icon;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.interaction.GuideTooltip;
import appeng.client.guidebook.document.interaction.InteractiveElement;
import appeng.client.guidebook.document.interaction.ItemTooltip;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.ColorRef;
import appeng.client.guidebook.render.RenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Renders a standard Minecraft GUI slot.
 */
public class LytSlot extends LytBlock implements InteractiveElement {
    private static final int ITEM_SIZE = 16;
    private static final int PADDING = 1;
    public static final int OUTER_SIZE = ITEM_SIZE + 2 * PADDING;
    private static final int CYCLE_TIME = 2000;

    private final ItemStack[] stacks;

    public LytSlot(Ingredient ingredient) {
        this.stacks = ingredient.getItems();
    }

    public LytSlot(ItemStack stack) {
        this.stacks = new ItemStack[]{stack};
    }

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        return new LytRect(x, y, OUTER_SIZE, OUTER_SIZE);
    }

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {

    }

    @Override
    public void render(RenderContext context) {
        var x = bounds.x();
        var y = bounds.y();

        context.drawIcon(x, y, Icon.SLOT_BACKGROUND, ColorRef.WHITE);

        var stack = getDisplayedStack();
        if (!stack.isEmpty()) {
            context.renderItem(stack, x + 1, y + 1, 1, ITEM_SIZE, ITEM_SIZE);
        }
    }

    @Override
    public Optional<GuideTooltip> getTooltip() {
        var stack = getDisplayedStack();
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ItemTooltip(stack));
    }

    private ItemStack getDisplayedStack() {
        if (stacks.length == 0) {
            return ItemStack.EMPTY;
        }

        var cycle = System.nanoTime() / TimeUnit.MILLISECONDS.toNanos(CYCLE_TIME);
        return stacks[(int) (cycle % stacks.length)];
    }
}
