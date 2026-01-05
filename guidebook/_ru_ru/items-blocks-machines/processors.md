---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Процессоры
  icon: logic_processor
  position: 010
categories:
- misc ingredients blocks
item_ids:
- ae2:logic_processor
- ae2:calculation_processor
- ae2:engineering_processor
- ae2:printed_silicon
- ae2:printed_logic_processor
- ae2:printed_calculation_processor
- ae2:printed_engineering_processor
- ae2:silicon
---

# Процессоры

<Row>
  <ItemImage id="logic_processor" scale="4" />

  <ItemImage id="calculation_processor" scale="4" />

  <ItemImage id="engineering_processor" scale="4" />
</Row>

Процессоры — один из основных компонентов [устройств](../ae2-mechanics/devices.md) и машин мода «Прикладная энергетика 2». Они также являются одной из первых
серьёзных задач в области автоматизации. Существует три типа процессоров: из золота, <ItemLink id="certus_quartz_crystal" />,
и алмаза соответственно. Они создаются с помощью [печатей](presses.md) в <ItemLink id="inscriber" />, в многоэтапном
процессе (обычно с использованием серии высекателей и фильтрующего кабеля).

## Этапы производства

<Column gap="5">
  1.  Соберите/создайте необходимые ингредиенты: кремний, редстоуновую пыль, золото, <ItemLink id="certus_quartz_crystal" />, алмаз.

  <RecipeFor id="silicon" />

  <br />

  2.  Нажмите на необходимые компоненты печатной схемы.

  <Row>
    <RecipeFor id="printed_silicon" />

    <RecipeFor id="printed_logic_processor" />
  </Row>

  <Row>
    <RecipeFor id="printed_calculation_processor" />

    <RecipeFor id="printed_engineering_processor" />
  </Row>

  <br />

  3.  Окончательная сборка.

  <Row>
    <RecipeFor id="logic_processor" />

    <RecipeFor id="calculation_processor" />
  </Row>

  <RecipeFor id="engineering_processor" />
</Column>
