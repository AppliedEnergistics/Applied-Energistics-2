---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Formation Plane
  icon: formation_plane
  position: 210
categories:
- devices
item_ids:
- ae2:formation_plane
---

# The Formation Plane

<GameScene zoom="8" background="transparent">
  <ImportStructure src="../assets/blocks/formation_plane.snbt" />
</GameScene>

The Formation Plane places blocks and drops items. It works similarly to an insert-only <ItemLink id="storage_bus" />,
placing/dropping when things are "stored" in it by [devices](../ae2-mechanics/devices.md) inserting into [network storage](../ae2-mechanics/import-export-storage.md),
like <ItemLink id="import_bus" />ses and <ItemLink id="interface" />s.

<GameScene zoom="8" interactive={true}>
  <ImportStructure src="../assets/assemblies/formation_plane_demonstration.snbt" />
  <IsometricCamera yaw="255" pitch="30" />
</GameScene>

Notice that these are similar to the import bus -> storage bus and interface -> storage bus pipes in [pipe subnets](../example-setups/pipe-subnet.md).

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/import_storage_pipe.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/interface_storage_pipe.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

This [device](../ae2-mechanics/devices.md) makes use of the mechanics used by storage busses in things like [pipe subnets](../example-setups/pipe-subnet.md),
and can replace storage busses in those setups if you want to drop items/place blocks instead of transport items.

They are [cable subparts](../ae2-mechanics/cable-subparts.md).

**REMEMBER TO ENABLE FAKE PLAYERS IN YOUR CHUNK CLAIM**

## Filtering

By default the plane will place/drop anything. Items inserted into its filter slots will act as a whitelist, only
allowing those specific items to be placed.

Items and fluids can be dragged into the slots from JEI/REI even if you don't actually have any of that item.

Right-click with a fluid container (like a bucket or fluid tank) to set that fluid as a filter instead of the bucket or tank item.

## Priority

Priorities can be set by clicking the wrench in the top-right of the GUI.
Items entering the network will start at the highest priority storage.

## Settings

*   The plane can be set to place blocks in-world or drop items

## Upgrades

The formation plane supports the following [upgrades](upgrade_cards.md):

*   <ItemLink id="capacity_card" /> increases the amount of filter slots
*   <ItemLink id="fuzzy_card" /> lets the plane filter by damage level and/or ignore item NBT
*   <ItemLink id="inverter_card" /> switches the filter from a whitelist to a blacklist

## Recipe

<RecipeFor id="formation_plane" />
