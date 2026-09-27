---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: ME Storage Bus
  icon: storage_bus
  position: 220
categories:
- devices
item_ids:
- ae2:storage_bus
---

# The Storage Bus

<GameScene zoom="8" background="transparent">
<ImportStructure src="../assets/blocks/storage_bus.snbt" />
</GameScene>

Ever wanted to *keep* your chest monster instead of replacing it with something sensible? We present the storage bus!

The storage bus turns the inventory it's touching into [network storage](../ae2-mechanics/import-export-storage.md).
It does this by allowing the network to see the contents of that inventory, and by pushing to and pulling from that
inventory in order to fulfill the needs of [devices](../ae2-mechanics/devices.md) that push to and pull from network storage.

Due to AE2's philosophy of emergent mechanics through interaction of the functions of the [devices](../ae2-mechanics/devices.md), you don't
necessarily *have* to use a storage bus for *storage*. By using [subnetworks](../ae2-mechanics/subnetworks.md)
to make a storage bus (or handful of storage buses) the *only* storage on a network, you can use it as a source or destination
for item transfer. For examples, see ["pipe subnet"](../example-setups/pipe-subnet.md).

IMPORTANT NOTE: Big optimized inventories like drawers are fine, but big *unoptimized* inventories with many slots, like
colossal chests, are terrible for performance when used with storage buses.

They are [cable subparts](../ae2-mechanics/cable-subparts.md).

## Filtering

By default, the bus will store everything. Items inserted into its filter slots will act as a whitelist, only
allowing those specific items to be stored.

Items and fluids can be dragged into the slots from JEI/REI, even if you don't actually have any of that item.

Right-click with a fluid container (like a bucket or fluid tank) to set that fluid as a filter instead of the bucket or tank item.

## Priority

Priorities can be set by clicking the wrench in the top-right of the GUI.
Items entering the network will start at the highest priority storage as
their first destination. In the case of two storages have the same priority,
if one already contains the item, they will prefer that storage over any
other. Any filtered storages will be treated as already containing the item
when in the same priority group as other storages. Items being removed from storage will
be removed from the storage with the lowest priority. This priority system means as items are inserted and removed
from network storage, higher priority storages will be filled and lower priority storages will be emptied.

## Settings

*   The bus can be partitioned (filtered) to what is currently in the adjacent inventory
*   The network can be disallowed or allowed to see items in the adjacent inventory that the bus cannot extract.
    For example, a storage bus cannot extract items from the middle input slot of an <ItemLink id="inscriber" />.
*   The bus can filter on both insertion and extraction or just insertion
*   The bus can be bidirectional, insert-only, or extract-only

## Upgrades

The storage bus supports the following [upgrades](upgrade_cards.md):

*   <ItemLink id="capacity_card" /> increases the amount of filter slots
*   <ItemLink id="fuzzy_card" /> lets the bus filter by damage level and/or ignore item NBT
*   <ItemLink id="inverter_card" /> switches the filter from a whitelist to a blacklist
*   <ItemLink id="void_card" /> voids items inserted if the attached inventory is full. Useful for preventing farms from backing up. Be careful to partition the storage bus!

## Recipe

<RecipeFor id="storage_bus" />
