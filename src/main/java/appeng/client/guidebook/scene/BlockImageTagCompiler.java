package appeng.client.guidebook.scene;

import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.tags.BlockTagCompiler;
import appeng.client.guidebook.compiler.tags.MdxAttrs;
import appeng.client.guidebook.document.block.LytBlockContainer;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import net.minecraft.core.BlockPos;

public class BlockImageTagCompiler extends BlockTagCompiler {
    @Override
    protected void compile(PageCompiler compiler, LytBlockContainer parent, MdxJsxElementFields el) {
        var pair = MdxAttrs.getRequiredBlockAndId(compiler, parent, el, "id");


        var scale = MdxAttrs.getFloat(compiler, parent, el, "scale", 1.0f);

        var scene = new LytScene();
        scene.setScale(scale);

        var level = scene.getLevel();
        level.setBlock(BlockPos.ZERO, pair.getRight().defaultBlockState(), 3);
        var be = (InscriberBlockEntity) level.getBlockEntity(BlockPos.ZERO);
        var inv = be.getInternalInventory();
        inv.setItemDirect(0, AEItems.SILICON_PRESS.stack());
        inv.setItemDirect(1, AEItems.SILICON_PRESS.stack());
        inv.setItemDirect(2, AEItems.SILICON_PRESS.stack());
        be.setSmash(true);
        be.setRepeatSmash(true);

        var chargerPos = BlockPos.ZERO.east().east();
        level.setBlockAndUpdate(chargerPos, AEBlocks.CHARGER.block().defaultBlockState());
        var charger = (ChargerBlockEntity) level.getBlockEntity(chargerPos);
        charger.getInternalInventory().setItemDirect(0, AEItems.WIRELESS_CRAFTING_TERMINAL.stack());

        parent.append(scene);
    }
}
