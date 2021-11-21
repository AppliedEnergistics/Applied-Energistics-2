/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.networking.security;

import javax.annotation.Nonnegative;

import net.minecraft.world.entity.player.Player;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridService;

/**
 * Provides facilities around controlling player access to a grid.
 */
public interface ISecurityService extends IGridService {

    /**
     * @return true if exactly one node implementing the {@link ISecurityProvider} node service is connected to the
     *         grid.
     */
    boolean isAvailable();

    /**
     * Check if a player has the specified permissions on this grid.
     *
     * @param player The connected player.
     * @param perm   The permission to check.
     * @return True if the player has permission.
     */
    boolean hasPermission(Player player, SecurityPermissions perm);

    /**
     * Check if a player has the specified permissions on this grid.
     * <p/>
     * This overload can be used to check permissions for a player who is not currently connected.
     *
     * @param playerId The ID of the player to check for. See {@link appeng.api.features.IPlayerRegistry}.
     * @param perm     The permission to check.
     * @return True if the player has permission.
     */
    boolean hasPermission(@Nonnegative int playerId, SecurityPermissions perm);

    /**
     * @return PlayerID of the admin, or owner, this is the person who placed the security block.
     */
    int getOwner();
}
