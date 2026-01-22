package appeng.client.render;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;

@FunctionalInterface
public interface BakedQuadSink {
    void add(@Nullable Direction cullFace, BakedQuad quad);
}
