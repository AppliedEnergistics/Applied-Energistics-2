package appeng.client.guidebook.render;

import java.io.File;
import java.io.IOException;

import com.mojang.blaze3d.platform.NativeImage;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import appeng.api.util.AEColor;
import appeng.client.guidebook.color.MutableColor;

class MutableColorTest {

    @Test
    @Disabled
    void testLighterDarkerSpread() throws IOException {
        var colors = AEColor.values();
        var percentages = new float[] { -30, -20, -10, 0, 10, 20, 30 };

        try (var img = new NativeImage(NativeImage.Format.RGBA, 50 * colors.length, percentages.length * 25, false)) {

            for (int i = 0; i < colors.length; i++) {
                var baseColor = MutableColor.ofArgb32(colors[i].mediumVariant);
                baseColor.setAlpha(1.0f);

                for (int y = 0; y < percentages.length; y++) {
                    var percentage = percentages[y];
                    MutableColor color = baseColor.copy();
                    if (percentage < 0) {
                        color.darker(-percentage);
                    } else if (percentage > 0) {
                        color.lighter(percentage);
                    }

                    for (var dx = 0; dx < 50; dx++) {
                        for (var dy = 0; dy < 25; dy++) {
                            img.setPixelRGBA(
                                    i * 50 + dx,
                                    y * 25 + dy,
                                    color.toAbgr32());
                        }
                    }
                }
            }

            img.writeToFile(new File("spread.png"));

        }
    }
}
