package appeng.libs.micromark.html;

import appeng.libs.micromark.Extension;

import java.util.ArrayList;
import java.util.List;

public class ParseOptions {
    private final List<Extension> extensions = new ArrayList<>();

    public List<Extension> getExtensions() {
        return extensions;
    }

    public ParseOptions withExtension(Extension extension) {
        this.extensions.add(extension);
        return this;
    }
}
