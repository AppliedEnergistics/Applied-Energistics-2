package appeng.client.guidebook.extensions;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import appeng.client.guidebook.compiler.TagCompiler;
import appeng.client.guidebook.compiler.tags.ATagCompiler;
import appeng.client.guidebook.compiler.tags.BoxFlowDirection;
import appeng.client.guidebook.compiler.tags.BoxTagCompiler;
import appeng.client.guidebook.compiler.tags.BreakCompiler;
import appeng.client.guidebook.compiler.tags.CategoryIndexCompiler;
import appeng.client.guidebook.compiler.tags.DivTagCompiler;
import appeng.client.guidebook.compiler.tags.FloatingImageCompiler;
import appeng.client.guidebook.compiler.tags.ItemGridCompiler;
import appeng.client.guidebook.compiler.tags.ItemLinkCompiler;
import appeng.client.guidebook.compiler.tags.RecipeCompiler;
import appeng.client.guidebook.scene.BlockImageTagCompiler;
import appeng.client.guidebook.scene.ItemImageTagCompiler;
import appeng.client.guidebook.scene.SceneTagCompiler;
import appeng.client.guidebook.scene.annotation.BlockAnnotationElementCompiler;
import appeng.client.guidebook.scene.annotation.BoxAnnotationElementCompiler;
import appeng.client.guidebook.scene.annotation.DiamondAnnotationElementCompiler;
import appeng.client.guidebook.scene.element.ImportStructureElementCompiler;
import appeng.client.guidebook.scene.element.IsometricCameraElementCompiler;
import appeng.client.guidebook.scene.element.SceneBlockElementCompiler;
import appeng.client.guidebook.scene.element.SceneElementTagCompiler;

public final class DefaultExtensions {
    private static final List<Registration<?>> EXTENSIONS = List.of(
            new Registration<>(TagCompiler.EXTENSION_POINT, DefaultExtensions::tagCompilers),
            new Registration<>(SceneElementTagCompiler.EXTENSION_POINT, DefaultExtensions::sceneElementTagCompilers));

    private DefaultExtensions() {
    }

    public static void addAll(ExtensionCollection.Builder builder, Set<ExtensionPoint<?>> disabledExtensionPoints) {
        for (var registration : EXTENSIONS) {
            add(builder, disabledExtensionPoints, registration);
        }
    }

    private static <T extends Extension> void add(ExtensionCollection.Builder builder,
            Set<ExtensionPoint<?>> disabledExtensionPoints, Registration<T> registration) {
        if (disabledExtensionPoints.contains(registration.extensionPoint)) {
            return;
        }

        for (var extension : registration.factory.get()) {
            builder.add(registration.extensionPoint, extension);
        }
    }

    private static List<TagCompiler> tagCompilers() {
        return List.of(
                new DivTagCompiler(),
                new ATagCompiler(),
                new ItemLinkCompiler(),
                new FloatingImageCompiler(),
                new BreakCompiler(),
                new RecipeCompiler(),
                new ItemGridCompiler(),
                new CategoryIndexCompiler(),
                new BlockImageTagCompiler(),
                new ItemImageTagCompiler(),
                new BoxTagCompiler(BoxFlowDirection.ROW),
                new BoxTagCompiler(BoxFlowDirection.COLUMN),
                new SceneTagCompiler());
    }

    private static List<SceneElementTagCompiler> sceneElementTagCompilers() {
        return List.of(
                new SceneBlockElementCompiler(),
                new ImportStructureElementCompiler(),
                new IsometricCameraElementCompiler(),
                new BlockAnnotationElementCompiler(),
                new BoxAnnotationElementCompiler(),
                new DiamondAnnotationElementCompiler());
    }

    private record Registration<T extends Extension> (ExtensionPoint<T> extensionPoint,
            Supplier<Collection<T>> factory) {
    }
}
