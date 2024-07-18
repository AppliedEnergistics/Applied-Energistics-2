package appeng.client.guidebook.scene.export;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * A buffer source we pass into the standard renderer to capture all rendered 3D data in buffers suitable for export.
 */
class MeshBuildingBufferSource extends MultiBufferSource.BufferSource {
    private final List<Mesh> meshes = new ArrayList<>();

    public MeshBuildingBufferSource() {
        super(new BufferBuilder(256), Map.of());
    }

    public List<Mesh> getMeshes() {
        return meshes;
    }

    @Override
    public void endBatch(RenderType renderType) {
        var bufferBuilder = this.fixedBuffers.getOrDefault(renderType, this.builder);
        boolean bl = Objects.equals(this.lastState, renderType.asOptional());
        if (!bl && bufferBuilder == this.builder) {
            return;
        }
        if (!this.startedBuffers.remove(bufferBuilder)) {
            return;
        }
        var buffer = bufferBuilder.endOrDiscardIfEmpty();
        if (buffer != null) {

            var drawState = buffer.drawState();

            var vbSource = buffer.vertexBuffer();
            var vertexBuffer = ByteBuffer.allocate(vbSource.remaining())
                    .order(ByteOrder.nativeOrder());
            vertexBuffer.put(vbSource);
            vertexBuffer.flip();

            var ibSource = buffer.indexBuffer();
            var indexBuffer = ByteBuffer.allocate(ibSource.remaining());
            indexBuffer.put(ibSource);
            indexBuffer.flip();

            this.meshes.add(new Mesh(
                    drawState,
                    vertexBuffer,
                    indexBuffer,
                    renderType));

            buffer.release();
        }

        if (bl) {
            this.lastState = Optional.empty();
        }
    }

}
