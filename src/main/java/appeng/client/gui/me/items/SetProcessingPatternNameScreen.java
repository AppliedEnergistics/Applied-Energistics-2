package appeng.client.gui.me.items;

import java.util.function.Consumer;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.widgets.TabButton;
import appeng.core.localization.GuiText;
import appeng.menu.me.items.PatternEncodingTermMenu;

/**
 * Allows precisely setting the item name to use for a processing pattern slot.
 * <p/>
 * Note that this is a sub-screen of {@link PatternEncodingTermScreen}
 */
public class SetProcessingPatternNameScreen<C extends PatternEncodingTermMenu>
        extends AESubScreen<C, PatternEncodingTermScreen<C>> {

    private final GenericStack currentStack;

    private final Consumer<GenericStack> setter;

    private final EditBox name;

    public SetProcessingPatternNameScreen(PatternEncodingTermScreen<C> parent, GenericStack currentStack,
            Consumer<GenericStack> setter) {
        super(parent, "/screens/set_processing_pattern_name.json");

        this.currentStack = currentStack;
        this.setter = setter;

        var itemStack = currentStack.what().wrapForDisplayOrFilter();

        this.name = widgets.addTextField("name");
        this.name.setValue(itemStack.getHoverName().getString());

        this.name.setMaxLength(32);

        widgets.addButton("save", GuiText.Set.text(), this::setName);

        var icon = getMenu().getHost().getMainMenuIcon();
        var button = new TabButton(icon, icon.getHoverName(), btn -> returnToParent());
        widgets.add("back", button);
    }

    private void setName() {
        String newName = name.getValue();
        var itemStack = currentStack.what().wrapForDisplayOrFilter();
        itemStack.setHoverName(Component.literal(newName));
        setter.accept(GenericStack.fromItemStack(itemStack));
        returnToParent();
    }
}
