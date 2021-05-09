package appeng.client.gui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Assertions;

import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;

import appeng.core.AppEng;

/**
 * Fake resource manager that more or less loads AE2 resource pack resources.
 */
public final class MockResourceManager {
    private MockResourceManager() {
    }

    public static IReloadableResourceManager create() throws IOException {
        IReloadableResourceManager resourceManager = mock(IReloadableResourceManager.class, withSettings().lenient());

        when(resourceManager.getResource(any())).thenAnswer(invoc -> {
            ResourceLocation loc = invoc.getArgument(0);
            return getResource(loc);
        });
        when(resourceManager.getAllResources(any())).thenAnswer(invoc -> {
            ResourceLocation loc = invoc.getArgument(0);
            return Collections.singletonList(getResource(loc));
        });

        when(resourceManager.getResourceNamespaces()).thenReturn(Collections.singleton(AppEng.MOD_ID));

        return resourceManager;
    }

    private static IResource getResource(ResourceLocation loc) throws FileNotFoundException {
        Assertions.assertEquals(AppEng.MOD_ID, loc.getNamespace());
        InputStream in = MockResourceManager.class
                .getResourceAsStream("/assets/appliedenergistics2/" + loc.getPath());
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
                return "ae2";
            }

            @Override
            public void close() throws IOException {
                in.close();
            }
        };
    }
}
