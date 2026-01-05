---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Плоскость уничтожения
  icon: annihilation_plane
  position: 210
categories:
- devices
item_ids:
- ae2:annihilation_plane
---

# Плоскость уничтожения

<GameScene zoom="8" background="transparent">
<ImportStructure src="../assets/blocks/annihilation_plane.snbt" />
</GameScene>

Плоскость уничтожения ломает блоки и подбирает предметы. Она работает аналогично <ItemLink id="import_bus" />, помещая предметы
в [сетевое хранилище](../ae2-mechanics/import-export-storage.md). Чтобы подобрать предметы, они должны столкнуться
с поверхностью плоскости, она не подбирает предметы в определённой области.

Плоскости уничтожения можно зачаровать любыми зачарованиями для кирки, так что да, вы можете наложить безумные уровни удачи и
[автоматизировать удачу руды](../example-setups/ore-fortuner.md), если ваш модпак это позволяет. Кроме того, шёлковое касание делает то,
что и ожидалось: эффективность снижает затраты энергии на ломание блока, а прочность даёт шанс не тратить энергию вообще.

Это [подчасти кабеля](../ae2-mechanics/cable-subparts.md).

**НЕ ЗАБУДЬТЕ ВКЛЮЧИТЬ ПОДДЕЛЬНЫХ ИГРОКОВ НА ДОБАВЛЕНИЕ В ВАШ ЧАНК**

## Фильтрация

Плоскость уничтожения сломает блок или подберёт предмет только в том случае, если она сможет сохранить полученное выпадение/предметы
в своей сети. Это означает, что для фильтрации необходимо ограничить количество хранимых предметов в сети, скорее всего,
поместив в [подсеть](../ae2-mechanics/subnetworks.md). Для достижения этой цели <ItemLink id="storage_bus" /> или [ячейка](../items-blocks-machines/storage_cells.md)
могут быть [разделены](cell_workbench.md).

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/annihilation_filtering.snbt" />

  <DiamondAnnotation pos="1 0.5 0.5" color="#00ff00">
        Фильтруется по всему, что выпадает из того, что вы хотите сломать.
  </DiamondAnnotation>

  <DiamondAnnotation pos=".5 0.5 2.5" color="#00ff00">
        Разделяет по всему, что выпадает из того, что вы хотите сломать.
  </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Опять же, фильтрация производится *по выпавшим предметам*. Например, если вы хотите отфильтровать ломание <ItemLink id="minecraft:amethyst_cluster" />,
вам нужна плоскость с зачарованием шёлкового касания. В противном случае на каждой предыдущей стадии роста ничего не выпадет, и плоскость всё равно это сломает,
поскольку сеть всегда может хранить «ничего».

## Рецепт

<RecipeFor id="annihilation_plane" />
