package appeng.core;

import appeng.client.EffectType;
import net.minecraft.world.World;

/**
 * Contains mod functionality specific to a dedicated server.
 */
public class AppEngServer extends AppEngBase {
    @Override
    public World getClientWorld() {
        return null;
    }

    @Override
    public void spawnEffect(EffectType effect, World world, double posX, double posY, double posZ, Object o) {
        // Spawning client-side effects on a server is impossible
    }
}
