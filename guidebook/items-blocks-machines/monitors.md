---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Monitors
  icon: storage_monitor
  position: 210
item_ids:
- ae2:storage_monitor
- ae2:conversion_monitor
---
# Monitors

Monitors allow visualization and interaction with a single item or fluid type, without opening a GUI.

Monitors will inherit the color of the [cable](cables.md) they are mounted on.

If the monitor is on the floor or ceiling, you can rotate it with a <ItemLink id="certus_quartz_wrench" />.

They are [cable subparts](../ae2-mechanics/cable-subparts.md).

---

# Storage Monitor

Will show an item or fluid and its quantity. Put them next to your farms or something...

Does *not* require a [channel](../ae2-mechanics/channels.md).

Keybinds:
- Right-click with an item or double-right-click with a fluid container to set the monitor to that item/fluid.
- Right-click with an empty hand to clear the monitor.
- Shift-right-click with an empty hand to lock the monitor.

# Recipe

<RecipeFor id="storage_monitor" />

---

# Conversion Monitor

The Conversion Monitor is similar to a storage monitor, but allows you to insert or extract its configured item.

*Does* require a [channel](../ae2-mechanics/channels.md).

Additional keybinds:
- Left-click to extract a stack of the configured item.
- Right-click with any item to insert that item.
- Right-click with an empty hand to insert all of the configured item from your inventory.

# Recipe

<RecipeFor id="conversion_monitor" />