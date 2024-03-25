package appeng.client.guidebook.extensions;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.tags.FlowTagCompiler;
import appeng.client.guidebook.document.flow.LytFlowParent;
import appeng.core.AEConfig;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

/**
 * Provides access to AE2 config values in guide content.
 */
public class ConfigValueTagExtension extends FlowTagCompiler {
    public static final Map<String, Supplier<String>> CONFIG_VALUES = Map.of(
            "crystalResonanceGeneratorRate",
            () -> String.valueOf(AEConfig.instance().getCrystalResonanceGeneratorRate()));

    @Override
    public Set<String> getTagNames() {
        return Set.of("ae2:ConfigValue");
    }

    @Override
    protected void compile(PageCompiler compiler, LytFlowParent parent, MdxJsxElementFields el) {
        var configValueName = el.getAttributeString("name", "");
        if (configValueName.isEmpty()) {
            parent.appendError(compiler, "name is required", el);
            return;
        }

        var configValueSupplier = CONFIG_VALUES.get(configValueName);
        if (configValueSupplier == null) {
            parent.appendError(compiler, "unknown configuration value", el);
            return;
        }

        parent.appendText(configValueSupplier.get());
    }
}
