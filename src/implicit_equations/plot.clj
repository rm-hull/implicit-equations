(ns implicit-equations.plot
  (:import
    [java.awt.image BufferedImage]
    [java.awt.geom AffineTransform GeneralPath Ellipse2D$Double]
    [java.awt Color Graphics2D RenderingHints BasicStroke GraphicsEnvironment]
    [javax.imageio ImageIO])
  (:require
    [infix.macros :refer [infix]]))

(defn biology [x y]
  (infix sin(sin x + cos y) - cos(sin(x . y) + cos x)))

(defn chain-mesh [x y]
  (infix sin((x ** 2) + (y ** 2)) - cos(x . y)))

(defn checkerboard [x y]
  (infix Math/exp(sin x + cos y) - sin (Math/exp(x + y))))

(defn dizzy [x y]
  (infix abs(x ** 2 - y ** 2) - (sin(x + y) + cos(x . y))))

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
        (eqn x y)))))

(def ε 0.1)
(def scale (double (/ 256 ε)))

(defn render [^BufferedImage image eqn bounds [w h]]
  (let [bounds (expand-bounds bounds)
        eqn (wrap-translation eqn bounds [w h])]
  (doseq [j (range h)
          i (range w)
          :let [v (Math/abs ^double (eqn i j))]
          :when (< v ε)]
    (.setRGB image i j (bit-or 0xFFFF (bit-shift-left (min 255 (int (* v scale))) 16))))))

(defn ^BufferedImage create-image [w h]
  (if (GraphicsEnvironment/isHeadless)
    (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)
    (.createCompatibleImage
       (.getDefaultConfiguration
         (.getDefaultScreenDevice
           (GraphicsEnvironment/getLocalGraphicsEnvironment)))
       w h)))

(defn ^Graphics2D create-graphics [^BufferedImage img]
  (let [g2d (.createGraphics img)]
    (doto g2d
      (.setRenderingHint RenderingHints/KEY_STROKE_CONTROL RenderingHints/VALUE_STROKE_NORMALIZE)
      (.setRenderingHint RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
      (.setRenderingHint RenderingHints/KEY_RENDERING RenderingHints/VALUE_RENDER_QUALITY)
      (.setBackground Color/WHITE)
      (.clearRect 0 0 1000 1000)
      )
    g2d))

(defn write-png [^BufferedImage image filename]
  (ImageIO/write image "png" (clojure.java.io/file filename)))

(def img (create-image 1000 1000))
(def g2d (create-graphics img))

(render img biology 10 [1000 1000])
(write-png img "biology.png")

(render img chain-mesh 10 [1000 1000])
(write-png img "chain-mesh.png")

(render img checkerboard 10 [1000 1000])
(write-png img "checkerboard.png")

(render img dizzy 10 [1000 1000])
(write-png img "dizzy.png")


