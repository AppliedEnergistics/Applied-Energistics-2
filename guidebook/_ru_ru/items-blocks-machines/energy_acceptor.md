---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Приёмник энергии
  icon: energy_acceptor
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:energy_acceptor
---

# Приёмник энергии

<Row gap="20">
<BlockImage id="energy_acceptor" scale="8" /> 

<GameScene zoom="8" background="transparent">
  <ImportStructure src="../assets/blocks/cable_energy_acceptor.snbt" />
</GameScene>
</Row>

Приёмник энергии преобразует распространённые формы энергии из других технологических модов во внутреннюю форму [энергии](../ae2-mechanics/energy.md),
AE. Хотя <ItemLink id="controller" /> тоже может это делать, ME Контроллеры имеют большую ценность, поэтому зачастую лучше использовать
приёмник энергии.

Коэффициенты преобразования энергии Forge и энергии Techreborn:

*   2 FE = 1 AE (Forge)
*   1 E  = 2 AE (Fabric)

Скорость преобразования полностью зависит от того, сколько энергии AE может хранить ваша сеть, по причинам, которые описаны на
[этой странице](../ae2-mechanics/energy.md).

## Варианты

Приёмники энергии бывают двух видов: обычные и плоские/[подчасть](../ae2-mechanics/cable-subparts.md). Это позволяет сделать некоторые установки более компактными.

## Рецепт

<RecipeFor id="energy_acceptor" />

<RecipeFor id="cable_energy_acceptor" />