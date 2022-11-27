package appeng.libs.micromark.html;

import appeng.libs.micromark.Extension;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CompileOptions {
    private List<HtmlExtension> extensions = new ArrayList<>();

    @Nullable
    private String defaultLineEnding;

    private boolean allowDangerousHtml;

    private boolean allowDangerousProtocol;

    public List<HtmlExtension> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<HtmlExtension> extensions) {
        this.extensions = extensions;
    }

    public String getDefaultLineEnding() {
        return defaultLineEnding;
    }

    public void setDefaultLineEnding(String defaultLineEnding) {
        this.defaultLineEnding = defaultLineEnding;
    }

    public boolean isAllowDangerousHtml() {
        return allowDangerousHtml;
    }

    public void setAllowDangerousHtml(boolean allowDangerousHtml) {
        this.allowDangerousHtml = allowDangerousHtml;
    }

    public boolean isAllowDangerousProtocol() {
        return allowDangerousProtocol;
    }

    public void setAllowDangerousProtocol(boolean allowDangerousProtocol) {
        this.allowDangerousProtocol = allowDangerousProtocol;
    }

    public CompileOptions allowDangerousHtml() {
        this.allowDangerousHtml = true;
        return this;
    }

    public CompileOptions allowDangerousProtocol() {
        this.allowDangerousProtocol = true;
        return this;
    }

    public CompileOptions withExtension(HtmlExtension extension) {
        extensions.add(extension);
        return this;
    }
}
