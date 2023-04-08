---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: ME IO Port
  icon: io_port
  position: 210
item_ids:
- ae2:io_port
---
# The ME I/O Port

<BlockImage id="io_port" p:powered="true" scale="8" />

The IO Port allows you to rapidly fill or empty [storage cells](../items-blocks-machines/storage_cells.md) to or from
[network storage](../ae2-mechanics/import-export-storage.md).

It can be rotated with a <ItemLink id="certus_quartz_wrench" />.

# Settings

- The IO Port can be set to move the cell to the output slots when the cell is empty, full, or when the work is done.
- If a <ItemLink id="redstone_card" /> is inserted, there will be options for various redstone conditions
- In the center of the GUI, there is an arrow to set which direction to transfer items, from the cell to [network storage](../ae2-mechanics/import-export-storage.md),
or from storage to the cell.

# Upgrades

The IO Port supports the following [upgrades](upgrade_cards.md):

- <ItemLink id="speed_card" /> increases the amount of stuff moved per operation
- <ItemLink id="redstone_card" /> adds redstone control, allowing active on high signal, low signal, or once per pulse

# Recipe

<RecipeFor id="io_port" />