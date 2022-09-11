package appeng.integration.modules.jei.widgets;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;

import appeng.core.AppEng;

public final class WidgetFactory {
    private static final ResourceLocation TEXTURE = AppEng.makeId("textures/guis/jei.png");

    private final IGuiHelper guiHelper;
    private final IDrawableStatic unfilledArrow;
    private final IDrawableStatic filledArrow;
    private final LoadingCache<Integer, IDrawableAnimated> cachedArrows;

    public WidgetFactory(IJeiHelpers jeiHelpers) {
        this.guiHelper = jeiHelpers.getGuiHelper();
        this.unfilledArrow = guiHelper.createDrawable(TEXTURE, 0, 17, 24, 17);
        this.filledArrow = guiHelper.createDrawable(TEXTURE, 0, 0, 24, 17);
        this.cachedArrows = CacheBuilder.newBuilder()
                .maximumSize(25)
                .build(new CacheLoader<>() {
                    @Override
                    public IDrawableAnimated load(Integer cookTime) {
                        return guiHelper.createAnimatedDrawable(filledArrow, cookTime,
                                IDrawableAnimated.StartDirection.LEFT, false);
                    }
                });
    }

    public Label label(float x, float y, Component text) {
        return new Label(x, y, text);
    }

    public Widget item(int x, int y, ItemStack stack) {
        return new DrawableWidget(guiHelper.createDrawableItemStack(stack), x, y);
    }

    /**
     * An unfilled recipe arrow.
     */
    public Widget unfilledArrow(int x, int y) {
        return new DrawableWidget(unfilledArrow, x, y);
    }

    /**
     * Animated arrow that fills from the left in the given number of ticks.
     */
    public Widget fillingArrow(int x, int y, int ticks) {
        if (ticks <= 0) {
            return unfilledArrow(x, y);
        }
        return new DrawableWidget(this.cachedArrows.getUnchecked(ticks), x, y);
    }

}
