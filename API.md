## Mod Initialization

AE2 offers various extension points for your mod to hook into. The following table lists the API classes that are most
relevant during mod initialization:

| Class | Purpose |
| ------------- | ------------- |
| `appeng.api.storage.StorageChannels`  | Access to AE2's built-in item and fluid storage channels, as well as storage channels registered by addons. Addons also register their storage channels here.  |
| `appeng.api.networking.GridServices`  | Addons can register their own grid-wide services here. |
| `appeng.api.features.AEWorldGen`  | Offers limited control over AE2's world generation. |
| `appeng.api.features.ChargerRegistry` | Controls how fast items charge in AE2's charger. |

In general, these classes are thread-safe and may be used directly in a mod's constructor or thereafter.
Once initialization of mods has completed however, changes to these registries result in undefined behavior.

## Grids and Nodes

AE2's core systems work by building grids from grid nodes that are created and owned by ingame objects such as block
entities or parts. Grids are never created directly. They form and disband automatically by creating grid nodes, and
connecting or disconnecting them.

**NOTE:** Grids a purely a server-side concept. They do not exist on the client.

### Node Owners and Listeners

Every node is owned by an in-game object. An owner doesn't need to implement any particular interface. This makes it
possible to integrate existing game objects with AE2 without having to introduce a hard dependency on it.

The node uses a listener (`IGridNodeListener<T>`) to interact with its owner. Both owner and listener have to be passed
together to `IGridHelper` to create a node to allow the listener to be reused while still having type-safe access to the
owner.

**Example:**

```java
class MyBlockEntityListener implements IGridNodeListener<MyBlockEntity> {
    public static final MyBlockEntityListener INSTANCE = new MyBlockEntityListener();

    @Override
    public void onStateChanged(MyBlockEntity nodeOwner, IGridNode node, StateChangeReason reason) {
        [...]
        // for example: change block state of nodeOwner to indicate state
        // send node owner to clients
    }
}
```

```java
class MyBlockEntity {
    // Create node with owner and listener
    private final IManagedNode mainNode = api.createManagedNode(
            this,
            MyBlockEntityListener.INSTANCE
    );
}
```

### Managed Grid Nodes

The `IGridHelper` API offers a `createManagedNode` method to create an `IManagedGridNode`. Managed grid nodes simplify
the lifecycle of creating and destroying grid nodes, and can be used to simplify the distinction between server and
client, since they are available on the client-side as well. They will just not create the underlying node if they're
being used on the client.

Your game object should notify the managed node about the following events:

- Call `destroy` on the node when your game object is destroyed or its chunk unloaded.
- Call `create` when the node can assume the owner is now in-world and ready to make outgoing connections (i.e. on its
  first tick).
- When your game object loads from NBT data, load the node's stored data using `loadFromNBT`. This has to occur before
  you call `create`.
- When your game object saves to NBT data, save the node's data using `saveToNBT`.

### In-World Nodes

The main type of grid node are in-world grid nodes. They need to know their location and world when being created with
`IManagedGridNode.create(Level, BlockPos)`. External connections are automatically attempt to connect with adjacent
in-world grid nodes by AE2 itself and do not need further handling.

In-world nodes can be selectively exposed on specific sides, or on all sides. The exposed sides can be changed after
node creation and will automatically trigger a repathing.

To expose the actual `IGridNode`, it needs to be exposed by `IManagedGridNode.getNode()` through an appropriate way like
capabilities.

### Virtual Nodes

A special case are virtual nodes, which will not automatically form connection with other nodes. These allow addons to
build ME networks outside the normal world for various reasons.

As these do not automatically establish connections, these have to be manually created with by using
`IGridHelper.createGridConnection(IGridNode, IGridNode)`. Removing a connection requires destroying the `IGridNode`,
which also handles chunk unloading and ensures it leaving no old connections behind.

### Node Services

The node's owner can add so-called services to a node, which can be used to add additional functionality or behavior to
grid nodes. Services are represented by an interface that extends from `IGridService`.

Node services are often used by grid services to offer additional functionality to grid nodes that implement a specific
service. These will be described in more detail in the description of the respective grid service.

### Grid Services

Each grid provides several services to machines connected to the grid.

AE2 provides some services by default (see sub-interfaces of `IGridService`). Addons can register their own services
using `IGridServiceRegistry`.

Services can be retrieved by calling `IGrid#getService` by passing the grid service's interface. For getting AE2's
default services, `IGrid` offers several convenience methods.

#### Energy

**Service Interface:** `IEnergyService`

This service allows energy to be extracted from and injected into the grid's energy storage (i.e. energy cells, the
grid's internal storage, etc.).

#### Ticking

**Service Interface:** `ITickManager`
**Convenience Getter**: `IGrid.getTickManager`

AE2 offers its grid connected machines an advanced ticking system with the following features:

* Ticking without being a tickable block entity
* Variable tick rates
* Putting devices to sleep if they run out of work
* Waking sleeping devices in reaction to some event (i.e. neighbors changed)

The grid's `ITickManager` service handles the per-grid aspects of this ticking system. It offers an API to manage the
sleep/wake status of grid nodes.

To participate in the ticking system, your grid node must provide the `IGridTickable`
grid node service. The `ITickManager` reacts to the presence of this service when your grid node joins the grid.

#### Storage

**Service Interface**: `IStorageService`
**Convenience Getter**: `IGrid.getStorageService`

This service allows nodes to notify listeners about changes to their inventory that are not caused by normal
extraction/insertion, such as the external inventories (i.e. chests) changing their content.

Storage in grids is organized in "cells" which model inventories.

It also implements `IStorageMonitorable` to allow changes to the grid's inventory to be monitored.

#### Auto-Crafting

**Service Interface**: `ICraftingService`

#### Security

**Service Interface**: `ISecurityService`

#### Pathing

**Service Interface**: `IPathingService`

#### Spatial I/O

**Service Interface**: `ISpatialService`


