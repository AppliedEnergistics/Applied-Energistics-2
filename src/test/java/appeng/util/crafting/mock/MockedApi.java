package appeng.util.crafting.mock;

import appeng.api.IAppEngApi;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.core.Api;
import appeng.core.api.ApiStorage;
import appeng.util.item.ItemList;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class MockedApi {
    public static void init() {
        IAppEngApi api = mock(IAppEngApi.class, RETURNS_DEEP_STUBS);

        when(api.storage()).thenReturn(new ApiStorage());
        Api.setInstance(api);
    }
}
