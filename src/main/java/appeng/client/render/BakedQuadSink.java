package appeng.client.render;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;

@FunctionalInterface
public interface BakedQuadSink {
    void add(@Nullable Direction cullFace, BakedQuad quad);
}
