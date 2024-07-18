---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: ME Chest
  icon: chest
  position: 210
categories:
- devices
item_ids:
- ae2:chest
---

# The ME Chest

<GameScene zoom="8" background="transparent">
<ImportStructure src="../assets/blocks/chest.snbt" />
</GameScene>

The ME Chest acts like a miniature network with a <ItemLink id="terminal" />, <ItemLink id="drive" />, and <ItemLink id="energy_acceptor" />.
While it can be used as a tiny storage network, its capacity for just one single [storage cell](../items-blocks-machines/storage_cells.md)
means it has limited utility as such.

Instead, it is useful for interacting with specifically the storage cell mounted inside it. Its integrated terminal can only see and access
the items in the mounted drive, while [devices](../ae2-mechanics/devices.md) on the general network can access items in any [network storage](../ae2-mechanics/import-export-storage.md),
including ME chests.

It has 2 different GUIs and is sided for item transport. Interacting with the top terminal opens the integrated terminal. Items can be inserted into
the mounted storage cell through this face, but not extracted. Interacting with any other face opens the GUI with the slot for the storage cell
and the priority settings. The cell can be inserted and removed by item logistics only through the face with the cell slot.

It can be rotated with a <ItemLink id="certus_quartz_wrench" />.

It has a small AE energy storage buffer, so if not on a network with an [energy cell](../items-blocks-machines/energy_cells.md),
inserting or extracting too many items at once may cause it to brown out.

The terminal can be colored with a <ItemLink id="color_applicator" />.

<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/chest_color.snbt" />
<IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Settings

The ME Chest has all the same settings as a <ItemLink id="terminal" /> or <ItemLink id="crafting_terminal" />.
It, however, does not support <ItemLink id="view_cell" />s.

## Cell Status LEDs

The cells in the chest have an LED on them which shows their status:

| Color  | Status                                                                           |
| :----- | :------------------------------------------------------------------------------- |
| Green  | Empty                                                                            |
| Blue   | Has some contents                                                                |
| Orange | [Types](../ae2-mechanics/bytes-and-types.md) full, no new types can be added     |
| Red    | [Bytes](../ae2-mechanics/bytes-and-types.md) full, no more items can be inserted |
| Black  | No power or drive has no [channel](../ae2-mechanics/channels.md)                 |

## Priority

Priorities can be set by clicking the wrench in the top-right of the cell slot GUI.
Items entering the network will start at the highest priority storage as
their first destination. In the case of two storages or cells have the same priority,
if one already contains the item, they will prefer that storage over any
other. Any [partitioned](cell_workbench.md) cells will be treated as already containing the item
when in the same priority group as other storages. Items being removed from storage will
be removed from the storage with the lowest priority. This priority system means as items are inserted and removed
from network storage, higher priority storages will be filled and lower priority storages will be emptied.

## Recipe

<RecipeFor id="chest" />
