package appeng.block;

import net.minecraft.entity.player.PlayerEntity;

/**
 * Implemented by tiles that need to be aware of the player who placed them.
 */
public interface IOwnerAwareTile {

    /**
     * Called when the tile is created by a player placing it.
     */
    void setOwner(PlayerEntity owner);

}
