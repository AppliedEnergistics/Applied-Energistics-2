package appeng.client.guidebook.scene.export;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.RenderType;

/**
 * Captured rendering data.
 */
record Mesh(BufferBuilder.DrawState drawState,
        ByteBuffer vertexBuffer,
        ByteBuffer indexBuffer,
        RenderType renderType) {
}
