package appeng.client.guidebook.scene.export;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMaps;

/**
 * A buffer source we pass into the standard renderer to capture all rendered 3D data in buffers suitable for export.
 */
class MeshBuildingBufferSource extends MultiBufferSource.BufferSource {
    private final List<Mesh> meshes = new ArrayList<>();

    public MeshBuildingBufferSource() {
        super(new ByteBufferBuilder(786432), Object2ObjectSortedMaps.emptyMap());
    }

    public List<Mesh> getMeshes() {
        return meshes;
    }

    @Override
    public void endBatch(RenderType renderType) {
        var bufferBuilder = startedBuilders.remove(renderType);
        if (bufferBuilder == null) {
            return;
        }

        try (var buffer = bufferBuilder.build()) {
            if (buffer != null) {
                var drawState = buffer.drawState();

                var vbSource = buffer.vertexBuffer();
                var vertexBuffer = ByteBuffer.allocate(vbSource.remaining())
                        .order(ByteOrder.nativeOrder());
                vertexBuffer.put(vbSource);
                vertexBuffer.flip();

                // Copy the index buffer
                ByteBuffer indexBuffer = null;
                var ibSource = buffer.indexBuffer();
                if (ibSource != null) {
                    indexBuffer = ByteBuffer.allocate(ibSource.remaining());
                    indexBuffer.put(ibSource);
                    indexBuffer.flip();
                }

                this.meshes.add(new Mesh(
                        drawState,
                        vertexBuffer,
                        indexBuffer,
                        renderType));
            }
        }
    }

}
