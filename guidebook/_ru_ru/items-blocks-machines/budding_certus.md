---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Цветущий блок истинного кварца
  icon: flawless_budding_quartz
  position: 010
categories:
- misc ingredients blocks
item_ids:
- ae2:flawless_budding_quartz
- ae2:flawed_budding_quartz
- ae2:chipped_budding_quartz
- ae2:damaged_budding_quartz
- ae2:small_quartz_bud
- ae2:medium_quartz_bud
- ae2:large_quartz_bud
- ae2:quartz_cluster
---

# Цветущий блок истинного кварца

(см. также [выращивание истинного кварца](../ae2-mechanics/certus-growth.md))

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/budding_blocks.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Бутоны истинного кварца прорастают из цветущих блоков истинного кварца, подобно аметисту. Их можно найти в [метеоритах](../ae2-mechanics/meteorites.md).
Существует 4 уровня цветущего блока истинного кварца: безупречный, испорченный, потрескавшийся, и повреждённый. Их проще всего определить
с помощью модов типа HWYLA, Jade, The One Probe и т. д. (или экрана F3).

У испорченного, потрескавшегося, и повреждённого цветущего истинного кварца, огда бутон вырастает на следующую стадию, у цветущего блока есть шанс
деградировать на один уровень, в конечном итоге превратившись в простой <ItemLink id="quartz_block" />.

Безупречный цветущий истинный кварц не будет деградировать от цветущих бутонов, и будет служить бесконечным источником.

Если сломать их с помощью обычной кирки, цветущие блоки истинного кварца деградируют на 1 уровень. Если сломать их с помощью
кирки с зачарование шёлкового касания, они не деградируют, если только они не были безупречными. ****Это означает, что безупречные цветущие блоки истинного кварца нельзя
поднять и переместить киркой**. Вместо этого можно использовать [пространственное хранилище](../ae2-mechanics/spatial-io.md) для
копирования и вставки безупречных цветущих блоков.

## Рецепты

Испорченный, потрескавшийся, и повреждённый цветуший истинный кварц можно создать, бросив предыдущий уровень цветущего блока (или <ItemLink id="quartz_block" />)
в воду с одним или несколькими <ItemLink id="charged_certus_quartz_crystal" />.

Безупречный цветуший истинный кварц невозможно создать вручную, его можно только найти в мире.

<Row>
  <RecipeFor id="damaged_budding_quartz" />

  <RecipeFor id="chipped_budding_quartz" />

  <RecipeFor id="flawed_budding_quartz" />
</Row>
