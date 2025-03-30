package appeng.client.api.renderer.parts;

import appeng.api.parts.IPart;
import net.neoforged.fml.DeferredWorkQueue;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.ParallelDispatchEvent;
import org.jetbrains.annotations.ApiStatus;

public class RegisterPartRendererEvent extends ParallelDispatchEvent {
    private final PartRegistrationSink delegate;

    public RegisterPartRendererEvent(ModContainer container, DeferredWorkQueue workQueue, PartRegistrationSink delegate) {
        super(container, workQueue);
        this.delegate = delegate;
    }

    public <T extends IPart> void register(Class<T> partClass, PartRenderer<? super T> renderer) {
        delegate.register(partClass, renderer);
    }

    @FunctionalInterface
    @ApiStatus.Internal
    public interface PartRegistrationSink {
        <T extends IPart> void register(Class<T> partClass, PartRenderer<? super T> renderer);
    }
}
