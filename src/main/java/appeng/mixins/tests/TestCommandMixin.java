package appeng.mixins.tests;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.gametest.framework.TestCommand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import appeng.server.testplots.TestPlots;

@Mixin(TestCommand.class)
public class TestCommandMixin {

    @Inject(method = "verifyStructureExists", at = @At("HEAD"), cancellable = true)
    private static void verifyStructureExists(ServerLevel level, String structureName,
            CallbackInfoReturnable<Boolean> cri) {
        var testPlot = TestPlots.getById(ResourceLocation.parse(structureName));
        if (testPlot != null) {
            cri.setReturnValue(true);
        }
    }

}
