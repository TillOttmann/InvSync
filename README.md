## Playerdata Syncronisation Plugin for Spigot

  This Spigot plugin provided the capability to syncronize essential playerdata between servers. It also acts as a backup, in case the world folder ever gets corrupted / deleted
  
## Features

This plugin can save (and thus syncronize) the following data about any player inside a sql database:
- Inventory
- Enderchest
- Advancements
- Experience
- Health
- Effects

## Installation

1) Download the latest release (.jar) from [releases](https://github.com/TillOttmann/InvSync/releases)
2) Put the .jar inside the plugins folder
3) Run the server once OR create the config.yml file manually (server_dir/plugins/TimeLimit/config.yml)
4) Add your database connection inside config.yml
5) IMPORTANT: If you want to have multiple syncronisations (e.g. server 1 and 2 are syncronized, and server 3 with server 4, but server
   3 and 4 should not be syncronized with 1 and 2, etc), you MUST specify a unique database name inside config.yml. Every server that
   uses the same database name automatically syncronizes

```yml
DB_Name: '[NAME]'
DB_Url: '[URL]:[PORT]'
DB_User: '[USER]'
DB_Pw: '[PASSWORD]'
```
6) You are done! Just start the server, atabases and tables are created automatically but will not overwrite existing databases / tables with the same name
    
## Usage

No command have been added yet

## Used Resources

 - [Spigot API using BuildTools](https://www.spigotmc.org/wiki/buildtools/)
