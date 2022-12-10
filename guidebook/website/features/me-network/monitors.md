---
navigation:
  parent: website/index.md
  title: Storage Monitors
item_ids:
  - ae2:conversion_monitor
  - ae2:storage_monitor
---

## Storage Monitor

The <ItemLink id="storage_monitor"/> is a simple
way to see the current level of a specified item. There are several
interactions to modify it.

| Action                            | Effect                                                                |
| --------------------------------- | --------------------------------------------------------------------- |
| Right-click with item             | Will display the current stored amount of that item if not locked.    |
| Right-click with empty hand       | Will reset the display if not locked.                                 |
| Shift+Right-click with empty hand | Will toggle the lock.                                                 |
| Right-click with wrench           | Will rotate the monitor if it is locked and on the ground or ceiling. |

<RecipeFor id="storage_monitor" />

## Conversion Monitor

The <ItemLink id="conversion_monitor"/> is the
upgraded version of the <ItemLink
id="storage_monitor"/>. It adds the ability to
directly withdraw from or store items into the [ME Network](../me-network.md).

In addition to the storage monitor's interactions, conversion monitors support the following actions:

| Action                      | Effect                                                                  |
| --------------------------- | ----------------------------------------------------------------------- |
| Left-click                  | Extracts a stack of the shown item into your inventory.                 |
| Right-click with item       | Inserts the held item into the network.                                 |
| Right-click with empty hand | Will insert all of the shown item from your inventory into the network. |

<RecipeFor id="conversion_monitor" />
