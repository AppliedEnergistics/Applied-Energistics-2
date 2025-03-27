package appeng.mixins.tests;

import java.util.Optional;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import appeng.core.AppEng;
import appeng.server.testplots.TestPlots;
import appeng.server.testworld.GameTestPlotAdapter;

@Mixin(value = TestInstanceBlockEntity.class, priority = 0)
public abstract class TestInstanceBlockEntityMixin {
    @Shadow
    public abstract BlockPos getStartCorner();

    @Shadow
    private TestInstanceBlockEntity.Data data;

    @WrapMethod(method = "getStructureTemplate")
    private static Optional<StructureTemplate> getStructureTemplate(ServerLevel level,
            ResourceKey<GameTestInstance> testInstance, Operation<Optional<StructureTemplate>> operation) {
        if (AppEng.MOD_ID.equals(testInstance.location().getNamespace())) {
            var testPlot = TestPlots.getById(testInstance.location());
            if (testPlot != null) {
                return GameTestPlotAdapter.createStructure(level, testPlot);
            }
        }

        return operation.call(level, testInstance);
    }

    @Inject(method = "placeStructure(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;)V", at = @At("TAIL"))
    private void placeStructure(ServerLevel level, StructureTemplate template, CallbackInfo ci) {
        var test = data.test();
        if (test.isPresent()) {
            var testId = test.get().location();
            if (AppEng.MOD_ID.equals(testId.getNamespace())) {
                var testPlot = TestPlots.getById(testId);
                if (testPlot != null) {
                    GameTestPlotAdapter.placeStructure(level, getStartCorner(), testPlot);
                }
            }
        }
    }
}
