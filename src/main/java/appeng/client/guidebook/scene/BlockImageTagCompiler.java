package appeng.client.guidebook.scene;

import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.tags.BlockTagCompiler;
import appeng.client.guidebook.compiler.tags.MdxAttrs;
import appeng.client.guidebook.document.block.LytBlockContainer;
import appeng.core.definitions.AEItems;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import net.minecraft.core.BlockPos;

public class BlockImageTagCompiler extends BlockTagCompiler {
    @Override
    protected void compile(PageCompiler compiler, LytBlockContainer parent, MdxJsxElementFields el) {
        var pair = MdxAttrs.getRequiredBlockAndId(compiler, parent, el, "id");

        var scene = new LytScene();
        var level = scene.getLevel();
        level.setBlock(BlockPos.ZERO, pair.getRight().defaultBlockState(), 3);
        var be = (InscriberBlockEntity) level.getBlockEntity(BlockPos.ZERO);
        be.getInternalInventory().setItemDirect(0, AEItems.SILICON_PRESS.stack());
        be.getInternalInventory().setItemDirect(1, AEItems.SILICON.stack());
        be.setSmash(true);
        parent.append(scene);
    }
}
