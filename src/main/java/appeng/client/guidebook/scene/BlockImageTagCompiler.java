package appeng.client.guidebook.scene;

import net.minecraft.core.BlockPos;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.tags.BlockTagCompiler;
import appeng.client.guidebook.compiler.tags.MdxAttrs;
import appeng.client.guidebook.document.block.LytBlockContainer;
import appeng.client.guidebook.scene.level.GuidebookLevel;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

/**
 * Handles tags like <code>&lt;BlockImage id="mod:blockid" /&gt;</code> and renders a 3D block image in its place.
 */
public class BlockImageTagCompiler extends BlockTagCompiler {
    @Override
    protected void compile(PageCompiler compiler, LytBlockContainer parent, MdxJsxElementFields el) {
        var pair = MdxAttrs.getRequiredBlockAndId(compiler, parent, el, "id");
        if (pair == null) {
            return;
        }

        var scale = MdxAttrs.getFloat(compiler, parent, el, "scale", 1.0f);
        var perspective = MdxAttrs.getEnum(compiler, parent, el, "perspective", PerspectivePreset.ISOMETRIC_NORTH_EAST);
        if (perspective == null) {
            return;
        }

        var level = new GuidebookLevel();
        var cameraSettings = new CameraSettings();
        cameraSettings.setZoom(scale);
        cameraSettings.setPerspectivePreset(perspective);

        var scene = new GuidebookScene(level, cameraSettings);

        level.setBlockAndUpdate(BlockPos.ZERO, pair.getRight().defaultBlockState());

        var lytScene = new LytGuidebookScene();
        lytScene.setScene(scene);
        lytScene.setInteractive(false);
        parent.append(lytScene);
    }
}
