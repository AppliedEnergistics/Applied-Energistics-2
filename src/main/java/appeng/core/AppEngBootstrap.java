package appeng.core;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

/**
 * This class is just responsible for initializing the client or server-side mod class.
 */
@Mod(AppEng.MOD_ID)
public class AppEngBootstrap {
    public AppEngBootstrap() {
        DistExecutor.unsafeRunForDist(() -> AppEngClient::new, () -> AppEngServer::new);
    }
}
