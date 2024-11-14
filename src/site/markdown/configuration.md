---
title: XMvn configuration
author:
  - Mikolaj Izdebski
date: 2013-03-05
---


Configuring XMvn
================


Basics
------

XMvn is driven by configuration.  It loads configuration in XML
format, which can be used to control many aspects of XMvn behaviour.

XMvn loads configuration from various sources.  Each source uses
exactly the same configuration format.  Configuration particles from
all the sources are merged together into a single master XMvn
configuration.


Compatibility levels
--------------------

XMvn has support for different compatibility levels.  Compatibility
level decies which set of configuration files is loaded.  Each of the
levels has different set of configuration files.  Unless otherwise
indicated, all XMvn documentation assumes that default compatibility
level is used.


Sources of configuration
------------------------

XMvn reads its configuration from the following sources:

1. Reactor configuration directory - specified in configuration files
in `$PWD/.xmvn/config.d/` directory,
1. Reactor configuration file - specified in configuration file
`$PWD/.xmvn/configuration.xml`,
1. User configuration directory - specified in configuration files in
`$XDG_CONFIG_HOME/xmvn/config.d/` directory,
1. User configuration file - specified in configuration file
`$XDG_CONFIG_HOME/xmvn/configuration.xml`,
1. User data directory - specified in configuration files in
`$XDG_DATA_HOME/xmvn/config.d/` directory,
1. User data file - specified in configuration file
`$XDG_DATA_HOME/xmvn/configuration.xml`,
1. System configuration directories - specified in configuration files
in `$XDG_CONFIG_DIRS/xmvn/config.d/` directory,
1. System configuration files - specified in configuration file
`$XDG_CONFIG_DIRS/xmvn/configuration.xml`,
1. System data directories - specified in configuration files in
`$XDG_DATA_DIRS/xmvn/config.d/` directory,
1. System data files - specified in configuration file
`$XDG_DATA_DIRS/xmvn/configuration.xml`,
1. Builtin configuration - embedded directly in `xmvn-core.jar` as a
resource.

In the above list `$PWD` means current working directory.  For the
meaning of `$XDG_CONFIG_HOME`, `$XDG_DATA_HOME`, `$XDG_CONFIG_DIRS`
and `$XDG_DATA_DIRS` see the [XDG Base Directory
Specification](http://standards.freedesktop.org/basedir-spec/basedir-spec-0.8.html).

All these files are in the same format, which is fully documented in
configuration reference.  Configuration is inherited from files of
lower precedence.  For example if some setting is not found in reactor
configuration then it will be inherited from user configuration, but
if this setting is explicitly set in reactor configuration then it
will take precedence over user configuration.


Builtin configuration
---------------------

As builtin configuration is embedded into XMvn it is assumed to always
be present and correct.  Builtin configuration is minimal and it is
not really usable, but it is used only if no other configuration files
are provided or available, for example when unsupported compatbility
level is specified.

Configuration merging
---------------------

Each configuration source has defined its precedence, which affects
the way how configuration particles are merged together to give the
single master configuration.
