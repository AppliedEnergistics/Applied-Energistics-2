---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Создание многоблочного процессора (хранилище, блок совместной обработки, монитор, блок)
  icon: 1k_crafting_storage
  position: 210
categories:
- devices
item_ids:
- ae2:1k_crafting_storage
- ae2:4k_crafting_storage
- ae2:16k_crafting_storage
- ae2:64k_crafting_storage
- ae2:256k_crafting_storage
- ae2:crafting_accelerator
- ae2:crafting_monitor
- ae2:crafting_unit
---

# Создание процессора

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/crafting_cpus.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

<Row>
  <BlockImage id="1k_crafting_storage" scale="4" />

  <BlockImage id="crafting_accelerator" scale="4" />

  <BlockImage id="crafting_monitor" scale="4" />

  <BlockImage id="crafting_unit" scale="4" />
</Row>

Процессоры для создания управляют запросами/заданиями на создание. Они хранят промежуточные ингредиенты во время выполнения многоэтапных заданий
и влияют на объём работ и, в некоторой степени, на скорость их выполнения. Подробнее см. [автоматическое создание](../ae2-mechanics/autocrafting.md).

Каждый процессор для создания обрабатывает один запрос или задание, поэтому, если вы хотите одновременно запросить и вычислительный процессор, и 256 гладкого камня, вам понадобится два многоблочных процессора.

Их можно настроить на обработку запросов от игроков, автоматизации (ME Шин экспорта и ME Интерфейсов) или и того, и другого.

Щелчок [ПКМ] по одному из них открывает GUI статуса создания, в котором вы можете проверить ход выполнения задания по созданию, выполняемого процессором.

## Настройки

*   Процессор можно настроить на приём запросов только от игроков, только от автоматизации (например, запросов <ItemLink id="export_bus" /> с
    <ItemLink id="crafting_card" />), или от обоих.

## Конструкция

Процессоры для создания являются многоблочными и должны представлять собой сплошные прямоугольные призмы без зазоров. Они состоят из нескольких компонентов.

Каждый процессор должен содержать как минимум один блок хранилища для создания (а минимально допустимый процессор — это всего лишь один блок хранилища для создания объёмом 1КБ).

# Блок для создания

<BlockImage id="crafting_unit" scale="4" />

(Необязательно) Блоки для создания просто заполняют пространство в процессоре, превращая его в сплошную прямоугольную призму, если у вас недостаточно
других компонентов. Они также являются базовыми ингредиентами для других компонентов.

<RecipeFor id="crafting_unit" />

# Хранилище для создания

<Row>
  <BlockImage id="1k_crafting_storage" scale="4" />

  <BlockImage id="4k_crafting_storage" scale="4" />

  <BlockImage id="16k_crafting_storage" scale="4" />

  <BlockImage id="64k_crafting_storage" scale="4" />

  <BlockImage id="256k_crafting_storage" scale="4" />
</Row>

(Обязательно) Хранилища для создания доступны во всех стандартных размерах ячеек (1КБ, 4КБ, 16КБ, 64КБ, 256КБ). В них хранятся ингредиенты и
промежуточные продукты, используемые при создании, поэтому процессору требуется больше хранилищ для обработки
большего количества ингредиентов.

<Column>
  <Row>
    <RecipeFor id="1k_crafting_storage" />

    <RecipeFor id="4k_crafting_storage" />

    <RecipeFor id="16k_crafting_storage" />
  </Row>

  <Row>
    <RecipeFor id="64k_crafting_storage" />

    <RecipeFor id="256k_crafting_storage" />
  </Row>
</Column>

# Блок совместной обработки для создания

<BlockImage id="crafting_accelerator" scale="4" />

(Необязательно) Блоки совместной обработки для создания позволяют системе чаще отправлять партии ингредиентов из <ItemLink id="pattern_provider" />.
Это позволяет им работать с машинами, которые выполняют быструю обработку. Примером этого может служить поставщик шаблонов, окружённый
<ItemLink id="molecular_assembler" />, который может выдавать ингредиенты быстрее, чем может обработать один сборщик, и, таким образом,
распределять партии ингредиентов между окружающими сборщиками.

<RecipeFor id="crafting_accelerator" />

# Монитор для создания

<BlockImage id="crafting_monitor" scale="4" />

(Необязательно) Монитор для создания отображает работу/задачу, которую выполняет процессор в данный момент.
Монитор можно покрасить с помощью <ItemLink id="color_applicator" />.

<RecipeFor id="crafting_monitor" />
