package appeng.client.guidebook.scene.annotation;

import org.joml.Vector3f;

import net.minecraft.client.Minecraft;

import appeng.client.guidebook.color.ColorValue;
import appeng.client.guidebook.color.ConstantColor;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.render.RenderContext;
import appeng.client.guidebook.scene.GuidebookScene;
import appeng.core.AppEng;

public class DiamondAnnotation extends OverlayAnnotation {
    private final Vector3f pos;
    private final ColorValue outerColor;
    private final ColorValue color;

    public DiamondAnnotation(Vector3f pos, ColorValue color) {
        this.pos = pos;
        this.color = color;
        this.outerColor = new ConstantColor(0xFFCCCCCC);
    }

    public Vector3f getPos() {
        return pos;
    }

    public ColorValue getColor() {
        return color;
    }

    @Override
    public LytRect getBoundingRect(GuidebookScene scene, LytRect viewport) {
        var screenPos = scene.worldToScreen(pos.x, pos.y, pos.z);
        var docPoint = scene.screenToDocument(viewport, screenPos);
        var x = Math.round(docPoint.x());
        var y = Math.round(docPoint.y());
        return new LytRect(x - 8, y - 8, 16, 16);
    }

    @Override
    public void render(GuidebookScene scene, RenderContext context, LytRect viewport) {
        var rect = getBoundingRect(scene, viewport);

        var texture = Minecraft.getInstance().getTextureManager()
                .getTexture(AppEng.makeId("textures/guide/diamond.png"));
        context.fillTexturedRect(rect, texture, outerColor, outerColor, outerColor, outerColor, 0, 0, 0.5f, 1);
        context.fillTexturedRect(rect, texture, color, color, color, color, 0.5f, 0, 1f, 1);
    }
}
