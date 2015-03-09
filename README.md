# Dynmap-Structures [![Build Status](https://travis-ci.org/Codeski/dynmap-structures.svg?branch=master)](https://travis-ci.org/Codeski/dynmap-structures)

A Bukkit plugin that shows your world's structures (such as Villages, Strongholds, and Temples) on Dynmap.

## Configuration

The **structures** node supports boolean values for the following keys:

**fortress**

    If true, displays Nether Fortresses on your map.

**mineshaft**

    If true, displays Abandoned Mineshafts on your map. Default value is false.

**monument**

    If true, displays Ocean Monuments on your map.

**stronghold**

    If true, displays Strongholds on your map.

**temple**

    If true, displays Desert Temples and Jungle Temples on your map.

**witch**

    If true, displays Witch Huts on your map.

**village**

    If true, displays Villages on your map.

The **layer** node supports the following key-value pairs:

**name**

    A string that is used for the name of the layer. It is shown in the layer control UI element.

**hidebydefault**

    If true, the structures layer will be hidden by default.

**layerprio**

    An integer representing the layer priority in Dynmap.

**nolabels**

    If true, no labels will be shown for structures on the map.

**minzoom**

    The minimum zoom level where structures will be shown on the map.

**inc-coord**

    If true, coordinates will be included in the labels for structures.

You can also place a hash in front of any of the nodes to comment it out and disable it.

## Links

* Website: <http://codeski.com/#dynmapstructures>
* Issues: <https://github.com/Codeski/dynmap-structures/issues>
* Source: <https://github.com/Codeski/dynmap-structures>
* Builds: <https://travis-ci.org/Codeski/dynmap-structures>
* Bukkit: <http://dev.bukkit.org/bukkit-plugins/dynmap-structures>

## License

Copyright © 2013 Kevin Breslin

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
