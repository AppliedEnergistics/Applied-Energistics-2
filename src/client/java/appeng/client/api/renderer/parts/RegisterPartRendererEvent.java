package appeng.client.api.renderer.parts;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import net.neoforged.fml.DeferredWorkQueue;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.ParallelDispatchEvent;

import appeng.api.parts.IPart;

public class RegisterPartRendererEvent extends ParallelDispatchEvent {
    private final PartRegistrationSink delegate;

    public RegisterPartRendererEvent(ModContainer container, DeferredWorkQueue workQueue,
            PartRegistrationSink delegate) {
        super(container, workQueue);
        this.delegate = delegate;
    }

    public <T extends IPart> void register(Class<T> partClass, PartRendererProvider<? super T> factory) {
        delegate.register(partClass, factory);
    }

    public <T extends IPart> void register(Class<T> partClass, Supplier<PartRenderer<? super T, ?>> factory) {
        delegate.register(partClass, context -> factory.get());
    }

    @FunctionalInterface
    @ApiStatus.Internal
    public interface PartRegistrationSink {
        <T extends IPart> void register(Class<T> partClass, PartRendererProvider<? super T> factory);
    }
}
