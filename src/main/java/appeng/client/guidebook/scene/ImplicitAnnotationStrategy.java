package appeng.client.guidebook.scene;

import appeng.client.guidebook.extensions.Extension;
import appeng.client.guidebook.extensions.ExtensionPoint;
import appeng.client.guidebook.scene.annotation.SceneAnnotation;
import appeng.client.guidebook.scene.level.GuidebookLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Provides a way to generate a {@link appeng.client.guidebook.scene.annotation.SceneAnnotation} on the fly
 * if no explicit annotation could be found under the mouse.
 */
public interface ImplicitAnnotationStrategy extends Extension {
    ExtensionPoint<ImplicitAnnotationStrategy> EXTENSION_POINT = new ExtensionPoint<>(ImplicitAnnotationStrategy.class);

    @Nullable
    SceneAnnotation getAnnotation(GuidebookLevel level, BlockState blockState, BlockHitResult blockHitResult);
}
