package appeng.client.guidebook.style;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import appeng.client.guidebook.document.DefaultStyles;

public interface Styleable {
    TextStyle getStyle();

    void setStyle(TextStyle style);

    TextStyle getHoverStyle();

    void setHoverStyle(TextStyle style);

    @Nullable
    Styleable getStylingParent();

    default void modifyStyle(Consumer<TextStyle.Builder> customizer) {
        var builder = getStyle().toBuilder();
        customizer.accept(builder);
        setStyle(builder.build());
    }

    default void modifyHoverStyle(Consumer<TextStyle.Builder> customizer) {
        var builder = getHoverStyle().toBuilder();
        customizer.accept(builder);
        var hoverStyle = builder.build();
        if (hoverStyle.whiteSpace() != null) {
            throw new IllegalStateException("Hover-Style may not override layout properties");
        }
        setHoverStyle(hoverStyle);
    }

    default ResolvedTextStyle resolveStyle() {
        var stylingParent = getStylingParent();
        if (stylingParent != null) {
            return getStyle().mergeWith(stylingParent.resolveStyle());
        }

        return getStyle().mergeWith(DefaultStyles.BASE_STYLE);
    }

    default ResolvedTextStyle resolveHoverStyle(ResolvedTextStyle baseStyle) {
        var stylingParent = getStylingParent();
        if (stylingParent != null) {
            return getHoverStyle().mergeWith(stylingParent.resolveHoverStyle(baseStyle));
        }

        return getHoverStyle().mergeWith(baseStyle);
    }
}
