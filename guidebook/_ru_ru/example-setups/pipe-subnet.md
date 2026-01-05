---
navigation:
  parent: example-setups/example-setups-index.md
  title: Подсеть кабеля для предметов/жидкостей
  icon: storage_bus
---

# Подсеть кабеля для предметов/жидкостей

Простой метод эмуляции предмета и/или кабеля с жидкостью с помощью мода «Прикладная энергетика 2» [устройств](../ae2-mechanics/devices.md), полезный практически для всего, для чего используются предметы или жидкость.
Это включает в себя возврат результата создания в <ItemLink id="pattern_provider" />.

Обычно для этого есть два разных метода:

## ME Шина импорта -> ME Шина хранения

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/import_storage_pipe.snbt" />

<BoxAnnotation color="#dddddd" min="3.7 0 0" max="4 1 1">
        (1) ME Шина импорта: фильтруется.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 0 0" max="1.3 1 1">
        (2) ME Шина хранения: фильтруется. Эта ME Шина (и другие ME Шины хранения, которые вы хотите использовать в месте назначения)
        должна быть единственным хранилищем в сети.
  </BoxAnnotation>

<DiamondAnnotation pos="4.5 0.5 0.5" color="#00ff00">
        Источник
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 0.5" color="#00ff00">
        Место назначения
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

<ItemLink id="import_bus" /> (1) в исходном инвентаре импортирует предметы или жидкость и пытается сохранить их в [сетевом хранилище](../ae2-mechanics/import-export-storage.md).
Поскольку единственным хранилищем в сети является <ItemLink id="storage_bus" /> (2) (именно поэтому это подсеть, а не ваша основная сеть), предметы или жидкость
помещаются в исходный инвентарь, тем самым передавая. Энергия поступает через <ItemLink id="quartz_fiber" />.
Как ME Шина импорта, так и ME Шина хранения фильтруются, но система передаст всё, к чему сможет получить доступ, если фильтры не применены.
Эта настройка также работает с несколькими ME Шинами импорта и несколькими шинами хранения.

## ME Шина хранения -> ME Шина экспорта

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/storage_export_pipe.snbt" />

<BoxAnnotation color="#dddddd" min="3.7 0 0" max="4 1 1">
        (1) ME Шина хранения: фильтруется. Эта ME Шина (и другие ME Шины хранения, которые вы хотите использовать в месте назначения)
        должна быть единственным хранилищем в сети.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 0 0" max="1.3 1 1">
        (2) ME Шина экспорта: фильтруется.
  </BoxAnnotation>

<DiamondAnnotation pos="4.5 0.5 0.5" color="#00ff00">
        Источник
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 0.5" color="#00ff00">
        Место назначения
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

<ItemLink id="export_bus" /> в инвентаре назначения пытается извлечь предметы из своего фильтра из [сетевого хранилища](../ae2-mechanics/import-export-storage.md).
Поскольку единственным хранилищем в сети является <ItemLink id="storage_bus" /> (именно поэтому это подсеть, а не ваша основная сеть), предметы или жидкость
извлекаются из исходного инвентаря, тем самым передавая. Энергия поступает через <ItemLink id="quartz_fiber" />.
Поскольку ME Шины экспорта фильтруются для работы, эта установка работает только в том случае, если вы отфильтровали ME Шину экспорта.
Эта установка также работает с несколькими ME Шинами хранения и несколькими ME Шинами экспорта.

## Установка, которая не работает (ME Шина импорта -> ME Шина экспорта)

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/import_export_pipe.snbt" />

<BoxAnnotation color="#dd3333" min="3.7 0 0" max="4 1 1">
        ME Шина импорта: поскольку в сети нет хранилища, импортировать некуда.
  </BoxAnnotation>

<BoxAnnotation color="#dd3333" min="1 0 0" max="1.3 1 1">
        (2) ME Шина экспорта: поскольку в сети нет хранилища, экспортировать нечего.
  </BoxAnnotation>

<DiamondAnnotation pos="4.5 0.5 0.5" color="#ff0000">
        Источник
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 0.5" color="#ff0000">
        Место назначения
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Установка только с ME Шиной импорта и экспорта не будет работать. ME Шина импорта попытается извлечь из исходного инвентаря
и сохранить предметы или жидкость в сетевом хранилище. ME Шина экспорта попытается извлечь из сетевого хранилища и поместить
предметы или жидкость в целевой инвентарь. Однако, поскольку в этой сети **нет хранилища**, ME Шина импорта не может импортировать,
а ME Шина экспорта не может экспортировать, поэтому ничего не происходит.

## Ввод и вывод через 1 сторону

Допустим, у вас есть машина, которая может принимать входные ингредиенты и передавать выходные ингредиенты через одну сторону. (Например, <ItemLink id="charger" />)
Вы можете как вводить ингредиенты, так и извлекать результат, комбинируя два метода подсети каналов:

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/import_storage_export_pipe.snbt" />

<BoxAnnotation color="#dddddd" min="4 1 1" max="5 1.3 2">
        (1) ME Шина импорта: фильтруется.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2 1 1" max="3 1.3 2">
        (2) ME Шина хранения: фильтруется. Эта ME Шина (и другие ME Шины хранения, по которым вы хотите передавать и извлекать)
        должна быть единственным хранилищем в сети.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2 0 1" max="3 1 2">
        (3) То, что вы хотите поместить и извлечь: В данном случае — зарядник.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 1 1" max="1 1.3 2">
        (4) ME Шина экспорта: фильтруется по необходимости.
  </BoxAnnotation>

<DiamondAnnotation pos="4.5 0.5 1.5" color="#00ff00">
        Источник
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 1.5" color="#00ff00">
        Место назначения
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Интерфейсы

Оказывается, помимо ME Шин импорта и экспорта, существуют [устройства](../ae2-mechanics/devices.md), которые помещают предметы
в [сетевое хранилище](../ae2-mechanics/import-export-storage.md) и извлекают их из него!
Здесь уместен <ItemLink id="interface" />. Если вставлен предмет, для которого в ME Интерфейсе не задан режим хранения, ME Интерфейс
отправит его в сетевое хранилище, что мы можем использовать аналогично ME Шине импорта -> ME Шине хранения. Настройка ME Интерфейса для
хранения какого-либо предмета приведёт к его извлечению из сетевого хранилища, аналогично ME Шине хранения -> ME Шине экспорта. Интерфейсы могут быть настроены на
хранение одного и отсутствие хранения другого, что позволяет вам удалённо подключаться к ME Шинам хранения, если вы по какой-либо причине захотите это сделать.

<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/interface_pipes.snbt" />

<BoxAnnotation color="#dddddd" min="3.7 0 0" max="4 1 1">
        Интерфейс
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 0 0" max="1.3 1 1">
        ME Шина хранения
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3.7 0 2" max="4 1 3">
        ME Шина хранения
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 1 2" max="1 1.3 3">
        ME Шина хранения
  </BoxAnnotation>

<IsometricCamera yaw="195" pitch="30" />
</GameScene>

## «Один ко многим» и «многие к одному» (и «многие ко многим»)

Конечно, вам не обязательно использовать только одну <ItemLink id="import_bus" /> или <ItemLink id="export_bus" /> или <ItemLink id="storage_bus" />.

<GameScene zoom="3" background="transparent">
<ImportStructure src="../assets/assemblies/many_to_many_pipe.snbt" />

<IsometricCamera yaw="185" pitch="30" />
</GameScene>

## Поставка в несколько мест

Исходя из всего этого, мы можем разработать метод отправки ингредиентов с одной стороны <ItemLink id="pattern_provider" /> во множество различных
мест, например, в массив машин или на несколько разных сторон одной машины.

Нам не нужен канал импорта -> хранения или канал хранения -> экспорта, потому что <ItemLink id="pattern_provider" /> фактически никогда
не содержит ингредиенты. Вместо этого поставщики *перемещают* ингредиенты в соседние инвентари, поэтому нам нужен
соседний инвентарь, который также может импортировать предметы.

Похоже на... <ItemLink id="interface" />!
Убедитесь, что поставщик находится в режиме направленной или плоской подчасти и/или ME Интерфейс находится в режиме плоской подчасти, чтобы они не
образовывали сетевое соединение.

<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/provider_interface_storage.snbt" />

<BoxAnnotation color="#dddddd" min="2.7 0 1" max="3 1 2">
        Интерфейс (должен быть плоским, а не полноблочным)
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 0 0" max="1.3 1 4">
        ME Шины хранения
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 0 0" max="1 1 4">
        Места, в которых вы хотите разместить шаблон (несколько машин или несколько сторон одной машины)
  </BoxAnnotation>

<IsometricCamera yaw="185" pitch="30" />
</GameScene>