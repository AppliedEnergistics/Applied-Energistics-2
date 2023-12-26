package appeng.integration.modules.emi;

import net.minecraft.client.gui.screens.Screen;

import dev.emi.emi.api.EmiStackProvider;
import dev.emi.emi.api.stack.EmiStackInteraction;

import appeng.client.gui.AEBaseScreen;

class EmiAeBaseScreenStackProvider implements EmiStackProvider<Screen> {
    @Override
    public EmiStackInteraction getStackAt(Screen screen, int x, int y) {
        if (screen instanceof AEBaseScreen<?>aeScreen) {
            var stack = aeScreen.getStackUnderMouse(x, y);
            if (stack != null) {
                var emiStack = EmiStackHelper.toEmiStack(stack.stack());
                if (emiStack != null) {
                    return new EmiStackInteraction(emiStack);
                }
            }
        }
        return EmiStackInteraction.EMPTY;
    }
}
