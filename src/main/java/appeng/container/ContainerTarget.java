package appeng.container;

/**
 * The game object that a container has been opened for.
 * <p/>
 * Containers can be opened for various in-game objects which do not derive from a common base-class:
 * <ul>
 * <li>Items in the player inventory (i.e. wireless terminals)</li>
 * <li>Tile Entities in the world (i.e. vibration chamber)</li>
 * <li>Parts that are attached to a multi-block tile entity</li>
 * </ul>
 * <p/>
 * This class tries to capture all of these cases for container base classes which do not enforce a specific interface
 * requirement.
 */
public final class ContainerTarget {

}
