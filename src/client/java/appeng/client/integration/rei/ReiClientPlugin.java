package appeng.client.integration.rei;

import appeng.api.integrations.rei.IngredientConverters;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.InscriberScreen;
import appeng.integration.modules.itemlists.CompatLayerHelper;
import appeng.integration.modules.rei.InscriberRecipeCategory;
import dev.architectury.event.CompoundEventResult;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.client.renderer.Rect2i;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@REIPluginClient
public class ReiClientPlugin implements REIClientPlugin {
    @Override
    public void registerScreens(ScreenRegistry registry) {
        if (CompatLayerHelper.IS_LOADED) {
            return;
        }

        registry.registerDraggableStackVisitor(new GhostIngredientHandler());
        registry.registerFocusedStack((screen, mouse) -> {
            if (screen instanceof AEBaseScreen<?> aeScreen) {
                var stack = aeScreen.getStackUnderMouse(mouse.x, mouse.y);
                if (stack != null) {
                    for (var converter : IngredientConverters.getConverters()) {
                        var entryStack = converter.getIngredientFromStack(stack.stack());
                        if (entryStack != null) {
                            return CompoundEventResult.interruptTrue(entryStack);
                        }
                    }
                }
            }

            return CompoundEventResult.pass();
        });
        registry.registerContainerClickArea(
                new Rectangle(82, 39, 26, 16),
                InscriberScreen.class,
                InscriberRecipeCategory.ID);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        if (CompatLayerHelper.IS_LOADED) {
            return;
        }

        zones.register(AEBaseScreen.class, screen -> {
            return screen != null ? mapRects(screen.getExclusionZones()) : Collections.emptyList();
        });

    }

    private static List<Rectangle> mapRects(List<Rect2i> exclusionZones) {
        return exclusionZones.stream()
                .map(ez -> new Rectangle(ez.getX(), ez.getY(), ez.getWidth(), ez.getHeight()))
                .collect(Collectors.toList());
    }

}
