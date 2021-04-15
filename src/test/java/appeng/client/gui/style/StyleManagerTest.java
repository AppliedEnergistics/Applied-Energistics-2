package appeng.client.gui.style;

import appeng.container.SlotSemantic;
import appeng.core.AppEng;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class StyleManagerTest {

    @Mock
    Minecraft minecraft;

    @Mock
    IReloadableResourceManager resourceManager;

    @Captor
    ArgumentCaptor<IFutureReloadListener> reloadCaptor;

    @BeforeEach
    void setUp() throws IOException {
        when(resourceManager.getResource(any())).thenAnswer(invoc -> {
            ResourceLocation loc = invoc.getArgument(0);
            Assertions.assertEquals(AppEng.MOD_ID, loc.getNamespace());
            InputStream in = getClass().getResourceAsStream("/assets/appliedenergistics2/" + loc.getPath());
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
    }

    @Test
    void testInitialize() {
        when(minecraft.getResourceManager()).thenReturn(resourceManager);
        StyleManager.initialize(minecraft);
        verify(resourceManager).addReloadListener(reloadCaptor.capture());
        assertThat(reloadCaptor.getValue())
                .isNotNull()
                .isInstanceOf(ISelectiveResourceReloadListener.class);
        ((ISelectiveResourceReloadListener) reloadCaptor.getValue()).onResourceManagerReload(resourceManager);
    }

    @Test
    void testLoadStyleDoc() throws IOException {
        testInitialize();

        ScreenStyle style = StyleManager.loadStyleDoc("/screens/cell_workbench.json");

        assertThat(style.getColor(PaletteColor.TEXT_HEADLINE).toARGB()).isEqualTo(0xff404040);
        assertThat(style.getText()).hasSize(2);
        assertThat(style.getSlots()).containsOnlyKeys(
                SlotSemantic.PLAYER_INVENTORY,
                SlotSemantic.PLAYER_HOTBAR,
                SlotSemantic.CONFIG,
                SlotSemantic.STORAGE_CELL
        );
    }
}