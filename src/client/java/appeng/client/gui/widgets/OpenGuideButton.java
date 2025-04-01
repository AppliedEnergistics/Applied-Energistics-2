package appeng.client.gui.widgets;

import java.util.List;

import net.minecraft.network.chat.Component;

import appeng.util.Icon;
import appeng.core.localization.ButtonToolTips;

public class OpenGuideButton extends IconButton {
    public OpenGuideButton(OnPress onPress) {
        super(onPress);
    }

    @Override
    public List<Component> getTooltipMessage() {
        return List.of(
                ButtonToolTips.OpenGuide.text(),
                ButtonToolTips.OpenGuideDetail.text());
    }

    @Override
    protected Icon getIcon() {
        return Icon.HELP;
    }
}
