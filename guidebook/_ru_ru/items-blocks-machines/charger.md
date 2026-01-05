---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Зарядник
  icon: charger
  position: 310
categories:
- machines
item_ids:
- ae2:charger
---

# Зарядник

<BlockImage id="charger" scale="8" />

Зарядник позволяет заряжать
поддерживаемые инструменты и <ItemLink id="certus_quartz_crystal" />.

Питание может подаваться сверху или снизу, через [кабели](cables.md) мода «Прикладная энергетика 2», или кабели питания других модов. Он может
принимать как питание AE2 (AE), так и энергию Forge (FE). Предметы можно вставлять и вынимать с любой стороны. Можно извлечь только результаты,
поэтому фильтры, препятствующие извлечению кристаллов истинного кварца, не нужны. Можно вращать с помощью
<ItemLink id="certus_quartz_wrench" /> для автоматизации процесса.

Можно использовать для создания <ItemLink id="charged_certus_quartz_crystal" />
из <ItemLink id="certus_quartz_crystal" />, и <ItemLink id="meteorite_compass" /> из <ItemLink id="minecraft:compass" />.

Чтобы зарядить его вручную, поместите <ItemLink id="crank" /> сверху или снизу и щёлкайте [ПКМ], пока он не зарядится.

Он также служит рабочей станцией для жителя мода «Прикладная энергетика 2».

## Простая автоматизация

Например, возможность вращения позволяет вам полуавтоматизировать зарядные устройства следующим образом:

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/charger_hopper.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Рецепт

<RecipeFor id="charger" />
