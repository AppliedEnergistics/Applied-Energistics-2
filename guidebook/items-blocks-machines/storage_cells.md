---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Storage Cells
  icon: item_storage_cell_1k
  position: 410
item_ids:
- ae2:item_cell_housing
- ae2:fluid_cell_housing
- ae2:cell_component_1k
- ae2:cell_component_4k
- ae2:cell_component_16k
- ae2:cell_component_64k
- ae2:cell_component_256k
- ae2:item_storage_cell_1k
- ae2:item_storage_cell_4k
- ae2:item_storage_cell_16k
- ae2:item_storage_cell_64k
- ae2:item_storage_cell_256k
- ae2:fluid_storage_cell_1k
- ae2:fluid_storage_cell_4k
- ae2:fluid_storage_cell_16k
- ae2:fluid_storage_cell_64k
- ae2:fluid_storage_cell_256k
---
# Storage Cells

<Column>
<Row>
<ItemImage id="item_storage_cell_1k" scale="4" /><ItemImage id="item_storage_cell_4k" scale="4" /><ItemImage id="item_storage_cell_16k" scale="4" />
<ItemImage id="item_storage_cell_64k" scale="4" /><ItemImage id="item_storage_cell_256k" scale="4" />
</Row>

<Row>
<ItemImage id="fluid_storage_cell_1k" scale="4" /><ItemImage id="fluid_storage_cell_4k" scale="4" /><ItemImage id="fluid_storage_cell_16k" scale="4" />
<ItemImage id="fluid_storage_cell_64k" scale="4" /><ItemImage id="fluid_storage_cell_256k" scale="4" />
</Row>
</Column>

Storage Cells are one of the primary methods of storage in Applied Energistics. They go in <ItemLink id="drive" />s
or <ItemLink id="chest" />s.

See [Bytes and Types](../ae2-mechanics/bytes-and-types.md) for an explanation of their capacities in bytes and types.

Storage components can be removed from the housing if the cell is empty by shift-right clicking with the cell in your hand.

# Housings

Cells can be made with a storage component and a housing or with the housing recipe around a storage component:

<Row><Recipe id="network/cells/item_storage_cell_1k" /><Recipe id="network/cells/item_storage_cell_1k_storage" /></Row>

Housings by themselves are crafted like so:

<Row><RecipeFor id="item_cell_housing" /><RecipeFor id="fluid_cell_housing" /></Row>

# Storage Components

Storage Components are the core of all AE2 cells, determining the capacity of the cells. Each tier increases the capacity
by 4x and costs 3 of the previous tier.
<Column>
<Row><RecipeFor id="cell_component_1k" /><RecipeFor id="cell_component_4k" /><RecipeFor id="cell_component_16k" /></Row>
<Row><RecipeFor id="cell_component_64k" /><RecipeFor id="cell_component_256k" /></Row>
</Column>

# Partitioning

Cells can be filtered to only accept certain items, similar to how <ItemLink id="storage_bus" />ses can be filtered. This is
done in a <ItemLink id="cell_workbench" />.

Items can be dragged into the slots from JEI/REI even if you don't actually have any of that item.

# Upgrades

Storage cells support the following upgrades, inserted via a <ItemLink id="cell_workbench" />:

- <ItemLink id="fuzzy_card" /> (not available on fluid cells) lets the cell be partitioned by damage level and/or ignore item NBT
- <ItemLink id="inverter_card" /> switches the filter from a whitelist to a blacklist
- <ItemLink id="equal_distribution_card" /> allocates the same amount of cell byte space to each type, so one type cannot fill up the entire cell
- <ItemLink id="void_card" /> voids items inserted if the cell is full (or that specific type's allocated space in the
case of an equal distribution card), useful for stopping farms from backing up. Be careful to partition this!
- Portable cells can accept <ItemLink id="energy_card" /> in order to increase their battery capacity

# Item Storage Cells

Item storage cells can hold up to 63 distinct types of items, and are available in all the standard capacities.
<Column>
<Row><Recipe id="network/cells/item_storage_cell_1k_storage" /><Recipe id="network/cells/item_storage_cell_4k_storage" /><Recipe id="network/cells/item_storage_cell_16k_storage" /></Row>
<Row><Recipe id="network/cells/item_storage_cell_64k_storage" /><Recipe id="network/cells/item_storage_cell_256k_storage" /></Row>
</Column>

# Portable Item Storage

These act as a tiny <ItemLink id="chest" /> in your pocket, or like a form of backpack. They can be charged in a <ItemLink id="charger" />

Unlike standard storage cells, these actually *reduce* in type capacity as their byte capacity increases, and have half the
total byte capacity.

In addition to the upgrade cards all cells can receive, these also accept <ItemLink id="energy_card" />s to upgrade their internal batteries.
<Column>
<Row><RecipeFor id="portable_item_cell_1k" /><RecipeFor id="portable_item_cell_4k" /><RecipeFor id="portable_item_cell_16k" /></Row>
<Row><RecipeFor id="portable_item_cell_64k" /><RecipeFor id="portable_item_cell_256k" /></Row>
</Column>

# Fluid Storage Cells

Fluid storage cells can hold up to 5 distinct types of fluids, and are available in all the standard capacities.
<Column>
<Row><Recipe id="network/cells/fluid_storage_cell_1k_storage" /><Recipe id="network/cells/fluid_storage_cell_4k_storage" /><Recipe id="network/cells/fluid_storage_cell_16k_storage" /></Row>
<Row><Recipe id="network/cells/fluid_storage_cell_64k_storage" /><Recipe id="network/cells/fluid_storage_cell_256k_storage" /></Row>
</Column>

# Portable Fluid Storage

These act as a tiny <ItemLink id="chest" /> in your pocket, or like a form of backpack. They can be charged in a <ItemLink id="charger" />

Unlike standard storage cells, these actually *reduce* in type capacity as their byte capacity increases, and have half the
total byte capacity.

In addition to the upgrade cards all cells can receive, these also accept <ItemLink id="energy_card" />s to upgrade their internal batteries.
<Column>
<Row><RecipeFor id="portable_fluid_cell_1k" /><RecipeFor id="portable_fluid_cell_4k" /><RecipeFor id="portable_fluid_cell_16k" /></Row>
<Row><RecipeFor id="portable_fluid_cell_64k" /><RecipeFor id="portable_fluid_cell_256k" /></Row>
</Column>

# Coloring

Portable item and fluid cells can be colored similar to leather armor, by crafting them together with dyes.

# Creative Item and Fluid Cells

<Row><ItemImage id="creative_item_cell" scale="2" /><ItemImage id="creative_fluid_cell" scale="2" /></Row>

Creative item and fluid cells **do not provide infinite storage**. Instead, they act as infinite sources and sinks of whatever
item or fluid you [partition](cell_workbench.md) them to.