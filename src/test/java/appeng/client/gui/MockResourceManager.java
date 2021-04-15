package appeng.client.gui;

import appeng.core.AppEng;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Assertions;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Fake resource manager that more or less loads AE2 resource pack resources.
 */
public final class MockResourceManager {
    private MockResourceManager() {
    }

    public static IReloadableResourceManager create() throws IOException {
        IReloadableResourceManager resourceManager = mock(IReloadableResourceManager.class);

        when(resourceManager.getResource(any())).thenAnswer(invoc -> {
            ResourceLocation loc = invoc.getArgument(0);
            Assertions.assertEquals(AppEng.MOD_ID, loc.getNamespace());
            InputStream in = MockResourceManager.class.getResourceAsStream("/assets/appliedenergistics2/" + loc.getPath());
            if (in == null) {
                throw new FileNotFoundException("Missing resource: " + loc.getPath());
            }

            return new IResource() {
                @Override
                public ResourceLocation getLocation() {
                    return loc;
                }

                @Override
                public InputStream getInputStream() {
                    return in;
                }

                @Nullable
                @Override
                public <T> T getMetadata(IMetadataSectionSerializer<T> serializer) {
                    return null;
                }

                @Override
                public String getPackName() {
                    return null;
                }

                @Override
                public void close() throws IOException {
                    in.close();
                }
            };
        });

        return resourceManager;
    }
}
