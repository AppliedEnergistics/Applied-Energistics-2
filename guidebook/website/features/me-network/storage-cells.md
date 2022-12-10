---
navigation:
  parent: website/index.md
  title: Storage Cells
  icon: item_storage_cell_64k
item_ids:
  - ae2:fluid_storage_cell_1k
  - ae2:fluid_storage_cell_4k
  - ae2:fluid_storage_cell_16k
  - ae2:fluid_storage_cell_64k
---

Storage Cells, are one of the core mechanics of storage in Applied Energistics
2, there are three kinds: one for items, one for fluids, and one for regions of
space.

## Item Storage Cells

Item storage cells can hold up to 63 distinct types of items; the
number of items they can store depends in part on how many types they're
holding, and their storage capacity.

<ItemGrid>
  <ItemIcon id="item_storage_cell_1k" />
  <ItemIcon id="item_storage_cell_4k" />
  <ItemIcon id="item_storage_cell_16k" />
  <ItemIcon id="item_storage_cell_64k" />
</ItemGrid>

### Portable Item Storage

<ItemGrid>
  <ItemIcon id="portable_item_cell_1k" />
  <ItemIcon id="portable_item_cell_4k" />
  <ItemIcon id="portable_item_cell_16k" />
  <ItemIcon id="portable_item_cell_64k" />
</ItemGrid>

## Fluid Storage Cells

Fluid storage cells can hold up to 5 distinct types of fluids; the
volume of fluid they can store depends in part on how many types they're
holding, and their storage capacity.

<ItemGrid>
  <ItemIcon id="fluid_storage_cell_1k" />
  <ItemIcon id="fluid_storage_cell_4k" />
  <ItemIcon id="fluid_storage_cell_16k" />
  <ItemIcon id="fluid_storage_cell_64k" />
</ItemGrid>

### Portable Fluid Storage

<ItemGrid>
  <ItemIcon id="portable_fluid_cell_1k" />
  <ItemIcon id="portable_fluid_cell_4k" />
  <ItemIcon id="portable_fluid_cell_16k" />
  <ItemIcon id="portable_fluid_cell_64k" />
</ItemGrid>

## Capacity Limits

Storage cells have limits of size, and limits
of types, plus you need to consider the resource usage of your cells, to
decide what your best options are. Each storage cell can store a fixed amount
of data. Each type consumes a number of bytes (which varies with the cell
size), and each item consumes one bit of storage, so eight items consume one
byte, and a full stack of 64 consumes 8 bytes, regardless of how the item
would stack outside an ME network. For instance, 64 identical saddles don't
take up more space than 64 stone.

Gunning straight for top tier storage cells, is not generally the best idea,
since you use more resources, but don't get any extra type storage.

Below is a table comparing the different tiers of storage cells, how much they store, and
a rough estimate of their cost.

### Storage Cell Contents Vs Cost

| Cell                                    |  Bytes | Types | Byte/Type | C-Quartz | N-Quartz | Gold | Diamonds |
|-----------------------------------------|-------:|------:|----------:|---------:|---------:|-----:|---------:|
| <ItemLink id="item_storage_cell_1k" />  |  1,024 |    63 |         8 |        5 |        5 |    1 |        0 |
| <ItemLink id="item_storage_cell_4k" />  |  4,096 |    63 |        32 |       17 |        5 |    3 |        0 |
| <ItemLink id="item_storage_cell_16k" /> | 16,384 |    63 |       128 |       51 |       10 |    9 |        1 |
| <ItemLink id="item_storage_cell_64k" /> | 65,536 |    63 |       512 |      153 |       20 |   27 |        4 |

### Storage Capacity with Varying Type Count

| Cell                                    | Stacks of items With 1 Item In Cell | Stacks of items With 63 Items in Cell |
|-----------------------------------------|------------------------------------:|--------------------------------------:|
| <ItemLink id="item_storage_cell_1k" />  |                                 127 |                                    65 |
| <ItemLink id="item_storage_cell_4k" />  |                                 508 |                                   260 |
| <ItemLink id="item_storage_cell_16k" /> |                               2,032 |                                 1,040 |
| <ItemLink id="item_storage_cell_64k" /> |                               8,128 |                                 4,160 |

## Spatial Storage

Storage cells for spatial I/O come in three sizes.

<ItemGrid>
  <ItemIcon id="spatial_storage_cell_2" />
  <ItemIcon id="spatial_storage_cell_16" />
  <ItemIcon id="spatial_storage_cell_128" />
</ItemGrid>
