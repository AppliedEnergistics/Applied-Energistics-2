---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Пространственные ячейки
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

# Пространственные ячейки хранения

  <Row>
    <ItemImage id="spatial_storage_cell_2" scale="4" />

    <ItemImage id="spatial_storage_cell_16" scale="4" />

    <ItemImage id="spatial_storage_cell_128" scale="4" />
  </Row>

Пространственные ячейки хранения используются для [хранения физических объёмов пространства](../ae2-mechanics/spatial-io.md). 
Они используются в <ItemLink id="spatial_io_port" />.

В отличие от [ячеек хранения](../items-blocks-machines/storage_cells.md), пространственные ячейки хранения нельзя переформатировать.

Опять же, **ВЫ НЕ МОЖЕТЕ СБРОСИТЬ, ПЕРЕФОРМАТИРОВАТЬ ИЛИ ИЗМЕНИТЬ РАЗМЕР ПРОСТРАНСТВЕННОЙ ЯЧЕЙКИ ПОСЛЕ ЕЁ ИСПОЛЬЗОВАНИЯ.** Создайте новую ячейку, если хотите использовать другие измерения.


## Рецепты

  <Row>
    <Recipe id="network/cells/spatial_storage_cell_2_cubed_storage" />

    <Recipe id="network/cells/spatial_storage_cell_16_cubed_storage" />

    <Recipe id="network/cells/spatial_storage_cell_128_cubed_storage" />
  </Row>

# Корпуса

Ячейки могут быть созданы с использованием компонента и ME Корпуса или с использованием рецепта ME Корпуса вокруг компонента:

<Row>
  <Recipe id="network/cells/spatial_storage_cell_2_cubed" />

  <Recipe id="network/cells/spatial_storage_cell_2_cubed_storage" />
</Row>

Корпуса сами по себе создаются следующим образом:

  <RecipeFor id="item_cell_housing" />

# Пространственные компоненты

Пространственные компоненты являются основой пространственной ячейки хранения. Каждый уровень увеличивает размеры объёма,
который может быть сохранен, в 8 раз.

  <Row>
    <RecipeFor id="spatial_cell_component_2" />

    <RecipeFor id="spatial_cell_component_16" />

    <RecipeFor id="spatial_cell_component_128" />
  </Row>