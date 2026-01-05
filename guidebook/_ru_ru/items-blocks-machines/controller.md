---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: ME Контроллер
  icon: controller
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:controller
---

# ME Контроллер

<BlockImage id="controller" p:state="online" scale="8" />

ME Контроллер — это маршрутизатор [ME Сети](../ae2-mechanics/me-network-connections.md).
Без него сеть работает по принципу «специальная» и может содержать не более 8 [устройств](../ae2-mechanics/devices.md), использующих каналы.

Невозможно иметь 2 ME Контроллера в одной [ME Сети](../ae2-mechanics/me-network-connections.md).

ME Контроллер обеспечивает 32 [канала](../ae2-mechanics/channels.md) на каждую сторону.

Для работы ME Контроллера требуется 6 AE/t на каждый блок ME Контроллера.
Каждый блок ME Контроллера может хранить 8000 AE, поэтому для более крупных сетей может потребоваться дополнительное
хранилище энергии. Подробнее см. [энергия](../ae2-mechanics/energy.md).

Многоблочные ME Контроллеры могут быть построены в достаточно свободной форме.

<GameScene zoom="2" background="transparent">
  <ImportStructure src="../assets/assemblies/controllers.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Однако есть несколько правил, которые необходимо соблюдать:

1.  Все блоки ME Контроллера в [ME Сети](../ae2-mechanics/me-network-connections.md) должны быть подключены; в противном случае блоки станут красными.
2.  Размер ME Контроллера должен быть в пределах 7x7x7; в противном случае он станет красным.
3.  ME Контроллер может иметь 2 смежных блока не более чем на одной оси; если блок нарушает это правило, он отключается и становится красным.

<GameScene zoom="2" background="transparent">
  <ImportStructure src="../assets/assemblies/controller_rules.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Если все правила соблюдены и ME Контроллер подключен к сети, он должен светиться и
попеременно менять цвета.

Вы можете щёлкнуть [ПКМ] по ME Контроллеру, чтобы получить тот же GUI, что и <ItemLink id="network_tool" />.

## Рецепт

<RecipeFor id="controller" />
