package appeng.client.guidebook.scene.element;

import net.minecraft.nbt.CompoundTag;

import appeng.api.implementations.parts.ICablePart;
import appeng.api.parts.IPartItem;
import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.tags.MdxAttrs;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.client.guidebook.scene.GuidebookScene;
import appeng.core.definitions.AEBlocks;
import appeng.hooks.VisualStateSaving;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

public class CableElementCompiler implements SceneElementTagCompiler {
    @Override
    public void compile(GuidebookScene scene,
            PageCompiler compiler,
            LytErrorSink errorSink,
            MdxJsxElementFields el) {
        var pair = MdxAttrs.getRequiredItemAndId(compiler, errorSink, el, "id");
        if (pair == null) {
            return;
        }

        // Must be a part item
        if (!(pair.getRight() instanceof IPartItem<?>partItem)) {
            errorSink.appendError(compiler, "Must be a part item", el);
            return;
        }

        // And also a cable
        var part = partItem.createPart();
        if (!(part instanceof ICablePart)) {
            errorSink.appendError(compiler, "Must be a cable part", el);
            return;
        }

        var pos = MdxAttrs.getPos(compiler, errorSink, el);
        scene.getLevel().setBlockAndUpdate(pos, AEBlocks.CABLE_BUS.block().defaultBlockState());
        var be = scene.getLevel().getBlockEntity(pos);
        if (be == null) {
            errorSink.appendError(compiler, "BE failed to place", el);
            return;
        }

        VisualStateSaving.setEnabled(true);
        try {
            var data = be.saveWithoutMetadata();
            var cableNbt = new CompoundTag();
            cableNbt.putString("id", pair.getKey().toString());
            data.put("cable", cableNbt);
            be.load(data);
        } finally {
            VisualStateSaving.setEnabled(false);
        }
    }
}
