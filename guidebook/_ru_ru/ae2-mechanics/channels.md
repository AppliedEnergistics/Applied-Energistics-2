---
navigation:
  parent: ae2-mechanics/ae2-mechanics-index.md
  title: Каналы
  icon: controller
---

# Каналы

[ME Система](me-network-connections.md) мода «Прикладная энергетика 2» требует
каналов для поддержки [устройств](../ae2-mechanics/devices.md), использующих сетевое хранилище или другие сетевые
сервисы. Представьте себе каналы как USB-кабели, соединяющие все ваши устройства. Компьютер имеет ограниченное количество USB-портов и может поддерживать
лишь ограниченное количество подключенных к нему устройств. Большинство машин, полноблочных устройств и стандартных кабелей могут пропускать только
до 8 каналов. Полноблочные устройства и стандартные кабели можно представить как связку из 8 «канальных проводов». Однако [покрытые кабели](../items-blocks-machines/cables.md#dense-cable) могут поддерживать
до 32 каналов. Единственные другие устройства, способные передавать 32, — это <ItemLink id="me_p2p_tunnel" />
и [Квантовый сетевой мост](../items-blocks-machines/quantum_bridge.md). Каждый раз, когда устройство использует канал, представьте, что вы вытаскиваете USB-«провод» из
узла, что, очевидно, означает, что «провод» дальше по цепочке будет недоступен.

<GameScene zoom="7" interactive={true}>
  <ImportStructure src="../assets/assemblies/channel_demonstration_1.snbt" />

  <LineAnnotation color="#33ff33" from="1 .4 .7" to="2.4 .4 .7" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .6 .7" to="2.4 .6 .7" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .4 .6" to="2.6 .4 .6" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .6 .6" to="2.6 .6 .6" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .6 .6" to="2.6 .6 .6" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="2.4 .6 .7" to="2.4 .6 1.5" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="2.4 .4 .7" to="2.4 .4 1.5" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="2.6 .6 .6" to="2.6 .6 1.5" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="2.6 .4 .6" to="2.6 .4 1.5" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="2.1 .6 1.5" to="2.4 .6 1.5" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="2.6 .4 1.5" to="2.9 .4 1.5" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="2.6 .6 1.5" to="2.6 .9 1.5" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="2.4 .1 1.5" to="2.4 .4 1.5" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="1 .6 .4" to="3.5 .6 .4" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .4 .4" to="3.5 .4 .4" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="3.5 .6 .4" to="3.5 .9 .4" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="3.5 .1 .4" to="3.5 .4 .4" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="1 .6 .3" to="1.5 .6 .3" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .4 .3" to="1.5 .4 .3" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="1.5 .6 .3" to="1.5 .9 .3" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1.5 .1 .3" to="1.5 .4 .3" alwaysOnTop={true}/>

  <LineAnnotation color="#ff3333" from="3.5 .5 .5" to="5.5 .5 .5" alwaysOnTop={true}>
  All 8 channels in the cable have been used, so the Drive does not get one.  
  </LineAnnotation>

  <LineAnnotation color="#993333" from="1 .5 .5" to="1.25 .5 .5" alwaysOnTop={true}/>
  <LineAnnotation color="#993333" from="1.5 .5 .5" to="1.75 .5 .5" alwaysOnTop={true}/>
  <LineAnnotation color="#993333" from="2 .5 .5" to="2.25 .5 .5" alwaysOnTop={true}/>
  <LineAnnotation color="#993333" from="2.5 .5 .5" to="2.75 .5 .5" alwaysOnTop={true}/>
  <LineAnnotation color="#993333" from="3 .5 .5" to="3.25 .5 .5" alwaysOnTop={true}/>

  <DiamondAnnotation pos="3.6 0.5 0.5" color="#ff0000">
        Были использованы все 8 каналов в кабеле, поэтому ME Дисковод не получает ни одного из них.
    </DiamondAnnotation>

  <IsometricCamera yaw="15" pitch="30" />
</GameScene>

Простой способ увидеть, как используются и маршрутизируются каналы в вашей сети, — это использовать [умные кабели](../items-blocks-machines/cables.md), которые отобразят на них пути и использование каналов.

Каналы будут потреблять 1⁄128 AE/t на каждый проходящий через них узел, это означает, что
добавление <ItemLink id="controller" /> для
сети с 8 устройствами и более 96 узлами может фактически
уменьшить энергопотребление, поскольку это изменит способ распределения каналов.

Обратите внимание, что **КАНАЛЫ НЕ ИМЕЮТ НИКАКОГО ОТНОШЕНИЯ К ЦВЕТУ КАБЕЛЯ**, цвет кабеля лишь мешает его подключению.

## Маршрутизация каналов

При использовании <ItemLink id="controller" />,
каналы маршрутизируются в три этапа. Сначала они выбирают кратчайший путь через соседние машины к ближайшему [обычному кабелю](../items-blocks-machines/cables.md)
(стеклянному, покрытому или умному). Затем они выбирают кратчайший путь через этот обычный кабель к ближайшему [покрытому кабелю](../items-blocks-machines/cables.md)
(покрытому или умному). Затем они выбирают кратчайший путь через этот покрытый кабель к <ItemLink id="controller" />.
Если кратчайший путь уже использован, некоторые [устройства](devices.md) могут не получить необходимые каналы, используйте
цветные кабели, кабельные якоря и туннели, чтобы убедиться, что ваши каналы идут по нужному пути.

Например, в этом случае некоторые ME Дисководы не получают каналы, потому что, несмотря на достаточную емкость кабелей,
каналы пытаются выбрать кратчайший путь, перегружая одни кабели и оставляя другие пустыми.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../assets/assemblies/channel_path_length_issue.snbt" />

  <LineAnnotation color="#33ff33" from="3 .5 1.4" to="0.4 0.5 1.4" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="0.4 .5 1.4" to="0.4 0.5 3.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="0.4 0.5 3.6" to="1.4 0.5 3.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="1.4 0.5 3.6" to="1.4 0.5 5" alwaysOnTop={true} thickness="0.05"/>

  <LineAnnotation color="#33ff33" from="3 0.5 3.6" to="1.6 0.5 3.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="1.6 0.5 3.6" to="1.6 0.5 5" alwaysOnTop={true} thickness="0.05"/>

  <LineAnnotation color="#ff3333" from="3 .5 1.6" to="0.6 .5 1.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#ff3333" from="0.6 .5 1.6" to="0.6 .5 3.4" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#ff3333" from="0.6 .5 3.4" to="1.4 .5 3.4" alwaysOnTop={true} thickness="0.05"/>

  <LineAnnotation color="#ff3333" from="3 .5 3.4" to="1.6 .5 3.4" alwaysOnTop={true} thickness="0.05"/>

  <BoxAnnotation color="#dddddd" min="1.2 0.2 3.2" max="1.8 0.8 3.8" alwaysOnTop={true} thickness="0.05">
        Через это место пытаются пройти более 8 каналов, поэтому некоторые из них отключены.
  </BoxAnnotation>

  <IsometricCamera yaw="90" pitch="90" />

</GameScene>

Эту проблему можно решить, более тщательно ограничивая пути, по которым могут проходить каналы. Сети должны быть древовидными (или кустовидными).
Петли и неоднозначные пути канала должны быть сведены к минимуму.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../assets/assemblies/channel_path_length_issue_fix.snbt" />

  <LineAnnotation color="#33ff33" from="3 .5 1.4" to="0.4 0.5 1.4" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="0.4 .5 1.4" to="0.4 0.5 5.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="0.4 0.5 5.6" to="1 0.5 5.6" alwaysOnTop={true} thickness="0.05"/>

  <LineAnnotation color="#33ff33" from="3 0.5 3.6" to="1.6 0.5 3.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="1.6 0.5 3.6" to="1.6 0.5 5" alwaysOnTop={true} thickness="0.05"/>

  <IsometricCamera yaw="90" pitch="90" />

</GameScene>

## Специальные сети

Сеть без <ItemLink id="controller" />
считается специальной, и может поддерживать до 8 устройств, использующих каналы.
После того, как количество устройств превысит 8, использование каналов в сети будет прекращено,
вы можете либо удалить устройства, либо добавить <ItemLink id="controller" />.

В отличие от контролируемых сетей, [умные кабели](../items-blocks-machines/cables.md) в специальных сетях будут показывать количество
используемых каналов во всей сети, а не количество каналов, проходящих через этот конкретный кабель.

При использовании специальных сетей каждое устройство будет
использовать один канал в масштабе сети, что сильно отличается от того, как <ItemLink id="controller" /> распределяет каналы на основе
кратчайшего маршрута.

## Дизайн

Как уже упоминалось в разделе [маршрутизация каналов](channels.md#channel-routing), лучше всего проектировать сеть в виде древовидной структуры, где покрытые кабели отходят от контроллера, обычные кабели
отходят от покрытого кабеля, а [устройства](../ae2-mechanics/devices.md) располагаются в кластерах по 8 или менее устройств на обычных кабелях.

Вот пример того, чего не следует делать:

Следуя по путям каналов,

1. Сразу после выхода из контроллера справа мы оказываемся в узком месте с 8 каналами, поскольку ME Дисковод действует как обычный кабель.
Однако, поскольку мы не используем умный кабель, мы не можем видеть, сколько каналов используется. Осталось 8 каналов.
2. Дисковод занимает канал.
Осталось 7 каналов.
3. 2 канала идут к ME Терминалам.
Осталось 5 каналов.
4. Продолжая движение вправо, ME Интерфейс переключается на другой канал.
Осталось 4 канала.
5. 1 канал переходит к поставщику шаблонов.
Осталось 3 канала.
6. Продолжая движение направо, 1 канал идёт к ME Шине импорта.
Осталось 2 канала.
7. Кластер поставщиков шаблонов, питающих сборщики, получает только 2 канала, поэтому 2 поставщика не получают каналов.

В конечном итоге ошибка заключается в ограничении каналов и непродумывании того, как каналы будут распределены.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../assets/assemblies/bad_network_structure.snbt" />

<LineAnnotation color="#33ff33" from="6.5 .5 1.5" to="6 .5 1.5" alwaysOnTop={true} thickness="0.4">
  32 канала
</LineAnnotation>

<LineAnnotation color="#33ff33" from="6 .5 1.5" to="5.5 .5 1.5" alwaysOnTop={true} thickness="0.2">
  8 каналов
</LineAnnotation>

<LineAnnotation color="#33ff33" from="5.5 .5 1.5" to="5.5 1.5 1.5" alwaysOnTop={true} thickness="0.1">
  2 канала
</LineAnnotation>

<LineAnnotation color="#33ff33" from="5.5 .5 1.5" to="5.5 .3 1.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="5.5 1.5 1.5" to="5.5 2.5 1.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="5.5 2.5 1.5" to="5.5 2.5 1.1" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="5.5 .5 1.5" to="4.5 .5 1.5" alwaysOnTop={true} thickness="0.158">
  5 каналов
</LineAnnotation>

<LineAnnotation color="#33ff33" from="4.5 .5 1.5" to="4.5 .3 1.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="4.5 .5 1.5" to="4.5 1.5 1.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="4.5 .5 1.5" to="3.5 .5 1.5" alwaysOnTop={true} thickness="0.122">
  3 канала
</LineAnnotation>

<LineAnnotation color="#33ff33" from="3.5 .5 1.5" to="3.5 2.5 1.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="3.5 2.5 1.5" to="3.7 2.5 1.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="3.5 .5 1.5" to="1.5 .5 1.5" alwaysOnTop={true} thickness="0.1">
  2 канала
</LineAnnotation>

<LineAnnotation color="#33ff33" from="1.5 0.5 1.5" to="1.5 0.3 1.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="1.5 0.5 1.5" to="0.5 0.5 1.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="0.5 0.5 1.5" to="0.5 0.5 0.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#ff3333" from="0.5 1.5 1.5" to="0.5 1.3 1.5" alwaysOnTop={true} thickness="0.071">
  Нет каналов
</LineAnnotation>

<LineAnnotation color="#ff3333" from="1.5 1.5 0.5" to="1.5 1.3 0.5" alwaysOnTop={true} thickness="0.071">
  Нет каналов
</LineAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

---

Вот пример хорошей структуры:

<GameScene zoom="2.5" interactive={true}>
  <ImportStructure src="../assets/assemblies/treelike_network_structure.snbt" />

    <BoxAnnotation color="#dddddd" min="6.9 0 4.9" max="9.1 4 7.1" thickness="0.05">
        Обратите внимание, что поставщики шаблонов находятся в отдельных группах по 8.
    </BoxAnnotation>

    <BoxAnnotation color="#dddddd" min="5 4 4" max="8 5 5" thickness="0.05">
        Если объединить два обычных кабеля с каналами, вам понадобится покрытый кабель.
    </BoxAnnotation>

    <BoxAnnotation color="#dddddd" min="5 0 13" max="8 1 14" thickness="0.05">
        Для предотвращения соединения соседних кабелей используются кабели разных цветов.
    </BoxAnnotation>


  <IsometricCamera yaw="315" pitch="30" />
</GameScene>

## Режимы каналов

Мод «Прикладная энергетика 2» 10.0.0 для Minecraft 1.18 представляет новые возможности для изменения поведения каналов в вашем мире.
В разделе общей конфигурации появился новый параметр (`каналы`), который управляет этим параметром, а также новая внутриигровая
команда, позволяющая операторам изменять режим и конфигурацию непосредственно в игре. Команда « `/ae2 channelmode <mode>` »
изменяет его, а « `/ae2 channelmode` » отображает текущий режим. При изменении режима в игре все существующие сетки
перезагружаются и немедленно используют новый режим.

Это возрождает и улучшает возможность, доступную в Minecraft 1.12, и предоставляет лучшие возможности для
игроков, которые просто хотят немного более расслабленного игрового процесса, но не хотят полностью отказываться от механики.

В следующей таблице перечислены доступные режимы как в файле конфигурации, так и в команде.

| Настройки  | Описание                                                                                                                                                                                                                                                          |
| ---------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `default`  | Стандартный режим с пропускной способностью кабельных каналов и специальных сетей, как описано на этом сайте.                                                                                                                                                     |
| `x2`       | Все пропускные способности каналов удваиваются (16 на обычном кабеле, 64 на покрытом кабеле, специальные сети поддерживают 16 каналов).                                                                                                                           |
| `x3`       | Все пропускные способности каналов утраиваются (24 на обычном кабеле, 92 на покрытом кабеле, специальные сети поддерживают 24 каналов).                                                                                                                           |
| `x4`       | Все пропускные способности каналов учетверяются (32 на обычном кабеле, 128 на покрытом кабеле, специальные сети поддерживают 32 канала).                                                                                                                          |
| `infinite` | Все ограничения по каналам снимаются. Контроллеры по-прежнему *значительно* снижают энергопотребление сетей. Умные кабели будут переключаться только между режимами полного отключения (без передачи каналов) и полного включения (передача 1 или более каналов). |