package appeng.client.render.cablebus;

import java.util.List;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;

public record FacadeItemLayer(RenderType renderType, List<BakedQuad> quads) {
}
