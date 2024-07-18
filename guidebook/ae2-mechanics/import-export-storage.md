---
navigation:
  parent: ae2-mechanics/ae2-mechanics-index.md
  title: Import, Export, and Storage
---

# Import, Export, and Storage

**your ME system and the world**

An important concept in AE2 is the idea of Network Storage. It is the place in which the contents of a network are stored,
usually [storage cells](../items-blocks-machines/storage_cells.md) or whatever inventory a <ItemLink id="storage_bus" />
is connected to. Most AE2 [devices](../ae2-mechanics/devices.md) interact with it in one way or another.

For example,

*   <ItemLink id="import_bus" />ses push things into network storage
*   <ItemLink id="export_bus" />ses pull things from network storage
*   <ItemLink id="interface" />s both pull from and push to network storage
*   [Terminals](../items-blocks-machines/terminals.md) both push to and pull from network storage when you insert or take items, or to refill the crafting slots
*   <ItemLink id="storage_bus" />ses don't really push to or pull from storage, they push to or pull from the connected inventory
    in order to use it as network storage (so really other devices push to or pull from *them*)

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../assets/assemblies/import_export_storage.snbt" />

  <BoxAnnotation color="#dddddd" min="8 1 1" max="9 1.3 2">
        Import Busses import things from inventories they're pointing at into network storage
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="8 2 1" max="9 3 1.3">
        Inserting something into a terminal from your inventory counts as the network importing it
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="7 0 1" max="8 1 2">
        Interfaces will import from their internal inventories if that slot is not configured to stock anything, or there are more
        items in that slot than are configured to be stocked, so things can be pushed into them to insert into the network
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="6 0 1" max="7 1 2">
        Pattern Providers will import from their internal return slot inventory, so things can be pushed into them to insert into the network
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4 1 1" max="5 2 2">
        Drives provide the inserted cells as network storage
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="3 1 1" max="4 1.3 2">
        Storage Busses use the inventory they're pointing at as network storage
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="1 1 1" max="2 1.3 2">
        Export Busses export things from network storage into inventories they're pointing at
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="1 2 1" max="2 3 1.3">
        Pulling something out of a terminal counts as the network exporting it
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="0 1 1" max="1 2 2">
        Interfaces will export to their internal inventories if that slot is configured to stock something,
        so things can be pulled from them to extract from the network
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

The actions/events of pushing to and pulling from network storage are important to keep in mind when designing automation
and logistics setups.

## Storage Priority

Priorities can be set by clicking the wrench in the top-right of some GUIs.
Items entering the network will start at the highest priority storage, as
their first destination, in the case of two storages have the same priority,
if one already contains the item, they will prefer that storage over any
other. Any Whitelisted cells will be treated as already containing the item
when in the same priority group as other storages. Items being removed from storage will
be removed from the storage with the lowest priority. This priority system means as items are inserted and removed
from network storage, higher priority storages will be filled and lower priority storages will be emptied.
