package appeng.client.guidebook.scene.export;

import appeng.client.guidebook.document.LytSize;
import appeng.client.guidebook.scene.CameraSettings;
import appeng.client.guidebook.scene.GuidebookLevelRenderer;
import appeng.client.guidebook.scene.GuidebookScene;
import appeng.flatbuffers.scene.ExpCameraSettings;
import appeng.flatbuffers.scene.ExpDepthTest;
import appeng.flatbuffers.scene.ExpIndexElementType;
import appeng.flatbuffers.scene.ExpMaterial;
import appeng.flatbuffers.scene.ExpMesh;
import appeng.flatbuffers.scene.ExpPrimitiveType;
import appeng.flatbuffers.scene.ExpSampler;
import appeng.flatbuffers.scene.ExpScene;
import appeng.flatbuffers.scene.ExpTransparency;
import appeng.flatbuffers.scene.ExpVertexElementType;
import appeng.flatbuffers.scene.ExpVertexElementUsage;
import appeng.flatbuffers.scene.ExpVertexFormat;
import appeng.flatbuffers.scene.ExpVertexFormatElement;
import appeng.siteexport.OffScreenRenderer;
import appeng.siteexport.ResourceExporter;
import com.google.flatbuffers.FlatBufferBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexSorting;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPOutputStream;

/**
 * Exports a game scene 3d rendering to a custom 3d format for rendering it using WebGL in the browser. See scene.fbs
 * (we use FlatBuffers to encode the actual data).
 */
public class SceneExporter {
    private static final Logger LOG = LoggerFactory.getLogger(SceneExporter.class);

    private final ResourceExporter resourceExporter;

    public SceneExporter(ResourceExporter resourceExporter) {
        this.resourceExporter = resourceExporter;
    }

    public List<Path> exportAsImages(GuidebookScene scene, String exportName, LytSize baseSize, int... scales) throws IOException {
        if (scene == null) {
            return List.of();
        }

        var sprites = getSprites(scene);

        // Since block images are non-interactive and have no annotations, we just render them
        // ahead of time.
        var paths = new ArrayList<Path>();
        for (int scale : scales) {
            // We only scale the viewport, not scaling the view matrix means the scene will still fill it
            var width = Math.max(1, baseSize.width() * scale);
            var height = Math.max(1, baseSize.height() * scale);

            try (var renderer = new OffScreenRenderer(width, height)) {
                var path = resourceExporter.renderAndWrite(
                        renderer,
                        exportName + "@" + scale,
                        () -> {
                            var sceneRenderer = GuidebookLevelRenderer.getInstance();
                            scene.getCameraSettings().setViewportSize(baseSize);
                            sceneRenderer.render(scene.getLevel(), scene.getCameraSettings(), Collections.emptyList());
                        },
                        sprites,
                        true
                );
                paths.add(path);
            }
        }
        return paths;
    }

    private static Set<TextureAtlasSprite> getSprites(GuidebookScene scene) {
        var textureManager = Minecraft.getInstance().getTextureManager();

        var level = scene.getLevel();
        var bufferSource = new MeshBuildingBufferSource();
        GuidebookLevelRenderer.getInstance().renderContent(level, bufferSource);

        return bufferSource.getMeshes().stream()
                .flatMap(mesh -> mesh.getSprites(textureManager))
                .collect(Collectors.toSet());
    }

    public byte[] export(GuidebookScene scene) {
        var level = scene.getLevel();

        var bufferSource = new MeshBuildingBufferSource();

        // To avoid baking in the projection and camera, we need to reset these here
        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f(), VertexSorting.ORTHOGRAPHIC_Z);

        GuidebookLevelRenderer.getInstance().renderContent(level, bufferSource);

        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();

        // Concat all vertex buffers
        var builder = new FlatBufferBuilder(1024);
        var meshes = bufferSource.getMeshes();
        var vertexFormats = writeVertexFormats(meshes, builder);
        var materials = writeMaterials(meshes, builder);
        var meshesOffset = writeMeshes(meshes, builder, vertexFormats, materials);

        ExpScene.startExpScene(builder);
        ExpScene.addMeshes(builder, meshesOffset);
        var cameraOffset = createCameraModel(scene.getCameraSettings(), builder);
        ExpScene.addCamera(builder, cameraOffset);

        builder.finish(ExpScene.endExpScene(builder));

        var bout = new ByteArrayOutputStream();
        try (var out = new GZIPOutputStream(bout)) {
            out.write(builder.sizedByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return bout.toByteArray();
    }

    private Map<VertexFormat, Integer> writeVertexFormats(List<Mesh> meshes, FlatBufferBuilder builder) {
        var result = new IdentityHashMap<VertexFormat, Integer>();

        for (var mesh : meshes) {
            result.computeIfAbsent(mesh.drawState().format(), format -> writeVertexFormat(format, builder));
        }

        return result;
    }

    private int writeVertexFormat(VertexFormat format, FlatBufferBuilder builder) {

        // Count relevant vertex formats
        var count = (int) format.getElements().stream()
                .filter(SceneExporter::isRelevant)
                .count();

        ExpVertexFormat.startElementsVector(builder, count);

        // Vectors are written in reverse-order
        var elements = format.getElements();
        for (int i = elements.size() - 1; i >= 0; i--) {
            var offset = 0;
            for (int j = 0; j < i; j++) {
                offset += elements.get(j).getByteSize();
            }

            var element = elements.get(i);
            if (isRelevant(element)) {
                var normalized = element.getUsage() == VertexFormatElement.Usage.NORMAL
                        || element.getUsage() == VertexFormatElement.Usage.COLOR;

                ExpVertexFormatElement.createExpVertexFormatElement(
                        builder,
                        element.getIndex(),
                        mapType(element.getType()),
                        mapUsage(element.getUsage()),
                        element.getCount(),
                        offset,
                        element.getByteSize(),
                        normalized);
            }
        }
        var elementsOffset = builder.endVector();

        ExpVertexFormat.startExpVertexFormat(builder);
        ExpVertexFormat.addElements(builder, elementsOffset);
        ExpVertexFormat.addVertexSize(builder, format.getVertexSize());

        return ExpVertexFormat.endExpVertexFormat(builder);

    }

    private static boolean isRelevant(VertexFormatElement element) {
        return element.getUsage() != VertexFormatElement.Usage.PADDING
                && element.getUsage() != VertexFormatElement.Usage.GENERIC;
    }

    private Map<RenderType, Integer> writeMaterials(List<Mesh> meshes, FlatBufferBuilder builder) {
        var result = new IdentityHashMap<RenderType, Integer>();

        for (var mesh : meshes) {
            result.computeIfAbsent(mesh.renderType(), type -> writeMaterial(type, builder));
        }

        return result;
    }

    private int writeMaterial(RenderType type, FlatBufferBuilder builder) {

        var state = ((RenderType.CompositeRenderType) type).state();

        var shaderNameOffset = 0;
        if (state.shaderState.shader.isPresent()) {
            shaderNameOffset = builder.createSharedString(state.shaderState.shader.get().get().getName());
        }

        var nameOffset = builder.createSharedString(type.name);

        var disableCulling = state.cullState == RenderStateShard.NO_CULL;

        // Handle transparency
        var transparencyState = state.transparencyState;
        int transparency;
        if (transparencyState == RenderStateShard.NO_TRANSPARENCY) {
            transparency = ExpTransparency.DISABLED;
        } else if (transparencyState == RenderStateShard.ADDITIVE_TRANSPARENCY) {
            transparency = ExpTransparency.ADDITIVE;
        } else if (transparencyState == RenderStateShard.LIGHTNING_TRANSPARENCY) {
            transparency = ExpTransparency.LIGHTNING;
        } else if (transparencyState == RenderStateShard.GLINT_TRANSPARENCY) {
            transparency = ExpTransparency.GLINT;
        } else if (transparencyState == RenderStateShard.CRUMBLING_TRANSPARENCY) {
            transparency = ExpTransparency.CRUMBLING;
        } else if (transparencyState == RenderStateShard.TRANSLUCENT_TRANSPARENCY) {
            transparency = ExpTransparency.TRANSLUCENT;
        } else {
            LOG.warn("Cannot handle transparency state {} of render type {}", transparencyState, type);
            transparency = ExpTransparency.DISABLED;
        }

        // Handle depth-testing
        int depthTest;
        var depthTestShard = state.depthTestState;
        if (depthTestShard == RenderStateShard.NO_DEPTH_TEST) {
            depthTest = ExpDepthTest.DISABLED;
        } else if (depthTestShard == RenderStateShard.EQUAL_DEPTH_TEST) {
            depthTest = ExpDepthTest.EQUAL;
        } else if (depthTestShard == RenderStateShard.LEQUAL_DEPTH_TEST) {
            depthTest = ExpDepthTest.LEQUAL;
        } else if (depthTestShard == RenderStateShard.GREATER_DEPTH_TEST) {
            depthTest = ExpDepthTest.GREATER;
        } else {
            LOG.warn("Cannot handle depth-test state {} of render type {}", depthTestShard, type);
            depthTest = ExpDepthTest.DISABLED;
        }

        var samplersOffset = 0;
        var samplers = RenderTypeIntrospection.getSamplers(type);
        if (samplers.size() > 0) {
            var sampler = samplers.get(0);

            var texturePath = resourceExporter.exportTexture(sampler.texture());
            var textureOffset = builder.createSharedString(texturePath);

            var samplerOffset = ExpSampler.createExpSampler(builder, textureOffset, sampler.blur(), sampler.blur());
            samplersOffset = ExpMaterial.createSamplersVector(builder, new int[]{samplerOffset});
        }

        return ExpMaterial.createExpMaterial(
                builder,
                nameOffset,
                shaderNameOffset,
                disableCulling,
                transparency,
                depthTest,
                samplersOffset);

    }

    private static int mapMode(VertexFormat.Mode mode) {
        return switch (mode) {
            case LINES -> ExpPrimitiveType.LINES;
            case LINE_STRIP -> ExpPrimitiveType.LINE_STRIP;
            case DEBUG_LINES -> ExpPrimitiveType.DEBUG_LINES;
            case DEBUG_LINE_STRIP -> ExpPrimitiveType.DEBUG_LINE_STRIP;
            case QUADS, TRIANGLES -> ExpPrimitiveType.TRIANGLES;
            case TRIANGLE_STRIP -> ExpPrimitiveType.TRIANGLE_STRIP;
            case TRIANGLE_FAN -> ExpPrimitiveType.TRIANGLE_FAN;
        };
    }

    private static int mapUsage(VertexFormatElement.Usage usage) {
        return switch (usage) {
            case POSITION -> ExpVertexElementUsage.POSITION;
            case NORMAL -> ExpVertexElementUsage.NORMAL;
            case COLOR -> ExpVertexElementUsage.COLOR;
            case UV -> ExpVertexElementUsage.UV;
            case PADDING, GENERIC -> throw new IllegalStateException("Should have been skipped");
        };
    }

    private static int mapType(VertexFormatElement.Type type) {
        return switch (type) {
            case FLOAT -> ExpVertexElementType.FLOAT;
            case UBYTE -> ExpVertexElementType.UBYTE;
            case BYTE -> ExpVertexElementType.BYTE;
            case USHORT -> ExpVertexElementType.USHORT;
            case SHORT -> ExpVertexElementType.SHORT;
            case UINT -> ExpVertexElementType.UINT;
            case INT -> ExpVertexElementType.INT;
        };
    }

    private int writeMeshes(List<Mesh> meshes,
                            FlatBufferBuilder builder,
                            Map<VertexFormat, Integer> vertexFormats,
                            Map<RenderType, Integer> materials) {
        var writtenMeshes = new IntArrayList(meshes.size());

        for (var mesh : meshes) {
            int vb = ExpMesh.createVertexBufferVector(builder, mesh.vertexBuffer());
            var ibData = createIndexBuffer(mesh.drawState(), mesh.indexBuffer());
            int ib = ExpMesh.createIndexBufferVector(builder, ibData.data);

            ExpMesh.startExpMesh(builder);
            ExpMesh.addVertexBuffer(builder, vb);
            ExpMesh.addIndexBuffer(builder, ib);
            ExpMesh.addIndexType(builder, mapIndexType(ibData.indexType));
            ExpMesh.addIndexCount(builder, ibData.indexCount);
            ExpMesh.addMaterial(builder, materials.get(mesh.renderType()));
            ExpMesh.addVertexFormat(builder, vertexFormats.get(mesh.drawState().format()));
            ExpMesh.addPrimitiveType(builder, mapMode(mesh.drawState().mode()));
            writtenMeshes.add(ExpMesh.endExpMesh(builder));
        }

        return ExpScene.createMeshesVector(builder, writtenMeshes.elements());
    }

    private int mapIndexType(VertexFormat.IndexType indexType) {
        return switch (indexType) {
            case INT -> ExpIndexElementType.UINT;
            case SHORT -> ExpIndexElementType.USHORT;
        };
    }

    record IndexBufferAttributes(
            ByteBuffer data,
            VertexFormat.IndexType indexType,
            int indexCount) {
    }

    private IndexBufferAttributes createIndexBuffer(BufferBuilder.DrawState drawState, ByteBuffer idxBuffer) {
        // Handle index buffer
        ByteBuffer effectiveIndices;
        var indexType = drawState.indexType();
        var indexCount = drawState.indexCount();
        var mode = drawState.mode();

        // Auto-generated indices
        if (drawState.sequentialIndex()) {
            var generated = generateSequentialIndices(
                    mode,
                    drawState.vertexCount(),
                    drawState.indexCount());
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

                effectiveIndices = ByteBuffer.allocate(triIndices.remaining() * 2)
                        .order(ByteOrder.nativeOrder());
                while (triIndices.hasRemaining()) {
                    effectiveIndices.putShort(triIndices.get());
                }
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

                effectiveIndices = ByteBuffer.allocate(triIndices.remaining() * 4)
                        .order(ByteOrder.nativeOrder());
                while (triIndices.hasRemaining()) {
                    effectiveIndices.putInt(triIndices.get());
                }
            } else {
                effectiveIndices = idxBuffer;
            }
        } else {
            throw new RuntimeException("Unknown index type: " + indexType);
        }

        // Add raw buffer data for indices
        return new IndexBufferAttributes(effectiveIndices, indexType, indexCount);
    }

    private GeneratedIndexBuffer generateSequentialIndices(VertexFormat.Mode mode, int vertexCount,
                                                           int expectedIndexCount) {
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

    private int createCameraModel(CameraSettings cameraSettings, FlatBufferBuilder builder) {
        return ExpCameraSettings.createExpCameraSettings(
                builder,
                cameraSettings.getRotationY(),
                cameraSettings.getRotationX(),
                cameraSettings.getRotationZ(),
                cameraSettings.getZoom());
    }

}
