---
navigation:
  parent: example-setups/example-setups-index.md
  title: Types of Storage and Network Cleanliness
  icon: drive
---

# Various Kinds of Storage and Keeping Your Network Organized

Using filters, [partitions](../items-blocks-machines/cell_workbench.md), and [storage priority](../ae2-mechanics/import-export-storage.md#storage-priority),
you can set up several tiers of storage for various kinds of things.

The kinds of storage tend to be:
* General storage, for all the random stuff you have a few to a few thousand of. This uses small [cells](../items-blocks-machines/storage_cells.md),
like 4k or 16k.
* Bulk storage, for all the stuff you have more than a few thousand of, like cobble or iron. This uses big cells like 256k
or the cells from the MEGA addon.
* Local storage at farms, as described in [Specialized Local Storage](specialized-local-storage.md) and the 
[various](simple-certus-farm.md) [certus](semiauto-certus-farm.md) [farms](advanced-certus-farm.md).

The priorities are set up so that when items are dumped into the main network, it first tries to store them in the specialized
bulk or local storage, and if that can't be done (due to filters and partitions), it then puts the items in general storage.
This means that items WILL NOT ACTIVELY MOVE from one storage ot the other, but will "migrate" as they enter and leave the network.
To actively move items, use an <ItemLink id="io_port" />.

<GameScene zoom="3" interactive={true}>
  <ImportStructure src="../assets/assemblies/network_storage_types.snbt" />

    <BoxAnnotation color="#33dd33" min="11 0 1" max="12 1.3 2" thickness="0.05">
        Bulk Storage. In this case a filtered storage bus on a large capacity storage like a drawer. This storage bus is filtered to
        coal. It has a high priority so whenever coal enters the network, it goes to this storage bus, and whenever coal is 
        pulled from the network, it is pulled from *evere except here*, so coal "migrates" to this drawer.

        IMPORTANT NOTE: Big optimized inventories like drawers are fine for this, but big *un*optimized inventories with many slots, like
        colossal chests, are terrible for performance when used with storage busses.
    </BoxAnnotation>

    <BoxAnnotation color="#33dd33" min="11 0 3" max="12 1 4" thickness="0.05">
        Bulk Storage. In this case a partitioned 256k cell in a drive with high priority. This cell is partitioned to
        cobblestone and iron. It has an Equal Distribution Card, so it won't be completely filled with cobblestone, leaving
        no space for iron. The drive has a high priority so whenever cobble or iron enters the network, it goes to this storage bus,
        and whenever cobble or iron is pulled from the network, it is pulled from *evere except here*, so cobble and iron "migrate" to this cell.
    </BoxAnnotation>

    <BoxAnnotation color="#33dddd" min="11 0 5" max="12 1 6" thickness="0.05">
        General Storage. In this case a drive full of 16k cells. These cells are not partitioned. The drive has a neutral priority
        (in this case 0) so whenever something enters the network, it goes to the specialized bulk or local storage first,
        and whenever something is pulled from the network, it is pulled from here first, so items that have specialized storage naturally
        "migrate" out of general storage.
    </BoxAnnotation>

    <BoxAnnotation color="#88ff88" min="11 0 8" max="12 1 9" thickness="0.05">
        This IO Port plays an important role in keeping the network organized. Because storage priority does not *actively*
        move items, cells used in General Gtorage should be periodically "shuffled" through an IO port to move items that have a
        place in specialized storage into that specialized storage. This "defragments" the storage, making sure things aren't
        being stored in multiple places.
    </BoxAnnotation>

    <BoxAnnotation color="#dd3333" min="14 0 11" max="15 1 12" thickness="0.05">
        Local Storage at a mob farm. This drive has cells partitioned to the drops you want to keep, like bones and arrows.
        The drive itself is not given priority, because the thing that affects the priority is the storage bus accessing the subnet
        from the main net. The cells have equal distribution cards and overflow destruction cards.
    </BoxAnnotation>

    <BoxAnnotation color="#dd3333" min="14 1 10" max="15 2.3 11" thickness="0.05">
        Local Storage at a mob farm. This storage bus - interface setup allows the main network to acces this subnet's storage.
        The storage bus is given a high priority and filtered to the things stored in the cells on the subnet.

        IMPORTANT: Due to the trash can setup on the subnet, make sure to filter this storage bus or it will start trashing
        *every single item, fluid, etc. that enters the network*!
    </BoxAnnotation>

    <BoxAnnotation color="#dd3333" min="14 0 9" max="15 1.3 10" thickness="0.05">
        Local Storage at a mob farm. This storage bus on a matter condenser is set to a lower priority than the drive. This means
        that mob drops that cannot enter the cells in the drive will overflow to here, and be disposed of. This is important,
        in order to prevent the subnet from being jammed full of random junk like mostly-broken bows.
    </BoxAnnotation>

    <BoxAnnotation color="#dd33dd" min="8 1 11.7" max="9 2.3 13" thickness="0.05">
        Local Storage at a melon farm. This setup uses a similar method used in the various certus farm examples. A storage bus
        on the subnet inserts the thing being farmed into a barrel. Another storage bus on the main network (filtered to melon
        slices and with a high priority) gives the main network access to the things being farmed.
    </BoxAnnotation>

  <IsometricCamera yaw="270" pitch="30" />
</GameScene>
