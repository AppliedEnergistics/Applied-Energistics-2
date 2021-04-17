package appeng.client.gui.style;

import appeng.client.gui.MockResourceManager;
import appeng.container.SlotSemantic;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@MockitoSettings(strictness = Strictness.LENIENT)
class StyleManagerTest {

    @Captor
    ArgumentCaptor<IFutureReloadListener> reloadCaptor;

    @Test
    void testInitialize() throws IOException {
        IReloadableResourceManager resourceManager = MockResourceManager.create();
        StyleManager.initialize(resourceManager);
        verify(resourceManager).addReloadListener(reloadCaptor.capture());
        assertThat(reloadCaptor.getValue())
                .isNotNull()
                .isInstanceOf(ISelectiveResourceReloadListener.class);
        ((ISelectiveResourceReloadListener) reloadCaptor.getValue()).onResourceManagerReload(resourceManager);
    }

    @Test
    void testLoadStyleDoc() throws IOException {
        StyleManager.initialize(MockResourceManager.create());

        ScreenStyle style = StyleManager.loadStyleDoc("/screens/cell_workbench.json");

        assertThat(style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB()).isEqualTo(0xff404040);
        assertThat(style.getText()).hasSize(2);
        assertThat(style.getSlots()).containsOnlyKeys(
                SlotSemantic.PLAYER_INVENTORY,
                SlotSemantic.PLAYER_HOTBAR,
                SlotSemantic.CONFIG,
                SlotSemantic.STORAGE_CELL);
    }
}