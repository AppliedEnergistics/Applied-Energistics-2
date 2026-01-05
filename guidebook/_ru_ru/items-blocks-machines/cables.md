---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Кабели
  icon: fluix_glass_cable
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:white_glass_cable
- ae2:orange_glass_cable
- ae2:magenta_glass_cable
- ae2:light_blue_glass_cable
- ae2:yellow_glass_cable
- ae2:lime_glass_cable
- ae2:pink_glass_cable
- ae2:gray_glass_cable
- ae2:light_gray_glass_cable
- ae2:cyan_glass_cable
- ae2:purple_glass_cable
- ae2:blue_glass_cable
- ae2:brown_glass_cable
- ae2:green_glass_cable
- ae2:red_glass_cable
- ae2:black_glass_cable
- ae2:fluix_glass_cable
- ae2:white_covered_cable
- ae2:orange_covered_cable
- ae2:magenta_covered_cable
- ae2:light_blue_covered_cable
- ae2:yellow_covered_cable
- ae2:lime_covered_cable
- ae2:pink_covered_cable
- ae2:gray_covered_cable
- ae2:light_gray_covered_cable
- ae2:cyan_covered_cable
- ae2:purple_covered_cable
- ae2:blue_covered_cable
- ae2:brown_covered_cable
- ae2:green_covered_cable
- ae2:red_covered_cable
- ae2:black_covered_cable
- ae2:fluix_covered_cable
- ae2:white_covered_dense_cable
- ae2:orange_covered_dense_cable
- ae2:magenta_covered_dense_cable
- ae2:light_blue_covered_dense_cable
- ae2:yellow_covered_dense_cable
- ae2:lime_covered_dense_cable
- ae2:pink_covered_dense_cable
- ae2:gray_covered_dense_cable
- ae2:light_gray_covered_dense_cable
- ae2:cyan_covered_dense_cable
- ae2:purple_covered_dense_cable
- ae2:blue_covered_dense_cable
- ae2:brown_covered_dense_cable
- ae2:green_covered_dense_cable
- ae2:red_covered_dense_cable
- ae2:black_covered_dense_cable
- ae2:fluix_covered_dense_cable
- ae2:white_smart_cable
- ae2:orange_smart_cable
- ae2:magenta_smart_cable
- ae2:light_blue_smart_cable
- ae2:yellow_smart_cable
- ae2:lime_smart_cable
- ae2:pink_smart_cable
- ae2:gray_smart_cable
- ae2:light_gray_smart_cable
- ae2:cyan_smart_cable
- ae2:purple_smart_cable
- ae2:blue_smart_cable
- ae2:brown_smart_cable
- ae2:green_smart_cable
- ae2:red_smart_cable
- ae2:black_smart_cable
- ae2:fluix_smart_cable
- ae2:white_smart_dense_cable
- ae2:orange_smart_dense_cable
- ae2:magenta_smart_dense_cable
- ae2:light_blue_smart_dense_cable
- ae2:yellow_smart_dense_cable
- ae2:lime_smart_dense_cable
- ae2:pink_smart_dense_cable
- ae2:gray_smart_dense_cable
- ae2:light_gray_smart_dense_cable
- ae2:cyan_smart_dense_cable
- ae2:purple_smart_dense_cable
- ae2:blue_smart_dense_cable
- ae2:brown_smart_dense_cable
- ae2:green_smart_dense_cable
- ae2:red_smart_dense_cable
- ae2:black_smart_dense_cable
- ae2:fluix_smart_dense_cable
---

# Кабели

<GameScene zoom="3" background="transparent">
  <ImportStructure src="../assets/assemblies/cables.snbt" />
  <IsometricCamera yaw="180" pitch="30" />
</GameScene>

Хотя ME Сети также создаются смежными машинами с ME поддержкой, кабели являются основным способом
расширения ME Сети на большие площади.

Кабели разных цветов можно использовать для предотвращения соединения соседних кабелей друг с другом, что позволяет более эффективно
распределять [каналы](../ae2-mechanics/channels.md). Они также влияют на цвет подключаемых к ним ME Терминалов,
поэтому необязательно, чтобы все ваши ME Терминалы были фиолетовыми. Флюисовые кабели могут быть подключены к любым другим цветам.

Обратите внимание: **КАНАЛЫ НЕ ИМЕЮТ НИКАКОГО ОТНОШЕНИЯ К ЦВЕТУ КАБЕЛЯ**

## Важное примечание

**Если вы новичок в моде «Прикладная энергетика 2» и не знакомы с каналами, используйте умный кабель и умный плотный кабель везде, где это возможно.
Это покажет, как каналы маршрутизируются в вашей сети, и сделает их поведение более понятным.**

## Ещё одно примечание

**Это не «трубы» для предметов, жидкостей, энергии и т.п.** У них нет внутреннего инвентаря, поставщики шаблонов и машины не «помещают»
в них что-либо, они просто соединяют [устройства](../ae2-mechanics/devices.md) мода «Прикладная энергетика 2» в сети.

## Стеклянный кабель

<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/fluix_glass_cable.snbt" />
<IsometricCamera yaw="195" pitch="30" />
</GameScene>

<ItemLink id="fluix_glass_cable" /> — самый простой в создании кабель, передающий питание
и поддерживает до 8 [каналов](../ae2-mechanics/channels.md). Он создаётся в 17 различных цветах, по умолчанию
используется флюисовый, и может быть окрашен в любой цвет с использованием любого из 16 красителей.

Чтобы создать цветные кабели, окружите краску любого типа 8 кабелями того же
типа (цвет кабелей не имеет значения, но они должны быть однотипными,
стеклянными, умными и т.д.). Вы также можете покрасить кабели с помощью любой кисти/аппликатора,
совместимой с Forge.

Вы можете создать кабель любого цвета, используя ведро с водой, чтобы удалить краску.

Вы можете покрыть кабель шерстью, чтобы создать <ItemLink id="fluix_covered_cable" />, и создать <ItemLink id="fluix_smart_cable" />, чтобы получить лучшее представление о том, что происходит с
вашими [каналами](../ae2-mechanics/channels.md).

<RecipeFor id="fluix_glass_cable" />

<RecipeFor id="blue_glass_cable" />

## Покрытый кабель

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/fluix_covered_cable.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Вариант с покрытым кабелем не даёт никаких игровых преимуществ по сравнению с аналогом <ItemLink id="fluix_glass_cable" />. Однако его можно использовать
как альтернативный вариант, если вам больше нравится внешний вид с покрытым кабелем.

Можно раскрасить так же, как и <ItemLink id="fluix_glass_cable" />. Четыре <ItemLink id="fluix_covered_cable" /> можно создать из
редстоуновой и светокаменной пыли, чтобы получить <ItemLink id="fluix_covered_dense_cable" />.

<Recipe id="network/cables/covered_fluix" />

<RecipeFor id="blue_covered_cable" />

## Плотный кабель

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/fluix_covered_dense_cable.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Кабель повышенной ёмкости, может передавать 32 канала, в отличие от стандартного кабеля, который может передавать только 8,
однако он не поддерживает ME Шины, поэтому перед использованием необходимо сначала перейти с плотного кабеля на
кабель меньшего размера (например, <ItemLink id="fluix_glass_cable" /> или <ItemLink id="fluix_smart_cable" />).

Плотные кабели немного переопределяют поведение «кратчайшего пути» каналов: каналы выбирают кратчайший путь к плотному
кабелю, а затем кратчайший путь через этот плотный кабель к контроллеру.

<Recipe id="network/cables/dense_covered_fluix" />

<RecipeFor id="blue_covered_dense_cable" />

## Умный кабель

<Row>
<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/fluix_smart_cable.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>
<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/fluix_smart_dense_cable.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>
</Row>

Несмотря на некоторое сходство с <ItemLink id="fluix_covered_cable" /> по внешнему виду, они
выполняют диагностическую функцию, визуализируя использование каналов на кабелях.
Каналы отображаются в виде светящихся цветных линий, расположенных вдоль чёрной полосы на кабелях,
что позволяет понять, как используются каналы в вашей сети. Для обычных умных кабелей первые четыре канала отображаются линиями, соответствующими цвету кабеля,
а следующие четыре — белыми линиями. Для плотного умного кабеля каждая полоса соответствует 4 каналам.

В сетях с <ItemLink id="controller" />, линии на кабелях показывают точный маршрут каналов.

Умные кабели в специальных сетях вместо этого показывают количество каналов, используемых в сети, а не количество каналов, проходящих через этот конкретный кабель.

Их также можно покрасить так же, как <ItemLink id="fluix_glass_cable" />.

<Recipe id="network/cables/smart_fluix" />

<Recipe id="network/cables/dense_smart_fluix" />

<RecipeFor id="blue_smart_cable" />
