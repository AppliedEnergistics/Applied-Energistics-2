package appeng.mixins.tests;

import net.minecraft.commands.CommandSourceStack;
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
    private static void verifyStructureExists(CommandSourceStack sourceStack, ResourceLocation id, CallbackInfoReturnable<Boolean> cri) {
        var testPlot = TestPlots.getById(id);
        if (testPlot != null) {
            cri.setReturnValue(true);
        }
    }

}
