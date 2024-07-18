---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Toggle Bus
  icon: toggle_bus
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:toggle_bus
- ae2:inverted_toggle_bus
---

# The Toggle Bus

<GameScene zoom="8" background="transparent">
<ImportStructure src="../assets/assemblies/toggle_bus.snbt" />
<IsometricCamera yaw="195" pitch="30" />
</GameScene>

A bus which functions similarly to <ItemLink id="fluix_glass_cable" /> or other cables, but it
allows its connection state to be toggled via redstone. This allows you to cut
off a section of a [ME Network](../ae2-mechanics/me-network-connections.md).

When redstone signal supplied the part enables the connection, <ItemLink id="inverted_toggle_bus" /> provides the reverse
behavior by disabling the connection instead.

Of note, toggling these may cause the network to reboot and recalculate the connected devices.

They are [cable subparts](../ae2-mechanics/cable-subparts.md).

## Recipes

<RecipeFor id="toggle_bus" />

<RecipeFor id="inverted_toggle_bus" />
