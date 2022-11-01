package appeng.libs.mdast;

import appeng.libs.mdast.model.MdAstRoot;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.html.CompileContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An mdast extension changes how markdown tokens are turned into mdast.
 */
class MdastExtension {
    @FunctionalInterface
    interface Transform {
        MdAstRoot transform(MdAstRoot tree);
    }

    @FunctionalInterface
    interface Handler {
        void handle(MdastContext context, Token token);
    }

    public final List<String> canContainEols;
    public final List<Transform> transforms;
    public final Map<String, Handler> enter;
    public final Map<String, Handler> exit;

    public MdastExtension(List<String> canContainEols,
                          List<Transform> transforms,
                          Map<String, Handler> enter,
                          Map<String, Handler> exit) {
        this.canContainEols = List.copyOf(canContainEols);
        this.transforms = List.copyOf(transforms);
        this.enter = Map.copyOf(enter);
        this.exit = Map.copyOf(exit);
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<String> canContainEols = new ArrayList<>();
        private final List<Transform> transforms = new ArrayList<>();
        private final Map<String, Handler> enter = new HashMap<>();
        private final Map<String, Handler> exit = new HashMap<>();

        private Builder() {
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

        public Builder canContainEol(String... types) {
            Collections.addAll(canContainEols, types);
            return this;
        }

        public Builder transform(Transform transform) {
            transforms.add(transform);
            return this;
        }

        public Builder addAll(MdastExtension extension) {
            canContainEols.addAll(extension.canContainEols);
            transforms.addAll(extension.transforms);
            enter.putAll(extension.enter);
            exit.putAll(extension.exit);
            return this;
        }

        public MdastExtension build() {
            return new MdastExtension(canContainEols, transforms, enter, exit);
        }
    }
}
