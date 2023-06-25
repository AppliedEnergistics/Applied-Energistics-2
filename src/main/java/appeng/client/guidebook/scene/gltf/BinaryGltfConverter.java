package appeng.client.guidebook.scene.gltf;

import de.javagl.jgltf.impl.v2.Buffer;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.model.BufferModel;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.Optionals;
import de.javagl.jgltf.model.io.Buffers;
import de.javagl.jgltf.model.io.v2.GltfAssetV2;
import de.javagl.jgltf.model.v2.GltfCreatorV2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapted class from jgltf to NOT embed images, greatly simplifying the class.
 */
final class BinaryGltfConverter {
    private BinaryGltfConverter() {
    }

    static GltfAssetV2 create(GltfModel gltfModel) {
        GlTF outputGltf = GltfCreatorV2.create(gltfModel);

        // Create the new byte buffer for the data of the "binary_glTF" Buffer
        int binaryGltfBufferSize =
                computeBinaryGltfBufferSize(gltfModel);
        ByteBuffer binaryGltfByteBuffer =
                Buffers.create(binaryGltfBufferSize);

        // Create the binary Buffer,
        Buffer binaryGltfBuffer = new Buffer();
        binaryGltfBuffer.setByteLength(binaryGltfBufferSize);
        outputGltf.setBuffers(Collections.singletonList(binaryGltfBuffer));

        // Place the data from buffers and images into the new binary glTF
        // buffer. The mappings from IDs to offsets inside the resulting
        // buffer will be used to compute the offsets for the buffer views
        List<ByteBuffer> bufferDatas =
                gltfModel.getBufferModels().stream()
                        .map(BufferModel::getBufferData)
                        .collect(Collectors.toList());
        Map<Integer, Integer> bufferOffsets = concatBuffers(
                bufferDatas, binaryGltfByteBuffer);
        binaryGltfByteBuffer.position(0);

        // For all existing BufferViews, create new ones that are updated to
        // refer to the new binary glTF buffer, with the appropriate offset
        for (int i = 0; i < outputGltf.getBufferViews().size(); i++) {
            var bufferView = outputGltf.getBufferViews().get(i);

            Integer oldBufferIndex = bufferView.getBuffer();
            bufferView.setBuffer(0);
            int oldByteOffset = Optionals.of(bufferView.getByteOffset(), 0);
            int bufferOffset = bufferOffsets.get(oldBufferIndex);
            int newByteOffset = oldByteOffset + bufferOffset;
            bufferView.setByteOffset(newByteOffset);
        }

        return new GltfAssetV2(outputGltf, binaryGltfByteBuffer);
    }

    private static int computeBinaryGltfBufferSize(GltfModel gltfModel) {
        int binaryGltfBufferSize = 0;
        for (BufferModel bufferModel : gltfModel.getBufferModels()) {
            ByteBuffer bufferData = bufferModel.getBufferData();
            binaryGltfBufferSize += bufferData.capacity();
        }
        return binaryGltfBufferSize;
    }

    private static Map<Integer, Integer> concatBuffers(
            List<? extends ByteBuffer> buffers, ByteBuffer targetBuffer) {
        Map<Integer, Integer> offsets = new LinkedHashMap<>();
        for (int i = 0; i < buffers.size(); i++) {
            ByteBuffer oldByteBuffer = buffers.get(i);
            int offset = targetBuffer.position();
            offsets.put(i, offset);
            targetBuffer.put(oldByteBuffer.slice());
        }
        return offsets;
    }

}
