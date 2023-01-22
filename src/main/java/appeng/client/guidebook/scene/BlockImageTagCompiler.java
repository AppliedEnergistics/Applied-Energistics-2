package appeng.client.guidebook.scene;

import appeng.block.networking.ControllerBlock;
import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.tags.BlockTagCompiler;
import appeng.client.guidebook.compiler.tags.MdxAttrs;
import appeng.client.guidebook.document.block.LytBlockContainer;
import appeng.client.guidebook.scene.level.GuidebookLevel;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

public class BlockImageTagCompiler extends BlockTagCompiler {
    @Override
    protected void compile(PageCompiler compiler, LytBlockContainer parent, MdxJsxElementFields el) {
        var pair = MdxAttrs.getRequiredBlockAndId(compiler, parent, el, "id");


        var zoom = MdxAttrs.getFloat(compiler, parent, el, "zoom", 1.0f);

        var level = new GuidebookLevel();
        var cameraSettings = new CameraSettings();
        cameraSettings.setZoom(zoom);

        var scene = new GuidebookScene(level, cameraSettings);

        level.setBlock(BlockPos.ZERO, pair.getRight().defaultBlockState(), 3);
        var be = (InscriberBlockEntity) level.getBlockEntity(BlockPos.ZERO);
        var inv = be.getInternalInventory();
        inv.setItemDirect(0, AEItems.SILICON_PRESS.stack());
        inv.setItemDirect(1, AEItems.SILICON_PRESS.stack());
        inv.setItemDirect(2, AEItems.SILICON_PRESS.stack());
        be.setSmash(true);
        be.setRepeatSmash(true);

        var chargerPos = BlockPos.ZERO.east().east().east().east();
        level.setBlockAndUpdate(chargerPos, AEBlocks.CHARGER.block().defaultBlockState());
        var charger = (ChargerBlockEntity) level.getBlockEntity(chargerPos);
        charger.getInternalInventory().setItemDirect(0, AEItems.WIRELESS_CRAFTING_TERMINAL.stack());

        var controllerPos = BlockPos.ZERO.above().above().above();
        level.setBlockAndUpdate(controllerPos, AEBlocks.CONTROLLER.block().defaultBlockState()
                .setValue(ControllerBlock.CONTROLLER_STATE, ControllerBlock.ControllerBlockState.online));

        var drivePos = BlockPos.ZERO.north().north().north().west();
        level.setBlockAndUpdate(drivePos, AEBlocks.DRIVE.block().defaultBlockState());

        var tag = new CompoundTag();
        var visual = new CompoundTag();
        tag.put("visual", visual);

        visual.putBoolean("online", true);
        var cell0 = new CompoundTag();
        cell0.putString("id", AEItems.ITEM_CELL_1K.id().toString());
        cell0.putString("state", "NOT_EMPTY");
        visual.put("cell3", cell0);

        var drive = (DriveBlockEntity) level.getBlockEntity(drivePos);
        drive.load(tag);

        level.setBlock(BlockPos.ZERO.north(), Fluids.WATER.defaultFluidState().createLegacyBlock(), 11);

        var lytScene = new LytGuidebookScene();
        lytScene.setScene(scene);
        parent.append(lytScene);
    }
}
