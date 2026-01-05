---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: ME Ячейки хранения
  icon: item_storage_cell_1k
  position: 410
categories:
- tools
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

# ME Ячейки хранения

<Column>
  <Row>
    <ItemImage id="item_storage_cell_1k" scale="4" />

    <ItemImage id="item_storage_cell_4k" scale="4" />

    <ItemImage id="item_storage_cell_16k" scale="4" />

    <ItemImage id="item_storage_cell_64k" scale="4" />

    <ItemImage id="item_storage_cell_256k" scale="4" />
  </Row>

  <Row>
    <ItemImage id="fluid_storage_cell_1k" scale="4" />

    <ItemImage id="fluid_storage_cell_4k" scale="4" />

    <ItemImage id="fluid_storage_cell_16k" scale="4" />

    <ItemImage id="fluid_storage_cell_64k" scale="4" />

    <ItemImage id="fluid_storage_cell_256k" scale="4" />
  </Row>
</Column>

ME Ячейки хранения — один из основных методов хранения в моде «Прикладная энергетика 2». Они размещаются в ячейках <ItemLink id="drive" />а
или <ItemLink id="chest" />а.

См. [байты и типы](../ae2-mechanics/bytes-and-types.md) для получения информации об их ёмкости в байтах и ​​типах.

ME Компоненты хранения можно извлечь из ME Корпуса, если ячейка пуста, удерживая [Shift + ПКМ] в руке.

## Ёмкость хранилища с различным количеством типов

[Первоначальная стоимость типов](../ae2-mechanics/bytes-and-types.md) такова, что ячейка, содержащая 1 тип, может содержать в 2 раза больше, чем ячейка, в которой используются все 63 типа.

| Ячейка                                     | Общая ёмкость ячеек при использовании одного типа | Общая ёмкость ячеек с использованием 63 типов |
| ------------------------------------------ | ------------------------------------------------: | --------------------------------------------: |
| <ItemLink id="item_storage_cell_1k" />     |                                             8,128 |                                         4,160 |
| <ItemLink id="item_storage_cell_4k" />     |                                            32,512 |                                        16,640 |
| <ItemLink id="item_storage_cell_16k" />    |                                           130,048 |                                        66,560 |
| <ItemLink id="item_storage_cell_64k" />    |                                           520,192 |                                       266,240 |
| <ItemLink id="item_storage_cell_256k" />   |                                         2,080,768 |                                     1,064,960 |


## Разделение

Ячейки можно фильтровать, чтобы принимать только определённые предметы, аналогично тому, как фильтруются предметы <ItemLink id="storage_bus" />. Это
делается в <ItemLink id="cell_workbench" />.

Предметы можно перетаскивать в слоты из JEI/REI, даже если у вас их нет.

## Улучшения

Ячейки хранения поддерживают следующие [улучшения](upgrade_cards.md), вставляемые через <ItemLink id="cell_workbench" />:

*   <ItemLink id="fuzzy_card" /> позволяет ME Шине фильтровать предметы по уровню повреждения и/или игнорировать NBT-предметы.
*   <ItemLink id="inverter_card" /> переключает фильтр с белого списка на чёрный список.
*   <ItemLink id="equal_distribution_card" /> выделяет одинаковое количество байтового пространства ячейки для каждого типа, поэтому один тип не может заполнить всю ячейку.
*   <ItemLink id="void_card" /> аннулирует помещённые предметы, если ячейка заполнена (или выделенное пространство для конкретного типа в случае карты с равномерным распределением),
    что полезно для предотвращения резервного копирования ферм. Будьте внимательны при разделении!
*   Портативные ячейки могут принимать <ItemLink id="energy_card" /> для увеличения ёмкости аккумулятора.

## Окрашивание

Переносные предметные и жидкостные ячейки можно покрасить так же, как кожаную броню, создав их вместе с красителями.

# Корпуса

Ячейки могут быть созданы с ME Компонентом и ME Корпусом или с использованием рецепта ME Корпуса вокруг ME Компонента:

<Row>
  <Recipe id="network/cells/item_storage_cell_1k" />

  <Recipe id="network/cells/item_storage_cell_1k_storage" />
</Row>

ME Корпуса сами по себе создаются следующим образом:

<Row>
  <RecipeFor id="item_cell_housing" />

  <RecipeFor id="fluid_cell_housing" />
</Row>

# ME Компоненты хранения

ME Компоненты хранения являются основой всех ячеек мода «Прикладная энергетика 2» и определяют их ёмкость. Каждый уровень увеличивает ёмкость
в 4 раза и стоит в 3 раза дороже предыдущего уровня.

<Column>
  <Row>
    <RecipeFor id="cell_component_1k" />

    <RecipeFor id="cell_component_4k" />

    <RecipeFor id="cell_component_16k" />
  </Row>

  <Row>
    <RecipeFor id="cell_component_64k" />

    <RecipeFor id="cell_component_256k" />
  </Row>
</Column>

# ME Предметная ячейка хранения

ME Предметные ячейки хранения могут вмещать до 63 различных типов предметов и доступны во всех стандартных вариантах ёмкости.

<Column>
  <Row>
    <Recipe id="network/cells/item_storage_cell_1k_storage" />

    <Recipe id="network/cells/item_storage_cell_4k_storage" />

    <Recipe id="network/cells/item_storage_cell_16k_storage" />
  </Row>

  <Row>
    <Recipe id="network/cells/item_storage_cell_64k_storage" />

    <Recipe id="network/cells/item_storage_cell_256k_storage" />
  </Row>
</Column>

## ME Переносная предметная ячейка хранения

Они служат как маленький <ItemLink id="chest" /> в кармане или как своего рода рюкзак. Их можно заряжать в <ItemLink id="charger" />е.

В отличие от стандартных ячеек хранения, ёмкость типов этих ячеек *уменьшается* по мере увеличения их ёмкости в байтах, и их общая ёмкость
в байтах составляет половину.

Помимо карт улучшения, которые могут принимать все ячейки, они также принимают <ItemLink id="energy_card" /> для обновления внутренних аккумуляторов.

<Column>
  <Row>
    <RecipeFor id="portable_item_cell_1k" />

    <RecipeFor id="portable_item_cell_4k" />

    <RecipeFor id="portable_item_cell_16k" />
  </Row>

  <Row>
    <RecipeFor id="portable_item_cell_64k" />

    <RecipeFor id="portable_item_cell_256k" />
  </Row>
</Column>

# ME Жидкостная ячейка хранения

ME Жидкостные ячейки хранения могут вмещать до 5 различных типов жидкостей и доступны во всех стандартных объёмах.

<Column>
  <Row>
    <Recipe id="network/cells/fluid_storage_cell_1k_storage" />

    <Recipe id="network/cells/fluid_storage_cell_4k_storage" />

    <Recipe id="network/cells/fluid_storage_cell_16k_storage" />
  </Row>

  <Row>
    <Recipe id="network/cells/fluid_storage_cell_64k_storage" />

    <Recipe id="network/cells/fluid_storage_cell_256k_storage" />
  </Row>
</Column>

## ME Переносная жидкостная ячейка хранения

Они служат как маленький <ItemLink id="chest" /> в кармане или как своего рода рюкзак. Их можно заряжать в <ItemLink id="charger" />е.

В отличие от стандартных ячеек хранения, ёмкость типов этих ячеек *уменьшается* по мере увеличения их ёмкости в байтах, и их общая ёмкость
в байтах составляет половину.

Помимо карт улучшения, которые могут принимать все ячейки, они также принимают <ItemLink id="energy_card" /> для обновления внутренних аккумуляторов.

<Column>
  <Row>
    <RecipeFor id="portable_fluid_cell_1k" />

    <RecipeFor id="portable_fluid_cell_4k" />

    <RecipeFor id="portable_fluid_cell_16k" />
  </Row>

  <Row>
    <RecipeFor id="portable_fluid_cell_64k" />

    <RecipeFor id="portable_fluid_cell_256k" />
  </Row>
</Column>

# ME Творческая предметная и жидкостная ячейки хранения

<Row>
  <ItemImage id="creative_item_cell" scale="2" />

  <ItemImage id="creative_fluid_cell" scale="2" />
</Row>

ME Творческие предметные и жидкостные ячейки **не обеспечивают бесконечное хранилище**. Вместо этого они служат бесконечными источниками и приёмниками любых предметов или жидкостей,
на которые вы их [разделяете](cell_workbench.md).
