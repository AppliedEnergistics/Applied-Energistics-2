package appeng.client.gui.widgets;

import appeng.client.gui.Icon;
import appeng.core.localization.ButtonToolTips;
import net.minecraft.network.chat.Component;

import java.util.List;

public class OpenGuideButton extends IconButton {
    public OpenGuideButton(OnPress onPress) {
        super(onPress);
    }

    @Override
    public List<Component> getTooltipMessage() {
        return List.of(
                ButtonToolTips.OpenGuide.text(),
                ButtonToolTips.OpenGuideDetail.text()
        );
    }

    @Override
    protected Icon getIcon() {
        return Icon.HELP;
    }
}
