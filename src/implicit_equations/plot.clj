(ns implicit-equations.plot
  (:import
    [java.awt.image BufferedImage]
    [java.awt.geom AffineTransform GeneralPath Ellipse2D$Double]
    [java.awt Color Graphics2D RenderingHints BasicStroke GraphicsEnvironment]
    [javax.imageio ImageIO])
  (:require
    [infix.macros :refer [infix]]))

(defn- unsigned [f bits]
  (let [max-val (bit-shift-left 1 (dec bits))
        neg (bit-shift-left 1 bits)]
    (fn [n]
      (f
        (if (>= n max-val)
          (- n neg)
          n)))))

(def ubyte (unsigned byte 8))
(def ushort (unsigned short 16))
(def uint (unsigned int 32))

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

(defn opacity [rgb alpha]
  (bit-or rgb (bit-shift-left (ubyte (bit-and 0xFF (int alpha))) 24)))

(defn alpha [rgba]
  (let [b (bit-shift-right rgba 24)]
   (if (neg? b) (+ b 0x100) b)))

(defn render! [^BufferedImage img f {:keys [bounds line-width width height rgb step] :as opts}]
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

    (doseq [y (range height)
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

(defn ^BufferedImage create-image [w h]
  (BufferedImage. w h BufferedImage/TYPE_INT_ARGB))

(defn ^Graphics2D create-graphics [^BufferedImage img]
  (let [g2d (.createGraphics img)]
    (doto g2d
      (.setRenderingHint RenderingHints/KEY_STROKE_CONTROL RenderingHints/VALUE_STROKE_NORMALIZE)
      (.setRenderingHint RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
      (.setRenderingHint RenderingHints/KEY_RENDERING RenderingHints/VALUE_RENDER_QUALITY))
    g2d))

(defn write-png [^BufferedImage image filename]
  (ImageIO/write image "png" (clojure.java.io/file filename)))

(def default-opts {
  :bounds 10
  :width 600
  :height 600
  :rgb 0xFF0000
  :line-width 2
  :step 0.02 })

(defn draw [f name & [opts]]
  (let [opts (merge default-opts opts)
        img (create-image (:width opts) (:height opts))
        g2d (create-graphics img)]
    (render! img f opts)
    (write-png img name)
    (.dispose g2d)))

(comment

  (use 'implicit-equations.plot)
  (use 'infix.macros)

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

  (time (draw quadrifolium "doc/quadrifolium.png" { :bounds 1 :line-width 4}))
  (time (draw knot-curve "doc/knot-curve.png" { :bounds 5 :line-width 2}))
  (time (draw biology "doc/biology.png"))
  (time (draw chain-mesh "doc/chain-mesh.png"))
  (time (draw checkerboard "doc/checkerboard.png"))
  (time (draw dizzy "doc/dizzy.png"))

)

