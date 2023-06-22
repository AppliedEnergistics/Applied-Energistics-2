package appeng.client.guidebook.scene.gltf;

import appeng.client.guidebook.scene.CameraSettings;
import appeng.client.guidebook.scene.GuidebookLevelRenderer;
import appeng.client.guidebook.scene.GuidebookScene;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Image;
import de.javagl.jgltf.model.AccessorDatas;
import de.javagl.jgltf.model.ElementType;
import de.javagl.jgltf.model.GltfConstants;
import de.javagl.jgltf.model.ImageModel;
import de.javagl.jgltf.model.TextureModel;
import de.javagl.jgltf.model.impl.DefaultAccessorModel;
import de.javagl.jgltf.model.impl.DefaultBufferModel;
import de.javagl.jgltf.model.impl.DefaultBufferViewModel;
import de.javagl.jgltf.model.impl.DefaultCameraModel;
import de.javagl.jgltf.model.impl.DefaultCameraOrthographicModel;
import de.javagl.jgltf.model.impl.DefaultGltfModel;
import de.javagl.jgltf.model.impl.DefaultImageModel;
import de.javagl.jgltf.model.impl.DefaultMeshModel;
import de.javagl.jgltf.model.impl.DefaultMeshPrimitiveModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import de.javagl.jgltf.model.impl.DefaultSceneModel;
import de.javagl.jgltf.model.impl.DefaultTextureModel;
import de.javagl.jgltf.model.io.Buffers;
import de.javagl.jgltf.model.io.GltfWriter;
import de.javagl.jgltf.model.io.v2.GltfAssetWriterV2;
import de.javagl.jgltf.model.io.v2.GltfAssetsV2;
import de.javagl.jgltf.model.io.v2.GltfModelWriterV2;
import de.javagl.jgltf.model.v2.GltfCreatorV2;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.zip.GZIPOutputStream;

public class SceneGltfExporter {

    private final GuidebookScene scene;
    private final Path exportPath;
    private final Path assetsBase;

    private SceneGltfExporter(GuidebookScene scene, Path exportPath, Path assetsBase) {
        this.scene = scene;
        this.exportPath = exportPath;
        this.assetsBase = assetsBase;
    }

    public static void export(GuidebookScene scene, Path exportPath, Path assetsBase) {
        var exporter = new SceneGltfExporter(scene, exportPath, assetsBase);
        exporter.export();
    }
    
    public void export() {
        var level = scene.getLevel();

        var gltfModel = new DefaultGltfModel();
        var cameraModel = createCameraModel(scene.getCameraSettings());
        gltfModel.addCameraModel(cameraModel);

        var sceneModel = new DefaultSceneModel();
        gltfModel.addSceneModel(sceneModel);

        var materials = new IdentityHashMap<RenderType, MaterialModelV2>();
        var samplers = new HashMap<SamplerKey, DefaultTextureModel>();
        var images = new HashMap<Integer, DefaultImageModel>();

        var bufferSource = new MultiBufferSource.BufferSource(new BufferBuilder(256), Map.of()) {
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
                    var material = buildMaterial(renderType, materials, samplers, images);

                    addMesh(buffer, gltfModel, sceneModel, material);

                    buffer.release();
                }

                if (bl) {
                    this.lastState = Optional.empty();
                }
            }
        };

        GuidebookLevelRenderer.getInstance().render(
                level,
                scene.getCameraSettings(),
                bufferSource,
                scene.getInWorldAnnotations()
        );

        materials.values().forEach(gltfModel::addMaterialModel);
        samplers.values().forEach(gltfModel::addTextureModel);
        images.values().forEach(gltfModel::addImageModel);

        // Print the glTF to the console.
        var actualImages = gltfModel.getImageModels();
        gltfModel.clearImageModels();
        var gltf = GltfAssetsV2.createBinary(gltfModel);
        for (ImageModel actualImage : actualImages) {
            var image = new Image();
            image.setName(actualImage.getName());
            image.setExtensions(actualImage.getExtensions());
            image.setUri(actualImage.getUri());
            gltf.getGltf().addImages(image);
        }

        var gltfWriter = new GltfAssetWriterV2();
        try (var out = new GZIPOutputStream(Files.newOutputStream(exportPath))) {
            gltfWriter.writeBinary(gltf, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MaterialModelV2 buildMaterial(RenderType renderType,
                                                 Map<RenderType, MaterialModelV2> materials,
                                                 Map<SamplerKey, DefaultTextureModel> samplers,
                                                 Map<Integer, DefaultImageModel> images) {
        var mat = materials.get(renderType);
        if (mat != null) {
            return mat;
        }

        mat = new MaterialModelV2();

        // Alpha testing is done in the shaders, so we cannot get that state from the GL pipeline
        if (renderType == RenderType.cutout() || renderType == RenderType.cutoutMipped()) {
            mat.setAlphaMode(MaterialModelV2.AlphaMode.MASK);
            mat.setAlphaCutoff(0.5f);
        }

        renderType.setupRenderState();

        // Handle texture
        var textureId = RenderSystem.getShaderTexture(0);
        RenderSystem.bindTexture(textureId);
        var minFilter = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER);
        var magFilter = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER);
        var wrapS = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S);
        var wrapT = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T);
        var samplerKey = new SamplerKey(textureId, minFilter, magFilter, wrapS, wrapT);
        mat.setBaseColorTexture(getOrCreateSampler(samplerKey, samplers, images));
        renderType.clearRenderState();

        materials.put(renderType, mat);
        return mat;
    }

    private TextureModel getOrCreateSampler(SamplerKey samplerKey,
                                                   Map<SamplerKey, DefaultTextureModel> samplers,
                                                   Map<Integer, DefaultImageModel> images) {
        var textureModel = samplers.get(samplerKey);
        if (textureModel != null) {
            return textureModel;
        }

        textureModel = new DefaultTextureModel();
        textureModel.setMinFilter(samplerKey.minFilter);
        textureModel.setMagFilter(samplerKey.magFilter);
        textureModel.setWrapS(samplerKey.wrapS);
        textureModel.setWrapT(samplerKey.wrapT);
        textureModel.setImageModel(getOrCreateImage(samplerKey.glTextureId, images));
        samplers.put(samplerKey, textureModel);
        return textureModel;
    }

    private ImageModel getOrCreateImage(int glTextureId, Map<Integer, DefaultImageModel> images) {
        var image = images.get(glTextureId);
        if (image != null) {
            return image;
        }

        image = new DefaultImageModel();

        RenderSystem.bindTexture(glTextureId);
        int w, h;
        int[] intResult = new int[1];
        GL11.glGetTexLevelParameteriv(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH, intResult);
        w = intResult[0];
        GL11.glGetTexLevelParameteriv(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT, intResult);
        h = intResult[0];

        var outputPath = assetsBase.resolve("texture_" + glTextureId + ".png");

        try (var nativeImage = new NativeImage(w, h, false)) {
            nativeImage.downloadTexture(0, false);
            ByteBuffer imageBuffer = ByteBuffer.wrap(nativeImage.asByteArray());
            image.setImageData(imageBuffer);
            nativeImage.writeToFile(outputPath);

            var relativeUrl = exportPath.getParent().relativize(outputPath).toString().replace('\\', '/');
            image.setUri(relativeUrl);
//            image.setUri(createDataUri(imageBuffer, "image/png"));
            image.setMimeType("image/png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        images.put(glTextureId, image);
        return image;
    }

    private record SamplerKey(int glTextureId, int minFilter, int magFilter, int wrapS, int wrapT) {
    }

    private DefaultBufferModel createEmbeddedBuffer(ByteBuffer buffer) {
        var model = new DefaultBufferModel();
        model.setBufferData(buffer);
        model.setUri(createDataUri(buffer, "application/gltf-buffer"));
        return model;
    }

    private String createDataUri(ByteBuffer buffer, String contentType) {
        var pos = buffer.position();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        String encodedData = Base64.getEncoder().encodeToString(data);
        String dataUriString = "data:" + contentType + ";base64," + encodedData;
        buffer.position(pos);
        return dataUriString;
    }

    private void addMesh(BufferBuilder.RenderedBuffer buffer, DefaultGltfModel gltfModel, DefaultSceneModel sceneModel, MaterialModelV2 material) {
        var drawState = buffer.drawState();
        var mode = drawState.mode();

        // Add raw buffer data for vertices
        var vdbuf = createEmbeddedBuffer(buffer.vertexBuffer());

        gltfModel.addBufferModel(vdbuf);

        var vertexView = new DefaultBufferViewModel(GltfConstants.GL_ARRAY_BUFFER);
        vertexView.setBufferModel(vdbuf);
        vertexView.setByteOffset(0);
        vertexView.setByteLength(drawState.vertexBufferEnd() - drawState.vertexBufferStart());
        vertexView.setByteStride(drawState.format().getVertexSize());
        gltfModel.addBufferViewModel(vertexView);

        var primitiveMeshModel = new DefaultMeshPrimitiveModel(switch (mode) {
            // LINES/LINE_STRIP will not work correctly, since they use a custom shader
            case LINES, DEBUG_LINES -> GltfConstants.GL_LINES;
            case LINE_STRIP, DEBUG_LINE_STRIP -> GltfConstants.GL_LINE_STRIP;
            case QUADS, TRIANGLES -> GltfConstants.GL_TRIANGLES;
            case TRIANGLE_STRIP -> GltfConstants.GL_TRIANGLE_STRIP;
            case TRIANGLE_FAN -> GltfConstants.GL_TRIANGLE_FAN;
        });
        primitiveMeshModel.setMaterialModel(material);

        // Handle index buffer
        var idxBuffer = buffer.indexBuffer();
        ByteBuffer effectiveIndices;
        var indexType = drawState.indexType();
        var indexCount = drawState.indexCount();

        // Auto-generated indices
        if (drawState.sequentialIndex()) {
            var generated = generateSequentialIndices(
                    mode,
                    drawState.vertexCount(),
                    drawState.indexCount()
            );
            effectiveIndices = generated.data;
            indexType = generated.type;
            indexCount = generated.indexCount();
        } else if (indexType == VertexFormat.IndexType.SHORT) {
            // Convert quads -> triangles
            if (mode == VertexFormat.Mode.QUADS) {
                var idxShortBuffer = idxBuffer.asShortBuffer();
                var triIndices = ShortBuffer.allocate(idxShortBuffer.remaining() * 2);
                while (idxShortBuffer.hasRemaining()) {
                    short one = idxShortBuffer.get();
                    short two = idxShortBuffer.get();
                    short three = idxShortBuffer.get();
                    short four = idxShortBuffer.get();

                    triIndices.put(one);
                    triIndices.put(two);
                    triIndices.put(three);

                    triIndices.put(three);
                    triIndices.put(four);
                    triIndices.put(one);
                }
                triIndices.flip();

                effectiveIndices = Buffers.createByteBufferFrom(triIndices);
            } else {
                effectiveIndices = idxBuffer;
            }
        } else if (indexType == VertexFormat.IndexType.INT) {
            // Convert quads -> triangles
            if (mode == VertexFormat.Mode.QUADS) {
                var idxIntBuffer = idxBuffer.asIntBuffer();
                var triIndices = IntBuffer.allocate(idxIntBuffer.remaining() * 2);
                while (idxIntBuffer.hasRemaining()) {
                    var one = idxIntBuffer.get();
                    var two = idxIntBuffer.get();
                    var three = idxIntBuffer.get();
                    var four = idxIntBuffer.get();

                    triIndices.put(one);
                    triIndices.put(two);
                    triIndices.put(three);

                    triIndices.put(three);
                    triIndices.put(four);
                    triIndices.put(one);
                }
                triIndices.flip();
                effectiveIndices = Buffers.createByteBufferFrom(triIndices);
            } else {
                effectiveIndices = idxBuffer;
            }
        } else {
            throw new RuntimeException("Unknown index type: " + indexType);
        }

        // Add raw buffer data for indices
        var ibuf = createEmbeddedBuffer(effectiveIndices);
        gltfModel.addBufferModel(ibuf);

        var indexView = new DefaultBufferViewModel(GltfConstants.GL_ELEMENT_ARRAY_BUFFER);
        indexView.setBufferModel(ibuf);
        indexView.setByteOffset(0);
        indexView.setByteLength(effectiveIndices.remaining());
        gltfModel.addBufferViewModel(indexView);

        var indexAccessor = new DefaultAccessorModel(
                indexType.asGLType,
                indexCount,
                ElementType.SCALAR
        );
        indexAccessor.setBufferViewModel(indexView);
        indexAccessor.setAccessorData(AccessorDatas.create(indexAccessor, effectiveIndices));
        gltfModel.addAccessorModel(indexAccessor);
        primitiveMeshModel.setIndices(indexAccessor);

        // Add buffer views and accessors for each vertex attribute
        var nextOffset = 0;
        for (var element : drawState.format().getElements()) {
            var elementOffset = nextOffset;
            nextOffset += element.getByteSize();

            // We can ignore anything other than these attributes, since custom shaders aren't supported
            if (element.getUsage() != VertexFormatElement.Usage.POSITION
                    && element.getUsage() != VertexFormatElement.Usage.NORMAL
                    && element.getUsage() != VertexFormatElement.Usage.COLOR
                    && element.getUsage() != VertexFormatElement.Usage.UV
                    || element.getIndex() != 0) {
                continue;
            }

            var elType = switch (element.getUsage()) {
                case POSITION -> ElementType.VEC3;
                case NORMAL -> ElementType.VEC3;
                case COLOR -> element.getCount() == 3 ? ElementType.VEC3 : ElementType.VEC4;
                case UV -> ElementType.VEC2;
                default -> null;
            };
            if (elType == null) {
                continue;
            }

            var accessor = new DefaultAccessorModel(
                    element.getType().getGlType(),
                    drawState.vertexCount(),
                    elType
            );
            accessor.setBufferViewModel(vertexView);
            accessor.setByteOffset(elementOffset);
            accessor.setNormalized(
                    element.getUsage() == VertexFormatElement.Usage.NORMAL || element.getUsage() == VertexFormatElement.Usage.COLOR
            );
            accessor.setByteStride(drawState.format().getVertexSize());

            // Needed to compute MIN/MAX for GLTF
            accessor.setAccessorData(AccessorDatas.create(accessor, vertexView.getBufferViewData()));

            var attributeName = switch (element.getUsage()) {
                case POSITION -> "POSITION";
                case NORMAL -> "NORMAL";
                case COLOR -> "COLOR_" + element.getIndex();
                case UV -> "TEXCOORD_" + element.getIndex();
                default -> null;
            };

            if (attributeName != null) {
                gltfModel.addAccessorModel(accessor);
                primitiveMeshModel.putAttribute(attributeName, accessor);
            }
        }

        var meshModel = new DefaultMeshModel();
        meshModel.addMeshPrimitiveModel(primitiveMeshModel);
        gltfModel.addMeshModel(meshModel);
        var node = new DefaultNodeModel();
        node.addMeshModel(meshModel);
        float[] matrix = new float[16];
        RenderSystem.getModelViewStack().last().pose().get(matrix);
        node.setMatrix(matrix);
        sceneModel.addNode(node);
        gltfModel.addNodeModel(node);
    }

    private GeneratedIndexBuffer generateSequentialIndices(VertexFormat.Mode mode, int vertexCount, int expectedIndexCount) {
        var indicesPerPrimitive = switch (mode) {
            case LINES -> 2;
            case DEBUG_LINES -> 2;
            case TRIANGLES -> 3;
            case QUADS -> 6;
            default -> throw new UnsupportedOperationException();
        };
        var verticesPerPrimitive = switch (mode) {
            case LINES -> 2;
            case DEBUG_LINES -> 2;
            case TRIANGLES -> 3;
            case QUADS -> 4;
            default -> throw new UnsupportedOperationException();
        };
        var primitives = vertexCount / verticesPerPrimitive;
        var indexCount = primitives * indicesPerPrimitive;
        if (indexCount != expectedIndexCount) {
            throw new RuntimeException("Would generate " + indexCount + " but MC expected " + expectedIndexCount);
        }

        var indexType = VertexFormat.IndexType.least(indexCount);
        var buffer = ByteBuffer.allocate(indexType.bytes * indexCount).order(ByteOrder.nativeOrder());

        IntConsumer indexConsumer;
        if (indexType == VertexFormat.IndexType.SHORT) {
            indexConsumer = value -> buffer.putShort((short) value);
        } else {
            indexConsumer = buffer::putInt;
        }

        for (var i = 0; i < vertexCount; i += verticesPerPrimitive) {
            switch (mode) {
                case QUADS -> {
                    indexConsumer.accept(i + 0);
                    indexConsumer.accept(i + 1);
                    indexConsumer.accept(i + 2);
                    indexConsumer.accept(i + 2);
                    indexConsumer.accept(i + 3);
                    indexConsumer.accept(i + 0);
                }
                default -> IntStream.range(0, indexCount).forEach(indexConsumer);
            }
        }

        buffer.flip();

        return new GeneratedIndexBuffer(indexType, buffer, indexCount);
    }

    record GeneratedIndexBuffer(VertexFormat.IndexType type, ByteBuffer data, int indexCount) {
    }

    private DefaultCameraModel createCameraModel(CameraSettings cameraSettings) {
        var cameraModel = new DefaultCameraModel();
        var orthoModel = new DefaultCameraOrthographicModel();
        orthoModel.setXmag(1000f);
        orthoModel.setYmag(1000f);
        orthoModel.setZnear(0f);
        orthoModel.setZfar(3000f);
        cameraModel.setCameraOrthographicModel(orthoModel);
        return cameraModel;
    }
}
