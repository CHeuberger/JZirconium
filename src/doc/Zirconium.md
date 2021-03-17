# Zirconium



## About

A Zirconium program (or a *map*) is a network of *stations* in two-dimensional space, linked together with *tunnels*, akin to a graph with nodes. At each tick of time, stations will dispatch a number of *drones* to occupy linked stations.

```
   0
.--+--O
 \ | /
  \|/
   o----@
```

## Stations

At the beginning of the tick, all stations count the number of drones *occupying* them, and then consume all those drones. Afterwards, the  stations execute the following:

|      |                                                              |
| ---- | ------------------------------------------------------------ |
| `.`  | If this is occupied by any amount of drones, dispatch one drone to each linked station. |
| `o`  | Dispatch the number of drones occupying this station to each linked station. |
| `0`  | Do not dispatch any drones.                                  |
| `O`  | Dispatch `N // K` drones to each linked station, where `N` is the number of drones occupying this station, `K` is the number of linked stations, and `//` is the floor division operation.[^1] |
| `Q`  | If this station is occupied by `N` drones, dispatch `N - 1` drones to linked stations. |
| `@`  | If this station is not occupied, dispatch one drone to each linked station. |

## Tunnel semantics

A tunnel may be horizontal, vertical or diagonal, using `-`, `|`, `/`, or `\` symbols appropriately. A tunnel can only travel in one direction, and must have at least one segment. A `+` behaves as both a horizontal and a vertical tunnel, a `X` behaves as both diagonal tunnels, and `*` behaves as any tunnel. These may be used to place overlapping tunnels on the map.

```
    .
    |
.---+---@
    |
    @
```

## Apertures

The endpoint of a tunnel may be replaced with an *aperture* to enforce a one-way link between stations. [^2] Horizontal and vertical apertures are marked with `>`, `^`, `<`, or `v`, to point east, north, west, or south. Diagonal apertures are all marked with `#`.

```
@-->.
^  /|
| / |
|#  v
.<--.
```

## Bubbles and lenses

Anything contained between inside parentheses `()` is a bubble and is ignored. Consider it a comment.

Anything contained inside double parentheses `(())` is a lens, used for synthetic station definitions. Consider it a documentation string.

## Exclusion zones

An area of the program may be enclosed with *fences* to make it an *exclusion zone*:[^3]

```
 {~~}
{    }
 {    ~~~~~~}
{            }
{     ~~     }
 {~~~}  {~~~}
```

The fences of an exclusion zone will behave as `*` tunnels.

An exclusion zone may contain special *defect stations*, which perform impure computation:[^4]

|      |                                                              |
| ---- | ------------------------------------------------------------ |
| `?`  | If any drones occupy this, read one byte from STDIN and dispatch that many drones to linked stations. |
| `!`  | If any drones occupy this, halt the program.                 |
| `%`  | If any drones occupy this, print the number of drones occupying this station as a byte modulo 256 to STDOUT. |
| `&`  | If any drones occupy this, write the number of drones occupying this as a byte modulo 256 to STDERR. |
| ` ` `  | If any drones occupy this, write the number of drones occupying this station in numeric form to STDOUT. |
| `_`  | If any drones occupy this, read a numeric value from STDIN and dispatch that many drones to linked stations. |
| `;`  | Pause execution for a duration equal to the number of drones occupying this station in milliseconds. |

## Bound stations

Stations that are adjacent are considered a single *bound station*. A bound station executes the behavior of all its child stations each  tick, and is linked to each station its child stations are linked to. [^5]

```
      .
     /
@->00
 #   0--.
  \ 0
   o
```

## Metropolis

An area of the program may be enclosed with *forts* to make it a *metropolis*:[^6]

```
 [==]
[    ]
 [    ======]
[            ]
[     ==     ]
 [===]  [===]
```

Like the fences of an exclusion zone, the forts of a metropolis behave as `*` tunnels.

A metropolis may contain special *synthetic stations*.

## Synthetic stations

A synthetic station is a station whose behavior is defined by the  user. Each tick, a synthetic station dispatches some number of drones  based on the number of occupying drones and the number of linked  stations using some arithmetic expression.

A synthetic station must be defined using a specific grammar. The definition includes a target symbol, which is the synthetic station to  be defined, and an arithmetic expression in postfix notation. The  expression is evaluated for the station on each tick, and represents the number of drones dispatched[^7]. The expression can be in terms of integer literals, as well as special variables `N` and `K`, which represent the number of drones currently occupying the station and the number of linked station.

For instance, 

```
Z = N 1 +
```

The `Z` station here is defined to dispatch `N + 1` drones to each linked station.

Expressions may contain the following operators: `+`, `-`, `*`, `/`, `=`, corresponding to addition, subtraction, multiplication, floor division[^8] and equality[^9].

The following is a complete grammar for synthetic station definitions.[^10][^11]

```
definition := symbol sp* "=" sp* expr
symbol := [^\s]
expr := value | expr sp* expr sp* operator
value := "N" | "K" | integer
integer := ["0"-"9"]+
operator := "+" | "-" | "*" | "/" | "=" 
sp := " " | "\t"
```

A synthetic station may be defined inside a lens. A lens is parsed at compile time:

```
((r = N K / 1 +))
```

A synthetic station may also be defined in a special header file.[^12] A header file is a file provided to the program, containing  newline-delimited synthetic station definitions. These are also parsed  at compile time.

## Computational class

Zirconium without extensions is [Turing complete](https://esolangs.org/wiki/Turing_complete) from reduction to a [register machine](https://esolangs.org/wiki/Minsky_machine) with any amount of registers and the instructions `INC`, `DEC`, and `JZ`.

The registers are "modules", each laid out like so:

```
 0
Q>0<@
v ^ ^
@ | |
| | |
```
The southwest tunnel is an output which dispatches 1 drone whenever the register is zero. The southern tunnel is an input which increments the register. The southeast tunnel is an input which decrements the register.

Instructions are also modules laid out like so:

##### `INC`

```
   |
   v        
 @>O        
  /v        
 0 .---
   | 
```

The northern tunnel is an input which tells this module to activate.
The eastern tunnel is an output which connects to one of the registers' "increment" inputs.
The southern tunnel is an output which leads to the next module to activate.

##### `DEC`

```
   |
   v
 @>O
  /v
 0 .---
   | 
```
The northern tunnel is an input which tells this module to activate.
The eastern tunnel is an output which connects to one of the registers' "decrement" inputs.
The southern tunnel is an output which leads to the next module to activate.

##### `JZ`

```
   |
   v
 @>O   .<---
  /|\ /v
 0 | X @
   v# #v
 0<O   O>0
   |   | 
```
The northern tunnel is an input which tells this module to activate.
The northeast tunnel is an input from one of the registers' "zero" outputs.
The southwest tunnel is an output leading to the next module to activate if the register is zero.
The southeast tunnel is an output leading to the next module to activate if the register is nonzero.



The module to be activated at the start of the program is triggered with the following setup:

```
 0
@>0
|
|
```
By piecing together these modules, one can construct a register machine capable of universal computation.

## Example programs

##### Print Fibonacci numbers
```
o~  o   @
{`}0>0<0>0
 ~
```
The rightmost bound station (containing `@`, `0`, and `0`) dispatches one drone to the middle station at the start of the program, and does nothing afterwards.
The middle bound station (containing `o`, `0`, and `0`) dispatches its drones to the leftmost bound station (containing `o` and `` `), as well as itself, each tick.
The leftmost bound station dispatches its drones to the middle  station, as well as printing the number of stations occupying it, each  tick. 

##### Echo input
```
 ~~~
{&<?}@
 ~~~
```
The `@` station triggers the `?` station, reading one byte of input.
The `&` station then prints that byte. One tick later, the `@` station triggers again. |

## Footnotes

[^1]: Division by zero returns 0.

[^2]: Tunnels with apertures pointing towards a station are not considered linked  from that station’s point of view. This applies to the `O` station, as well as synthetic stations.

[^3]: Exclusion zones may not be nested, or contain metropolises.

[^4]: All race conditions arising from the use of defect stations are undefined.

[^5]: A bound station may be linked to itself in certain configurations.

[^6]: Metropolises may not be nested, or contain exclusion zones.

[^7]: Negative drones are clamped to 0.

[^8]: Division by 0 returns 0.

[^9]: `=` returns 1 if its two arguments are equal, and 0 otherwise.

[^10]: Note that while `symbol` may be any non-whitespace character, it must not conflict with a character already defined in Zirconium grammar, such as `-` or `|`.

[^11]: The only exception to this rule are station symbols. These may be overridden, but the new definition will *only* apply to synthetic stations inside metropolises.

[^12]: By convention, these should be named `file.zch`, where `file` is the name of the original file.
