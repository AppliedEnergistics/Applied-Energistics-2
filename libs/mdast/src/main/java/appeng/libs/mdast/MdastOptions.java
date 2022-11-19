package appeng.libs.mdast;

import appeng.libs.micromark.Extension;
import appeng.libs.micromark.html.ParseOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MdastOptions extends ParseOptions {
    public final List<MdastExtension> mdastExtensions = new ArrayList<>();

    @Override
    public MdastOptions withSyntaxExtension(Extension extension) {
        super.withSyntaxExtension(extension);
        return this;
    }

    @Override
    public MdastOptions withSyntaxExtension(Consumer<Extension> customizer) {
        super.withSyntaxExtension(customizer);
        return this;
    }

    public MdastOptions withMdastExtension(MdastExtension extension) {
        mdastExtensions.add(extension);
        return this;
    }
}
