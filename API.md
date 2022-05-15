---
title: Addon and Mod API
# Note that this file is automatically included into the Website and is available at https://appliedenergistics.github.io/api.html
---

The Javadocs of the latest unreleased version are available at https://appliedenergistics.github.io/javadoc/.

## Mod Initialization

AE2 offers various extension points for your mod to hook into. The following table lists the API classes that are most
relevant during mod initialization:

| Class | Purpose                                                                                                          |
| ------------- |------------------------------------------------------------------------------------------------------------------|
| `appeng.api.stacks.AEKeyTypes`  | Addons can use this class to register custom storage types similar to `AEItemKey` and `AEFluidKey`.               |
| `appeng.api.networking.GridServices`  | Addons can register their own grid-wide services here.                                                           |
| `appeng.api.movable.BlockEntityMoveStrategies` | Allows mods to register custom strategies for moving block entities in and out of spatial storage.              |
| `appeng.api.features.GridLinkables` | For working with and adding items that can be linked to a grid in the security station.                          |
| `appeng.api.storage.StorageCells` | For working with and adding items that serve as storage cells for grids.                                         |
| `appeng.api.features.Locatables` | For discovering security stations and quantum network bridges based on their unique keys, regardless of location. |
| `appeng.api.parts.PartModels` | For registering JSON block models used by custom cable bus parts.                                                |
| `appeng.api.features.P2PTunnelAttunement` | For registering new items that attune P2P tunnels to specific types when right-clicked.                |
| `appeng.api.client.StorageCellModels` | For customizing the models of storage cells when they're inserted into drives or ME chests.                      |

In general, these classes are thread-safe and may be used directly in a mod's constructor or thereafter.
Once initialization of mods has completed however, changes to these registries result in undefined behavior.

Since order of mod initialization on Fabric is undefined, addons that rely on AE2's items and blocks being registered
will need to use the custom entrypoint defined by `IAEAddonEntrypoint`. See that classes javadoc for details.

## Item and Fluid Keys

Item and fluid types are represented by keys in AE2. The `AEKey` class is the base for all keys, whether they represent
items (`AEItemKey`) or fluids (`AEFluidKey`). Most of AE2s interfaces are generic in that they accept any `AEKey`,
whether it is for a fluid or item. 

Keys do not have counts since they don't represent a particular amount of items or fluid, they represent the *type*
of item or type of fluid. As such, an item key consists of a reference to the `Item` and potential NBT data.

To represent a stack of some key, AE2 provides the utility class `GenericStack`. It consists of a key and an amount.

Each type of key is represented by an instance of `AEKeyType`, which is accessible via `AEKey.getType()`. It stores
some properties common to all keys of a type (i.e. all item keys, or all fluid keys). 

Keys can be saved to from NBT using `toTagGeneric`, which also stores a reference to their type so that
`AEKey.fromTagGeneric` can restore the key of the correct type. The same mechanism can be used for packets with
`AEKey.writeToPacket` and `AEKey.readKey`.

Sicne Java 16, the following patter makes it easy to work with generic keys when your code only supports items:

```java
if (key instanceof AEItemKey itemKey) {
    ItemStack is = itemKey.toStack();
    // [...]
}
```

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
using `GridServices`.

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

## Adding New Upgrades or Making Upgradable Machines

This will be made available in 10.0.0-beta.3.

Relevant APIs:

- `appeng.api.upgrades.Upgrades` for managing upgrade cards and associating them with machines
- `appeng.api.upgrades.UpgradeInventories` for creating upgrade inventories for use in upgradable machines or items

### Custom Upgrade Cards

Each upgrade is unique identified by a registered item (the "upgrade card"). To create a custom upgrade card that
behaves like the existing AE2 cards (i.e. it can be inserted into the network tool's toolbelt), use the
utility function `Upgrades#createUpgradeCardItem` to create an item for your card. Remember it's your responsibility
to actually register this item, provide an icon and a translation key for it. It will however, show the tooltip for supported machines and support insertion into machines by right-click out of the box. 

### Associating Upgrade Cards with Machines

For both cases where your addon adds a custom machine or upgrade card, you need to associate possible upgrades
with potential machines. The `Upgrades.add` method allows you to link an upgrade card (represented by its Item) with
a Machine (also represented by an item, usually a `BlockItem` or `IPartItem`). 

If there are multiple machines that are treated equally with regard to upgrades, you can pass a translation key to 
the `tooltipGroup` parameter. When displaying the tooltip for an upgrade card, all supported machines with the same
`tooltipGroup` will be merged into a single line and shown using the translation for the group. This was used
for displaying fluid and item parts as one line, as well as the block/part form of interfaces. 

### Making custom Machines Upgradable

You can use the factory class `UpgradeInventories` to create inventories for storing upgrade cards. These
inventories will use the provided item to identify which upgrade cards are accepted by the inventory to
automatically prevent incompatible cards from being inserted.

They also offer convenience methods (see `IUpgradeInventory`) to quickly check if an upgrade is present or
count how many upgrades of a type are present.

For the machine version created by `forMachine`, you are responsible for saving the inventory yourself from the
change callback. For the item version created by `forItem`, the upgrade inventory will automatically save itself 
to the provided ´ItemStack` whenever its content changes.

# Changes from 1.17 and before to 1.18

There are large changes to the API in 1.18.

`IAEStack`, `IAEItemStack` and `IAEFluidStack` have been removed. The API now separates the "what" from the "how much"
in that it uses `AEKey` to identify what is being transferred, while a separate method-argument is used for the
amount.

The mapping is roughly as follows:

| Old Class                | New Class                    |
|--------------------------|------------------------------|
| IAEStack                 | GenericStack, AEKey          |
| IAEItemStack             | GenericStack, AEItemKey      |
| IAEFluidStack            | GenericStack, AEFluidKey     |
| IStorageChannel          | AEKeyType                    |
| StorageChannels          | AEKeyTypes                   |
| StorageChannels.items()  | AEKeyType.items()            |
| StorageChannels.fluids() | AEKeyType.fluids()           |
| IMEInventory             | MEStorage                    |
| IMEMonitorable           | MEMonitorStorage             |
| IGuiItem                 | IMenuItem (Use ItemMenuHost) |
| IPortableCell            | IPortableTerminal            |
| ICraftingMedium          | ICraftingMachine             |
| ICellProvider            | IStorageProvider             | 
| getUnitsPerByte          | getAmountPerByte             |
| transferFactor           | getAmountPerOperation        |

The network inventory is no longer channel specific. It contains items, fluids and potentially keys
from addons at the same time. This also means `IStorageMonitorable` has become superfluous and was removed.
`IStorageMonitorableAccessor` now gives direct access to the storage.

Stack watching has changed to only send the keys for which the stored amount has changed. This was
done since the amounts reported to the watchers were never reliable to begin with, and were never used.

Craftable items are no longer reported as part of the network storage. It has been replaced by
`grid.getCraftingService().getCraftables()`. `NoOpKeyFilter` is provided in case you want all types of 
keys, otherwise there are the convenience filters `AEItemKey.filter()` and `AEFluidKey.filter()` to
only retrieve items or fluids.

Mounting storage into the network storage has been changed. Since storage has been unified across types,
the storage service will now call `mountInventories` on the `IStorageProvider` service provided by any
grid node and allow the node to "mount" storage into the network.
When the node wants to remove or add storage due to an external event or config change, it can request
the storage to repeat the mounting process by calling `IStorageGrid.refreshNodeStorageProvider` or using
the utility provided in `IStorageProvider.requestUpdate`. This supersedes sending the `GridCellArrayUpdate` event.

## Internal APIs

The following changes have been made to internal APIs, which may still be of interest to addons that
depend on them.

Items that open AE GUIs are now more addon friendly. The `ItemMenuHost` class can be used as an easy
way to implement a menu host for hosting terminals and other menus.

The priority and crafting confirm menus now use a generic system for returning to the previous screen.
Your part, block entity or item menu host needs to implement `ISubMenuHost` for this to work.

Custom storage cells have been simplified, and the same class can be used to create addon storage
cells for any stored item key. Due to the storage math still being different for items and fluids,
there are still key-type specific cells, which are all based on the same class `BasicStorageCell`,
which doesn't have a guaranteed API however (this is an improvement for later).
