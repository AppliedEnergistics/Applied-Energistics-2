---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Фасады
  icon: facade
  icon_nbt: '{item: "minecraft:stone"}'
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:facade
---

# Фасады

Фасады могут быть использованы для придания вашему основанию более чистого вида. Они могут закрывать кабель обоих размеров и создаваться из различных
видов блоков.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/facades_1.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Они могут закрывать все стороны кабеля, но при этом [подчасти](../ae2-mechanics/cable-subparts.md) и соединения кабеля
будут выступать.

<GameScene zoom="6"  interactive={true}>
  <ImportStructure src="../assets/assemblies/facades_2.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Используйте их с умом, чтобы улучшить эстетику вашей базы или сделайте блоки с разными текстурами с каждой стороны.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../assets/assemblies/facades_3.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Скрытие фасадов

Фасады будут скрыты, если вы держите в любой руке <a href="network_tool.md">сетевой инструмент</a>.

Вы можете взаимодействовать с блоками, расположенными за скрытыми фасадами, без необходимости предварительно удалять фасады.

## Рецепт

Поместите блок, для которого вы хотите создать текстуру в середину и с каждой стороны блока поместите по одному <ItemLink id="cable_anchor" />.

![Facade Recipe](../assets/diagrams/facade_recipe.png)
