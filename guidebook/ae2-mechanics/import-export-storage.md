---
navigation:
  parent: ae2-mechanics/ae2-mechanics-index.md
  title: Import, Export, and Storage
---
# Import, Export, and Storage
**your ME system and the world**

An important concept in AE2 is the idea of Network Storage. It is the place in which the contents of a network are stored,
usually [storage cells](../items-blocks-machines/storage cells.md) or whatever inventory a <ItemLink id="storage_bus" />
is connected to. Most AE2 devices interact with it in one way or another.

For example,
- <ItemLink id="import_bus" />ses push things into network storage
- <ItemLink id="export_bus" />ses pull things from network storage
- <ItemLink id="interface" />s both pull from and push to network storage
- Terminals both push to and pull from network storage when you insert or take items, or to refill the crafting slots
- <ItemLink id="storage_bus" />ses don't really push to or pull from storage, they push to or pull from the connected inventory
in order to use it as network storage (so really other devices push to or pull from *it*)

Items entering the network will start at the highest priority storage, as
their first destination, in the case of two storages have the same priority,
if one already contains the item, they will prefer that storage over any
other. Any Whitelisted cells will be treated as already containing the item
when in the same priority group as other storages. Items being removed from storage will
be removed from the storage with the lowest priority. This priority system means as items are inserted and removed
from network storage, higher priority storages will be filled and lower priority storages will be emptied.

The actions/events of pushing to and pulling from network storage are important to keep in mind when designing automation
and logistics setups.