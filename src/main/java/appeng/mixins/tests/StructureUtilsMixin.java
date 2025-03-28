package appeng.mixins.tests;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;

import appeng.server.testplots.TestPlots;
import appeng.server.testworld.GameTestPlotAdapter;

@Mixin(value = StructureUtils.class, priority = 0)
public abstract class StructureUtilsMixin {
    @Inject(method = "prepareTestStructure", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;get(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;"), cancellable = true)
    private static void prepareTestStructure(GameTestInfo testInfo, BlockPos pos, Rotation rotation, ServerLevel level,
            CallbackInfoReturnable<StructureBlockEntity> cri) {
//    TODO 1.21.5    var id = ResourceLocation.tryParse(testInfo.getStructureName());
//        if (id == null) {
//            return;
//        }

//        var testPlot = TestPlots.getById(id);
//        if (testPlot == null) {
//            return;
//        }

//        cri.setReturnValue(GameTestPlotAdapter.createStructure(testPlot, testInfo, pos, level));
    }
}
