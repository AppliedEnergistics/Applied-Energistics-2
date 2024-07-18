---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: ME Import Bus
  icon: import_bus
  position: 220
categories:
- devices
item_ids:
- ae2:import_bus
---

# The Import Bus

<GameScene zoom="8" background="transparent">
<ImportStructure src="../assets/blocks/import_bus.snbt" />
</GameScene>

The import bus pulls items and fluids (and whatever else, given addons) from the inventory it's touching and pushes them into
[network storage](../ae2-mechanics/import-export-storage.md).

For purposes of lag reduction, if the import bus has not imported something recently, it goes into a sort of
"sleep mode" where it operates slowly, and wakes up and accelerates to full speed (4 operations per second) when it successfully imports something.

They are [cable subparts](../ae2-mechanics/cable-subparts.md).

## Filtering

By default the bus will import anything it has access to. Items inserted into its filter slots will act as a whitelist, only
allowing those specific items to be imported.

Items and fluids can be dragged into the slots from JEI/REI even if you don't actually have any of that item.

Right-click with a fluid container (like a bucket or fluid tank) to set that fluid as a filter instead of the bucket or tank item.

## Upgrades

The import bus supports the following [upgrades](upgrade_cards.md):

*   <ItemLink id="capacity_card" /> increases the amount of filter slots
*   <ItemLink id="speed_card" /> increases the amount of stuff moved per operation
*   <ItemLink id="fuzzy_card" /> lets the bus filter by damage level and/or ignore item NBT
*   <ItemLink id="inverter_card" /> switches the filter from a whitelist to a blacklist
*   <ItemLink id="redstone_card" /> adds redstone control, allowing active on high signal, low signal, or once per pulse

## Speeds

| Acceleration Cards | Items Moved per Operation |
|:-------------------|:--------------------------|
| 0                  | 1                         |
| 1                  | 8                         |
| 2                  | 32                        |
| 3                  | 64                        |
| 4                  | 96                        |

## Recipe

<RecipeFor id="import_bus" />
