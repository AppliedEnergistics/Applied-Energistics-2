---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: ME Шина переключения
  icon: toggle_bus
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:toggle_bus
- ae2:inverted_toggle_bus
---

# ME Шина переключения

<GameScene zoom="8" background="transparent">
<ImportStructure src="../assets/assemblies/toggle_bus.snbt" />
<IsometricCamera yaw="195" pitch="30" />
</GameScene>

ME Шина переключения, функционирующая аналогично <ItemLink id="fluix_glass_cable" /> или другим кабелям, но
позволяющая переключать состояние своего соединения с помощью редстоуна. Это позволяет
отрезать участок [ME Сети](../ae2-mechanics/me-network-connections.md).

При подаче сигнала редстоуна активируется подключение, <ItemLink id="inverted_toggle_bus" /> обеспечивает обратное
поведение, отключая соединение.

Следует отметить, что переключение этих параметров может привести к перезагрузке сети и пересчёту подключённых устройств.

Они являются [подчастями кабеля](../ae2-mechanics/cable-subparts.md).

## Рецепты

<RecipeFor id="toggle_bus" />

<RecipeFor id="inverted_toggle_bus" />
