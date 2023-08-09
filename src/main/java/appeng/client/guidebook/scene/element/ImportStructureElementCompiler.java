package appeng.client.guidebook.scene.element;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

import com.google.common.io.ByteStreams;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import appeng.client.guidebook.compiler.IdUtils;
import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.client.guidebook.scene.GuidebookScene;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

/**
 * Imports a structure into the scene.
 */
public class ImportStructureElementCompiler implements SceneElementTagCompiler {
    @Override
    public Set<String> getTagNames() {
        return Set.of("ImportStructure");
    }

    @Override
    public void compile(GuidebookScene scene,
            PageCompiler compiler,
            LytErrorSink errorSink,
            MdxJsxElementFields el) {
        var structureSrc = el.getAttributeString("src", null);
        if (structureSrc == null) {
            errorSink.appendError(compiler, "Missing src attribute", el);
            return;
        }

        ResourceLocation absStructureSrc;
        try {
            absStructureSrc = IdUtils.resolveLink(structureSrc, compiler.getPageId());
        } catch (ResourceLocationException e) {
            errorSink.appendError(compiler, "Invalid structure path: " + structureSrc, el);
            return;
        }

        var structureNbtData = compiler.loadAsset(absStructureSrc);
        if (structureNbtData == null) {
            errorSink.appendError(compiler, "Missing structure file", el);
            return;
        }

        CompoundTag compoundTag;
        try {
            if (absStructureSrc.getPath().toLowerCase(Locale.ROOT).endsWith(".snbt")) {
                compoundTag = NbtUtils.snbtToStructure(
                        new String(structureNbtData, StandardCharsets.UTF_8));
            } else {
                compoundTag = NbtIo.read(ByteStreams.newDataInput(structureNbtData));
            }
        } catch (Exception e) {
            errorSink.appendError(compiler, "Couldn't read structure: " + e.getMessage(), el);
            return;
        }

        var template = new StructureTemplate();
        var blocks = scene.getLevel().registryAccess().registryOrThrow(Registries.BLOCK).asLookup();
        template.load(blocks, compoundTag);
        var random = new SingleThreadedRandomSource(0L);
        var settings = new StructurePlaceSettings();
        settings.setIgnoreEntities(true); // Entities need a server level in structures

        var fakeServerLevel = new FakeForwardingServerLevel(scene.getLevel());
        if (!template.placeInWorld(fakeServerLevel, BlockPos.ZERO, BlockPos.ZERO, settings, random, 0)) {
            errorSink.appendError(compiler, "Placed to fail structure", el);
        }
    }
}
