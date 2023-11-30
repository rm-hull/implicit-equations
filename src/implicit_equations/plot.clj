(ns implicit-equations.plot
  (:import
   [java.awt Graphics2D Color]
   [java.awt.image BufferedImage])
  (:require
   [graph.heckbert :refer :all]
   [implicit-equations.image :refer :all]
   [task-scheduler.core :refer [fork join]]
   [infix.macros :refer [infix]]))

(defn fit-labels [[top right bottom left]]
  (let [min-max (juxt first last)
        [left right] (min-max (values (loose-label left right)))
        [bottom top] (min-max (values (loose-label bottom top)))]
    [top right bottom left]))

(defn expand-bounds [bounds]
  (if (number? bounds)
    (expand-bounds [bounds])
    (let [[top right bottom left] bounds]
      (case (min 4 (count bounds))
        2 [top right (- top) (- right)]
        3 [top right bottom (- right)]
        4 [top right bottom left]
        [top top (- top) (- top)]))))

(defn wrap-translation [eqn [top right bottom left] [w h]]
  (let [delta-x (double (/ (- left right) w))
        delta-y (double (/ (- bottom top) h)) ; flip horizontal axis
        start-x right
        start-y top]
    (fn [i j]
      (let [x (+ start-x (* i delta-x))
            y (+ start-y (* j delta-y))]
        (eqn x y)))))

(defn clamp [b]
  (bit-and 0xFF (int b)))

(defn render! [^BufferedImage img f [start end] {:keys [bounds line-width width height rgb step] :as opts}]
  (let [bounds (fit-labels (expand-bounds bounds))
        f (wrap-translation (fn [x y] (neg? (f x y))) bounds [width height])
        solid-color (opacity rgb 0xFF)
        half-line-width (max 1.0 (/ line-width 2.0))

        calc-wx (fn [sign x y]
                  (if (= (f x y) sign)
                    (recur sign (+ x step) y)
                    x))

        calc-wy (fn [sign x y]
                  (if (= (f x y) sign)
                    (recur sign x (+ y step))
                    y))

        draw-horiz (fn [sign x y]
                     (let [delta (clamp (* 0xFF (- (calc-wx sign x y) x)))
                           left (max 0 (- x half-line-width))
                           right (min (dec width) (+ left line-width))]
                       (set-pixel img left y (opacity rgb (- 0xFF delta)))
                       (set-pixel img right y (opacity rgb delta))
                       (loop [x (inc left)]
                         (when (<= x (dec right))
                           (set-pixel img x y solid-color)
                           (recur (inc x))))))

        draw-vert (fn [sign x y]
                    (let [delta (clamp (* 0xFF (- (calc-wy sign x y) y)))
                          top (max 0 (- y half-line-width))
                          bottom (min (dec height) (+ top line-width))]
                      (set-pixel img x top (opacity rgb (- 0xFF delta)))
                      (set-pixel img x bottom (opacity rgb delta))
                      (loop [y (inc top)]
                        (when (<= y (dec bottom))
                          (set-pixel img x y solid-color)
                          (recur (inc y))))))]

    (dotimes [y (- end start)]
      (dotimes [x width]
        (let [y (+ y start)
              sign (f x y)
              nsignx1 (f (+ x 0.5) y)
              nsignx2 (f (inc x) y)
              nsigny1 (f x (+ y 0.5))
              nsigny2 (f x (inc y))]

          (when (or (not= sign nsignx1) (not= sign nsignx2))
            (draw-horiz sign x y))

          (when (or (not= sign nsigny1) (not= sign nsigny2))
            (draw-vert sign x y)))))))

(def default-opts {:bounds 10
                   :width 600
                   :height 600
                   :rgb 0xFF0000
                   :line-width 2
                   :step 0.02
                   :gridlines false})

(defn draw-lines [lower upper size draw-fn & [divisor]]
  (let [intervals (count (loose-label lower upper))
        step (/ size (dec intervals))]
    (doall
     (for [i (range 0 size (/ step (or divisor 1)))]
       (draw-fn i)))))

(defn add-gridlines! [^Graphics2D g2d {:keys [bounds width height]}]
  (let [[top right bottom left] (expand-bounds bounds)]
    (.setColor g2d Color/LIGHT_GRAY)
    (draw-lines bottom top height #(.drawLine g2d 0 % width %) 10)
    (draw-lines left right height #(.drawLine g2d % 0 % height) 10)

    (.setColor g2d Color/GRAY)
    (draw-lines bottom top height #(.drawLine g2d 0 % width %))
    (draw-lines left right height #(.drawLine g2d % 0 % height))

    (.drawLine g2d 0 height width height)
    (.drawLine g2d width 0 width height)))

(defn draw [concurrency f name & [opts]]
  (let [opts (merge default-opts opts)
        img (create-image (inc (:width opts)) (inc (:height opts)))
        g2d (create-graphics img)
        w (/ (:height opts) concurrency)
        bands (map #(vector (* % w) (* (inc %) w)) (range concurrency))
        tasks (map #(fork (render! img f % opts)) bands)]
    (when (:gridlines opts)
      (add-gridlines! g2d opts))
    (doall (map join tasks))
    (write-png img name)
    (.dispose g2d)))

(comment

  (set! *unchecked-math* true)

  (def n-cpu (.availableProcessors (Runtime/getRuntime)))

  (use 'implicit-equations.plot)
  (use 'infix.macros)
  (use 'infix.math)

  (defn quadrifolium [x y]
    (infix (x ** 2 + y ** 2) ** 3 - x ** 2 * y ** 2))

  (defn knot-curve [x y]
    (infix (x ** 2 - 1) ** 2 - y ** 2 . (3 + 2 . y)))

  (defn biology [x y]
    (infix sin (sin x + cos y) - cos (sin (x . y) + cos x)))

  (defn chain-mesh [x y]
    (infix sin ((x ** 2) + (y ** 2)) - cos (x . y)))

  (defn checkerboard [x y]
    (infix exp (sin x + cos y) - sin (exp (x + y))))

  (defn dizzy [^double x ^double y]
    (infix abs (sin (x ** 2 - y ** 2)) - (sin (x + y) + cos (x . y))))

  (defn bands [x y]
    (infix sin (1 - x ** 2) . sin (2 - y ** 2) . x . y - cos (2 - y ** 2)))

  (defn glint [x y]
    (infix abs (sin (x ** 2 + 2 . x . y)) - sin (x - 2 . y)))

  (defn spira [x y]
    (infix sin (x ** 2 + y ** 2) - sin (x ÷ y ** 2)))

  (defn rectangle [w h]
    (fn [^double x ^double y]
      (max (- (Math/abs x) w) (- (Math/abs y) h))))

  (defn square [sz]
    (rectangle sz sz))

  (defn ellipse [a b]
    (fn [^double x ^double y]
      (infix (sqrt ((x / a) ** 2 + (y / b) ** 2)) - 1)))

  (defn circle [r]
    (ellipse 1 1))

  (defn translate [^double dx ^double dy eqn]
    (fn [x y]
      (eqn (+ x dx) (+ y dy))))

  (defn rotate [^double theta eqn]
    (let [cos-theta (Math/cos theta)
          sin-theta (Math/sin theta)]
      (fn [x y]
        (eqn (- (* x cos-theta) (* y sin-theta))
             (+ (* x sin-theta) (* y cos-theta))))))

  (defn rmin [r]
    (let [pi4 (/ Math/PI 4)
          neg-r (- r)
          r-sqrt2 (* r (Math/sqrt 2))]
      (fn [^double a ^double b]
        (if (>= (Math/abs (- a b)) r)
          (min a b)
          (+ b (* r (Math/sin (+ pi4 (Math/asin (/ (- a b) r-sqrt2))))) neg-r)))))

  (defn union
    ([r a b] (fn [x y] ((rmin r) (a x y) (b x y))))
    ([r a b c] (union r a (union r b c)))
    ([r a b c d] (union r a (union r b (union r c d)))))

  (def shape
    (rotate 0 ;(/ Math/PI 3)
            (union 0.5
                   (translate 0 -2.75 (ellipse 1.0 1.5))
                   (translate 0 0.75 (ellipse 1.0 1.5))
                   (translate -0.75 -1.0 (rectangle 1.0 1.5)))))

  (time (draw 4 shape "doc/union.png" {:bounds 5 :line-width 2 :step 0.001 :gridlines true}))

  ; 8-core   4-core   1-core
  ; ========================
  ; 184ms vs 320ms vs 917ms
  (time (draw 1 quadrifolium "doc/quadrifolium.png" {:bounds 0.5 :line-width 4}))
  (time (draw 4 quadrifolium "doc/quadrifolium.png" {:bounds 0.5 :line-width 4}))
  (time (draw 8 quadrifolium "doc/quadrifolium.png" {:bounds 0.5 :line-width 4}))

  ; 176ms vs 276ms vs 704ms
  (time (draw 1 knot-curve "doc/knot-curve.png" {:bounds 5 :line-width 2}))
  (time (draw 4 knot-curve "doc/knot-curve.png" {:bounds 5 :line-width 2}))
  (time (draw 8 knot-curve "doc/knot-curve.png" {:bounds 5 :line-width 2}))

  ; 575ms vs 963 vs 2297ms
  (time (draw 1 biology "doc/biology.png"))
  (time (draw 4 biology "doc/biology.png"))
  (time (draw 8 biology "doc/biology.png"))

  ; 1280 vs 1709ms vs 3204ms

  (time (draw 1 chain-mesh "doc/chain-mesh.png"))
  (time (draw 4 chain-mesh "doc/chain-mesh.png"))
  (time (draw 8 chain-mesh "doc/chain-mesh.png"))

  ; 534ms vs 813ms vs 1736ms
  (time (draw 1 checkerboard "doc/checkerboard.png"))
  (time (draw 4 checkerboard "doc/checkerboard.png"))
  (time (draw 8 checkerboard "doc/checkerboard.png"))

  ; 932ms vs 1344ms vs 2821ms
  (time (draw 1 dizzy "doc/dizzy.png"))
  (time (draw 4 dizzy "doc/dizzy.png"))
  (time (draw 8 dizzy "doc/dizzy.png"))

  ; 984ms vs 2303ms
  (time (draw 1 bands "doc/bands.png" {:bounds (* 2 Math/PI)}))
  (time (draw 4 bands "doc/bands.png" {:bounds (* 2 Math/PI)}))
  (time (draw 8 bands "doc/bands.png" {:bounds (* 2 Math/PI)}))

  ; 1044ms vs 1478ms vs 2804ms
  (time (draw 1 glint "doc/glint.png"))
  (time (draw 4 glint "doc/glint.png"))
  (time (draw 8 glint "doc/glint.png"))

  ; 1409ms vs 1870ms vs 3579ms
  (time (draw 1 spira "doc/spira.png"))
  (time (draw 4 spira "doc/spira.png"))
  (time (draw 8 spira "doc/spira.png")))

