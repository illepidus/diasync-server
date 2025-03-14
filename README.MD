# Diasync server

Серверная часть системы мониторинга глюкозы [Diasync](https://github.com/illepidus/diasync).

## Пример схемы

Очевидно, схема неполная, на ней не хватает некоторых связей.
Например, XDrip и ребенка и родителя может писать в Nightscout. Но это сильно усложняет восприятие основных потоков
данных,
поэтому я оставил их за скобками.

```mermaid
flowchart LR
    pump([Инсулиновая помпа])
    sensor([Датчик сахара])

    subgraph g_cloud[Google cloud]
        firebase[Firebase]
    end

    subgraph phone_master[Телефон ребёнка]
        juglucco[Juglucco]
        xdrip_master[XDrip]
        aaps_master[AAPS]
        ds_master[Diasync]
    end
    subgraph server[Сервер]
        ds_srv[Diasync Server]
        nightscout[NightScout]
    end
    subgraph phone_slave[Телефон родителя]
        ds_slave[Diasync]
        aaps_slave[AAPS]
        xdrip_slave[XDrip]
    end

    sensor --> juglucco --> xdrip_master
    xdrip_master --> ds_master --> ds_srv --> ds_slave
    xdrip_master -.-> aaps_master <-.-> nightscout <-.-> aaps_slave
    xdrip_master <-.-> firebase <-.-> xdrip_slave
    aaps_master -.-> pump
    style ds_srv color: #FF8800
    click juglucco href "https://www.juggluco.nl/"
    click firebase href "https://firebase.google.com/"
    click xdrip_master,xdrip_slave href "https://github.com/NightscoutFoundation/xDrip"
    click aaps_master,aaps_slave href "https://github.com/nightscout/AndroidAPS"
    click ds_master,ds_slave href "https://github.com/illepidus/diasync"
    click nightscout href "https://github.com/nightscout/cgm-remote-monitor"
```
