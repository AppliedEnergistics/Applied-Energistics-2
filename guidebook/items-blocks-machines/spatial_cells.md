---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Spatial Cells
  icon: spatial_storage_cell_128
  position: 410
categories:
- tools
item_ids:
- ae2:spatial_storage_cell_2
- ae2:spatial_storage_cell_16
- ae2:spatial_storage_cell_128
- ae2:spatial_cell_component_2
- ae2:spatial_cell_component_16
- ae2:spatial_cell_component_128
---

# Spatial Storage Cells

  <Row>
    <ItemImage id="spatial_storage_cell_2" scale="4" />

    <ItemImage id="spatial_storage_cell_16" scale="4" />

    <ItemImage id="spatial_storage_cell_128" scale="4" />
  </Row>

Spatial Storage Cells are used to [store physical volumes of space](../ae2-mechanics/spatial-io.md). 
They are used in a <ItemLink id="spatial_io_port" />.

Unlike [Storage Cells](../items-blocks-machines/storage_cells.md), spatial cells cannot be reformatted.

Again, **YOU CANNOT RESET, REFORMAT, OR RESIZE A SPATIAL CELL AFTER IT HAS BEEN USED.** Make a new cell if you want to use different dimensions.


## Recipes

  <Row>
    <Recipe id="network/cells/spatial_storage_cell_2_cubed_storage" />

    <Recipe id="network/cells/spatial_storage_cell_16_cubed_storage" />

    <Recipe id="network/cells/spatial_storage_cell_128_cubed_storage" />
  </Row>

# Housings

Cells can be made with a spatial component and a housing or with the housing recipe around a spatial component:

<Row>
  <Recipe id="network/cells/spatial_storage_cell_2_cubed" />

  <Recipe id="network/cells/spatial_storage_cell_2_cubed_storage" />
</Row>

Housings by themselves are crafted like so:

  <RecipeFor id="item_cell_housing" />

# Spatial Components

Spatial Components are the core of spatial storage cells. Each tier increases the dimensions of the volume that can be
stored by factor of 8.

  <Row>
    <RecipeFor id="spatial_cell_component_2" />

    <RecipeFor id="spatial_cell_component_16" />

    <RecipeFor id="spatial_cell_component_128" />
  </Row>