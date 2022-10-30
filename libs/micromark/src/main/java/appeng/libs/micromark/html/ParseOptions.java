package appeng.libs.micromark.html;

import appeng.libs.micromark.Extension;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ParseOptions {
    private final List<Extension> extensions = new ArrayList<>();

    public List<Extension> getExtensions() {
        return extensions;
    }

    public ParseOptions withExtension(Extension extension) {
        this.extensions.add(extension);
        return this;
    }

    public ParseOptions withExtension(Consumer<Extension> customizer) {
        var extension = new Extension();
        customizer.accept(extension);
        this.extensions.add(extension);
        return this;
    }
}
