(ns implicit-equations.plot
  (:import
    [java.awt.image BufferedImage])
  (:require
    [implicit-equations.image :refer :all]
    [implicit-equations.scheduler :refer [task join]]
    [infix.macros :refer [infix]]))

(defn expand-bounds [bounds]
  (if (number? bounds)
    (expand-bounds [bounds])
    (let [[a b c d] bounds]
    (case (min 4 (count bounds))
      2 [a  b (- a) (- b)]
      3 [a  b c (- b)]
      4 [a  b c d]
      [a  a (- a) (- a)]))))

(defn wrap-translation [eqn [top left bottom right] [w h]]
  (let [delta-x (double (/ (- left right) w))
        delta-y (double (/ (- bottom top) h)) ; flip horizontal axis
        start-x right
        start-y top]
    (fn [i j]
      (let [x (+ start-x (* i delta-x))
            y (+ start-y (* j delta-y))]
        (neg? (eqn x y))))))

(defn render! [^BufferedImage img f [start end] {:keys [bounds line-width width height rgb step] :as opts}]
  (let [bounds (expand-bounds bounds)
        f (wrap-translation f bounds [width height])
        solid-color (opacity rgb 0xFF)
        half-line-width (/ line-width 2)

        set-pixel (fn [x y rgba]
          (if (> (alpha rgba) (alpha (.getRGB img x y)))
            (.setRGB img x y rgba)))

        calc-wx (fn [sign x y]
                  (if (= (f x y) sign)
                    (recur sign (+ x step) y)
                    x))

        calc-wy (fn [sign x y]
                  (if (= (f x y) sign)
                    (recur sign x (+ y step))
                    y))

        draw-horiz (fn [sign x y]
                     (let [delta (- (calc-wx sign x y) x)
                           left (max 0 (- x half-line-width))
                           right (min (dec width) (+ left line-width))]
                       (set-pixel left y (opacity rgb (* 0x100 (- 1 delta))))
                       (set-pixel right y (opacity rgb (* 0x100 delta)))
                       (doseq [x (range (inc left) right)]
                         (set-pixel x y solid-color))))

        draw-vert (fn [sign x y]
                    (let [delta (- (calc-wy sign x y) y)
                          top (max 0 (- y half-line-width))
                          bottom (min (dec height) (+ top line-width))]
                      (set-pixel x top (opacity rgb (* 0x100 (- 1 delta))))
                      (set-pixel x bottom (opacity rgb (* 0x100 delta)))
                      (doseq [y (range (inc top) bottom)]
                        (set-pixel x y solid-color))))]

    (doseq [y (range start end)
            x (range width)
            :let [sign (f x y)
                  nsignx1 (f (+ x 0.5) y)
                  nsignx2 (f (inc x) y)
                  nsigny1 (f x (+ y 0.5))
                  nsigny2 (f x (inc y))]]

      (when (or (not= sign nsignx1) (not= sign nsignx2))
        (draw-horiz sign x y))

      (when (or (not= sign nsigny1) (not= sign nsigny2))
        (draw-vert sign x y)))))

(def default-opts {
  :bounds 10
  :width 600
  :height 600
  :rgb 0xFF0000
  :line-width 2
  :step 0.02 })

(comment
  (def n-cpu (.availableProcessors (Runtime/getRuntime)))

  (use 'implicit-equations.plot)
  (use 'infix.macros)
  (use 'infix.math)

  (defn draw [concurrency f name & [opts]]
    (let [opts (merge default-opts opts)
          img (create-image (:width opts) (:height opts))
          g2d (create-graphics img)
          w (/ (:height opts) concurrency)
          bands (map #(vector (* % w) (* (inc %) w)) (range concurrency))
          tasks (map #(task (render! img f % opts)) bands)]
      (doall (map join tasks))
      (write-png img name)
      (.dispose g2d)))

  (defn quadrifolium [x y]
    (infix (x ** 2 + y ** 2) ** 3 - x ** 2 * y ** 2))

  (defn knot-curve [x y]
    (infix (x ** 2 - 1) ** 2 - y ** 2 . (3 + 2 . y)))

  (defn biology [x y]
    (infix sin(sin x + cos y) - cos(sin(x . y) + cos x)))

  (defn chain-mesh [x y]
    (infix sin((x ** 2) + (y ** 2)) - cos(x . y)))

  (defn checkerboard [x y]
    (infix exp(sin x + cos y) - sin(exp(x + y))))

  (defn dizzy [x y]
    (infix abs(sin(x ** 2 - y ** 2)) - (sin(x + y) + cos(x . y))))

  (defn bands [x y]
    (infix sin(1 - x ** 2) . sin(2 - y ** 2) . x . y - cos (2 - y ** 2)))

  (defn glint [x y]
    (infix abs(sin(x ** 2 + 2 . x . y)) - sin(x - 2 . y)))

  (defn spira [x y]
    (infix sin(x ** 2 + y ** 2) - sin(x ÷ y ** 2)))

  ; 320ms vs 917ms
  (time (draw 1 quadrifolium "doc/quadrifolium.png" { :bounds 1 :line-width 4}))
  (time (draw 4 quadrifolium "doc/quadrifolium.png" { :bounds 1 :line-width 4}))

  ; 276ms vs 704ms
  (time (draw 1 knot-curve "doc/knot-curve.png" { :bounds 5 :line-width 2}))
  (time (draw 4 knot-curve "doc/knot-curve.png" { :bounds 5 :line-width 2}))

  ; 963 vs 2297ms
  (time (draw 1 biology "doc/biology.png"))
  (time (draw 4 biology "doc/biology.png"))

  ; 1709ms vs 3204ms
  (time (draw 1 chain-mesh "doc/chain-mesh.png"))
  (time (draw 4 chain-mesh "doc/chain-mesh.png"))

  ; 813ms vs 1736ms
  (time (draw 1 checkerboard "doc/checkerboard.png"))
  (time (draw 4 checkerboard "doc/checkerboard.png"))

  ; 1344ms vs 2821ms
  (time (draw 1 dizzy "doc/dizzy.png"))
  (time (draw 4 dizzy "doc/dizzy.png"))

  ; 984ms vs 2303ms
  (time (draw 1 bands "doc/bands.png" { :bounds (* 2 Math/PI) }))
  (time (draw 4 bands "doc/bands.png" { :bounds (* 2 Math/PI) }))

  ; 1478ms vs 2804ms
  (time (draw 1 glint "doc/glint.png"))
  (time (draw 4 glint "doc/glint.png"))

  ; 1870ms vs 3579ms
  (time (draw 1 spira "doc/spira.png"))
  (time (draw 4 spira "doc/spira.png"))
)

