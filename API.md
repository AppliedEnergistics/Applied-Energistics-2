## Grids and Nodes

AE2's core systems work by building grids from grid nodes that are created and owned by ingame objects such as tile
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
class MyTileEntityListener implements IGridNodeListener<MyTileEntity> {
    public static final MyTileEntityListener INSTANCE = new MyTileEntityListener();

    @Override
    public void onActiveChanged(MyTileEntity nodeOwner, IGridNode node, ActiveChangeReason reason) {
        [...]
        // for example: change block state of nodeOwner to indicate state
        // send node owner to clients
    }
}
```

```java
MyTileEntity te=[...]; // Grab from world or similar

// Create node owned by te
        api.createInternalGridNode(
        te,
        MyTileEntityListener.INSTANCE,
        [...]
        );
```

### Configurable Grid Nodes

The node creation APIs return an `IConfigurableGridNode`, which extends `IGridNode`
with functionality to set up and configure the node, and notify it of events. This functionality should only be used by
the node owner.

Your game object should notify the nodes it owns about the following events:

- Call `destroy` on the node when your game object is destroyed or its chunk unloaded. 
- Call `markReady` when the node can assume the owner is now in-world and ready to make
  outgoing connections (i.e. on its first tick).
- When your game object loads from NBT data, load the node's stored data using `loadFromNBT`.
- When your game object saves to NBT data, save the node's data using `saveToNBT`.

### In-World Nodes

A special type of grid node is an in-world grid node. They need to know their location and world, and will automatically
attempt to connect with adjacent in-world grid nodes. In-world nodes can be selectively exposed on specific sides, or on
all sides. The exposed sides can be changed after node creation.

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

* Ticking without being a tickable tile entity
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


