package appeng.libs.micromark.html;

import appeng.libs.micromark.Token;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class HtmlExtension {
    @FunctionalInterface
    public interface Handler {
        void handle(HtmlContext context, Token token);
    }

    @FunctionalInterface
    public interface DocumentHandler {
        void handle(HtmlContext context);
    }

    @Nullable
    public final DocumentHandler enterDocument;
    @Nullable
    public final DocumentHandler exitDocument;
    public final Map<String, Handler> enter;
    public final Map<String, Handler> exit;

    private HtmlExtension(@Nullable DocumentHandler enterDocument,
                          @Nullable DocumentHandler exitDocument,
                          Map<String, Handler> enter,
                          Map<String, Handler> exit) {
        this.enterDocument = enterDocument;
        this.exitDocument = exitDocument;
        this.enter = enter;
        this.exit = exit;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DocumentHandler enterDocument;
        private DocumentHandler exitDocument;
        private final Map<String, Handler> enter = new HashMap<>();
        private final Map<String, Handler> exit = new HashMap<>();

        private Builder() {
        }

        public Builder enterDocument(DocumentHandler handler) {
            enterDocument = handler;
            return this;
        }

        public Builder exitDocument(DocumentHandler handler) {
            exitDocument = handler;
            return this;
        }

        public Builder enter(String type, Handler handler) {
            enter.put(type, handler);
            return this;
        }

        public Builder enter(String type, Runnable handler) {
            enter.put(type, (context, token) -> handler.run());
            return this;
        }

        public Builder exit(String type, Handler handler) {
            exit.put(type, handler);
            return this;
        }

        public Builder exit(String type, Runnable handler) {
            exit.put(type, (context, token) -> handler.run());
            return this;
        }

        public Builder addAll(HtmlExtension extension) {
            enter.putAll(extension.enter);
            exit.putAll(extension.exit);
            return this;
        }

        public HtmlExtension build() {
            return new HtmlExtension(enterDocument, exitDocument, enter, exit);
        }
    }
}
