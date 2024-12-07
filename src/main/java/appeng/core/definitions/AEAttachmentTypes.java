package appeng.core.definitions;

import java.util.function.Supplier;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import appeng.core.AppEng;

public final class AEAttachmentTypes {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.ATTACHMENT_TYPES, AppEng.MOD_ID);

    public static final Supplier<AttachmentType<Boolean>> HOLDING_CTRL = ATTACHMENT_TYPES.register("ctrl",
            () -> AttachmentType.builder(() -> false).build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
