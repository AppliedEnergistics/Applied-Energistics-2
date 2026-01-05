---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: ME Беспроводная точка доступа
  icon: wireless_access_point
  position: 210
categories:
- devices
item_ids:
- ae2:wireless_booster
- ae2:wireless_access_point
---

# ME Беспроводная точка доступа

<BlockImage id="wireless_access_point" p:state="has_channel" scale="8" />

Разрешает беспроводной доступ через <ItemLink id="wireless_terminal" />.
Дальность действия и энергопотребление определяются количеством установленных <ItemLink id="wireless_booster" />.

В сети может быть любое количество точек беспроводного доступа с любым количеством
<ItemLink id="wireless_booster" /> в каждой, что позволяет оптимизировать энергопотребление
и дальность действия, изменяя настройки.

Требуется [канал](../ae2-mechanics/channels.md).

Также используется для привязки [беспроводных терминалов](wireless_terminals.md).

# Беспроводной усилитель

<ItemImage id="wireless_booster" scale="2" />

Используется для увеличения радиуса действия беспроводной точки доступа.

## Рецепты

<RecipeFor id="wireless_access_point" />

<RecipeFor id="wireless_booster" />
