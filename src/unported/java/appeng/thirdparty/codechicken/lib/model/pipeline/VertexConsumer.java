package appeng.thirdparty.codechicken.lib.model.pipeline;

import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

public interface VertexConsumer {
    VertexFormat getVertexFormat();
    void setQuadTint(int tint);
    void setQuadOrientation(Direction orientation);
    void setApplyDiffuseLighting(boolean diffuse);
    void setTexture(Sprite texture);
    void put(int element, float... data);
}
