package appeng.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.input.Keyboard;

import static appeng.client.ClientHelper.KEY_CATEGORY;


public enum KeyBindings {
    WT(new KeyBinding("open_wireless_terminal", KeyConflictContext.UNIVERSAL, KeyModifier.SHIFT, Keyboard.KEY_T, KEY_CATEGORY)),
    WCT(new KeyBinding("open_wireless_crafting_terminal", KeyConflictContext.UNIVERSAL, KeyModifier.SHIFT, Keyboard.KEY_E, KEY_CATEGORY)),
    WPT(new KeyBinding("open_wireless_pattern_terminal", KeyConflictContext.UNIVERSAL, KeyModifier.SHIFT, Keyboard.KEY_R, KEY_CATEGORY)),
    WFT(new KeyBinding("open_wireless_fluid_terminal", KeyConflictContext.UNIVERSAL, KeyModifier.SHIFT, Keyboard.KEY_F, KEY_CATEGORY));

    private KeyBinding keyBinding;

    KeyBindings(KeyBinding keyBinding) {
        this.keyBinding = keyBinding;
    }

    ;

    public KeyBinding getKeyBinding() {
        return keyBinding;
    }
}
