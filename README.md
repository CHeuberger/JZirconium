# JZirconium

Java IDE for the Zirconium programming language

https://esolangs.org/wiki/Zirconium



## Changes

### v0.12

* use `Monospaced` font if `DejaVu Sans Mono` is not available - #12
* corrected count of linked stations (`K`) for bound stations - #13
* shortcut keys for compile (`F9`), run (`F11`) and step (`F5`)

#### v0.11

* minor code optimization
* index out of bound error removed
* disallow syntax characters as symbols for synthetic stations - #10
* test class

#### v0.10

* header files reading, saving, compiling and showing - #1

#### v0.09

* new zone detection (zone inference, mixed zones) - #4, #5, #6

#### v0.08

* pure stations can be placed inside exclusion zones or metropolis - #2



## Requirement/Running

* Java: version 17 ([Open JDK](http://openjdk.java.net/))
  double-click the `jzirconium.jar` file or use the command 
  `java -jar jzirconium.jar` 
* Graphviz: **optional** for graph visualization ([https://graphviz.org](https://graphviz.org/))
  The environment variable `GRAPHVIZ_HOME` should point to the **installation directory** of Graphviz.
  Alternative: the `Graph` system property can be set to point to the Graphviz `dot` **executable** (ignores `GRAPHVIZ_HOME`): 
  `java -DGraph=path-to-dot -jar jzirconium.jar`

