package appeng.integration.modules.emi;

import java.util.function.Consumer;

import net.minecraft.client.gui.screens.Screen;

import dev.emi.emi.api.EmiExclusionArea;
import dev.emi.emi.api.widget.Bounds;

import appeng.client.gui.AEBaseScreen;

class EmiAeBaseScreenExclusionZones implements EmiExclusionArea<Screen> {
    @Override
    public void addExclusionArea(Screen screen, Consumer<Bounds> consumer) {
        if (!(screen instanceof AEBaseScreen<?>aeScreen)) {
            return;
        }

        for (var zone : aeScreen.getExclusionZones()) {
            consumer.accept(new Bounds(zone.getX(), zone.getY(), zone.getWidth(), zone.getHeight()));
        }
    }
}
