package appeng.mixins.tests;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import appeng.server.testworld.GameTestPlotAdapter;

@Mixin(value = StructureUtils.class, priority = 0)
public class StructureUtilsMixin {

    @Inject(method = "getStructureTemplate", at = @At("HEAD"), cancellable = true)
    private static void getStructureTemplate(String structureName, ServerLevel serverLevel,
            CallbackInfoReturnable<StructureTemplate> cri) {
        var template = GameTestPlotAdapter.getStructureTemplate(structureName);
        if (template != null) {
            cri.setReturnValue(template);
        }
    }

    @Inject(method = "createStructureBlock", at = @At("RETURN"))
    private static void createStructureBlock(String structureName, BlockPos pos, Rotation rotation,
            ServerLevel serverLevel, boolean bl, CallbackInfoReturnable<StructureBlockEntity> cri) {
        GameTestPlotAdapter.createStructure(cri.getReturnValue());
    }
}
