# Implicit Equations

[![Build Status](https://github.com/rm-hull/implicit-equations/actions/workflows/clojure.yml/badge.svg)](https://github.com/rm-hull/implicit-equations/actions/workflows/clojure.yml)
[![Build Status](https://travis-ci.org/rm-hull/implicit-equations.svg?branch=main)](http://travis-ci.org/rm-hull/implicit-equations)
[![Coverage Status](https://coveralls.io/repos/rm-hull/implicit-equations/badge.svg?branch=main)](https://coveralls.io/r/rm-hull/implicit-equations?branch=main)
[![Dependencies Status](https://versions.deps.co/rm-hull/implicit-equations/status.svg)](https://versions.deps.co/rm-hull/implicit-equations)
[![Maintenance](https://img.shields.io/maintenance/yes/2023.svg?maxAge=2592000)]()

In mathematics, an implicit equation is a relation of the form
_R(x<sub>1</sub>,...,x<sub>n</sub>) = 0_, where _R_ is a function of several variables (often
a polynomial). For example, the implicit equation of the unit circle is
_x<sup>2</sup> + y<sup>2</sup> - 1 = 0_.

## Examples

Given an X/Y equation (using the [infix](https://github.com/rm-hull/infix) library):

```clojure
(use 'implicit-equations.plot)
(use 'infix.macros)

(defn quadrifolium [x y]
  (infix (x ** 2 + y ** 2) ** 3 - x ** 2 * y ** 2))
```

By specifying some bounds, we can now render to PNG images, with the `draw`
command - this renders onto a `BufferedImage` when the equation crosses zero:

```clojure
(draw quadrifolium "graph.png" {:bounds 1 :line-width 4})
```

to produce:

![PNG](https://rawgithub.com/rm-hull/implicit-equations/main/doc/quadrifolium.png)

Other equations produce the following plots:

### Knot Curve

![PNG](https://rawgithub.com/rm-hull/implicit-equations/main/doc/knot-curve.png)

```clojure
(defn knot-curve [x y]
  (infix (x ** 2 - 1) ** 2 - y ** 2 . (3 + 2 . y)))
```

### Biology

![PNG](https://rawgithub.com/rm-hull/implicit-equations/main/doc/biology.png)

```clojure
(defn biology [x y]
  (infix sin(sin x + cos y) - cos(sin(x . y) + cos x)))
```

### Chain-mesh

![PNG](https://rawgithub.com/rm-hull/implicit-equations/main/doc/chain-mesh.png)

```clojure
(defn chain-mesh [x y]
  (infix sin((x ** 2) + (y ** 2)) - cos(x . y)))
```

### Checkerboard

![PNG](https://rawgithub.com/rm-hull/implicit-equations/main/doc/checkerboard.png)

```clojure
(defn checkerboard [x y]
  (infix exp(sin x + cos y) - sin(exp(x + y))))
```

### Dizzy

![PNG](https://rawgithub.com/rm-hull/implicit-equations/main/doc/dizzy.png)

```clojure
(defn dizzy [x y]
  (infix abs(sin(x ** 2 - y ** 2)) - (sin(x + y) + cos(x . y))))
```

### Glint

![PNG](https://rawgithub.com/rm-hull/implicit-equations/main/doc/glint.png)

```clojure
(defn glint [x y]
  (infix abs(sin(x ** 2 + 2 . x . y)) - sin(x - 2 . y)))
```

### Spira

![PNG](https://rawgithub.com/rm-hull/implicit-equations/main/doc/spira.png)

```clojure
(defn spira [x y]
  (infix sin(x ** 2 + y ** 2) - sin(x ÷ y ** 2))
```

### Bands

![PNG](https://rawgithub.com/rm-hull/implicit-equations/main/doc/bands.png)

```clojure
(defn bands [x y]
  (infix sin(1 - x ** 2) . sin(2 - y ** 2) . x . y - cos (2 - y ** 2)))
```

Refined from: http://mathematica.stackexchange.com/questions/19590/what-is-a-good-way-to-plot-some-difficult-implicit-equations

## References

- http://www.xamuel.com/graphs-of-implicit-equations/
- http://www.peda.com/grafeq/
- http://www.padowan.dk/
- http://doc.sagemath.org/html/en/reference/plot3d/sage/plot/plot3d/implicit_plot3d.html
- http://mathematica.stackexchange.com/questions/69421/plot-of-implicit-equations
- http://wmueller.com/precalculus/newfunc/4.html
- https://en.wikipedia.org/wiki/Implicit_curve
- http://www-groups.dcs.st-and.ac.uk/~history/Curves/Curves.html
- http://www.implicitcad.org/
- https://christopherolah.wordpress.com/2011/11/06/manipulation-of-implicit-functions-with-an-eye-on-cad/
- https://en.wikipedia.org/wiki/Marching_squares

## License

### The MIT License (MIT)

Copyright (c) 2016 Richard Hull

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
