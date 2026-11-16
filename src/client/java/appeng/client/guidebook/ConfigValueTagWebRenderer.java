package appeng.client.guidebook;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import guideme.web.CustomElementWebRenderer;
import guideme.web.CustomElementWebRenderingContext;
import guideme.web.html.HtmlNode;

public class ConfigValueTagWebRenderer implements CustomElementWebRenderer {
    @Override
    public Set<String> getTagNames() {
        return Set.of("ae2:ConfigValue");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void render(CustomElementWebRenderingContext context, Consumer<HtmlNode> output) {
        var configValueName = context.element().getAttributeString("name", "");
        if (configValueName.isEmpty()) {
            output.accept(context.compileError("name is required"));
            return;
        }

        // GuideME maps the config values of exports made before mod data existed to the same key
        var exportedConfigValues = (Map<String, String>) context.guide()
                .getExtraData(AEAdditionalExportData.DEFAULT_CONFIG_VALUES.toString());
        if (exportedConfigValues == null) {
            output.accept(context.compileError("no configuration values were exported"));
            return;
        }
        var configValue = exportedConfigValues.get(configValueName);
        if (configValue == null) {
            output.accept(context.compileError("unknown configuration value"));
            return;
        }

        output.accept(HtmlNode.text(configValue));
    }
}
