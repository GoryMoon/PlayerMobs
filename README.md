# PlayerMobs
Adds hostile mobs that have the skin of players

![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/GoryMoon/PlayerMobs/ci.yml)

[![CurseForge Downloads](http://cf.way2muchnoise.eu/full_430513_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/player-mobs)
[![CurseForge Versions](http://cf.way2muchnoise.eu/versions/430513_all.svg)](https://www.curseforge.com/minecraft/mc-mods/player-mobs)

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/player-mobs?color=1bd96a&label=Downlods&logo=Modrinth)](https://modrinth.com/mod/player-mobs)
[![Modrinth Versions](https://img.shields.io/modrinth/game-versions/player-mobs?color=1bd96a&label=Available%20for&logo=Modrinth)](https://modrinth.com/mod/player-mobs)

# Custom skin textures

More info about adding/overriding textures of the player mobs can be found [here](https://github.com/GoryMoon/PlayerMobs/wiki/Custom-player-mob-skins)

License
----
GNU GPL v.3

## Building

Use Java 21. The project points its plugin and dependency repositories at `https://neoforged.forgecdn.net/releases`. NeoGradle creates additional repositories internally, so pass the included init script to redirect those requests too:

```powershell
gradle -I neo-mirror.init.gradle build
```

The arm-model and EMF cape-rendering patches target NeoForge 21.1.92. The mod metadata supports NeoForge 21.1 and newer. In-game rendering still needs verification with the FA+Player resource pack and EMF enabled.
