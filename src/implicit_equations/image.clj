(ns implicit-equations.image
  (:import
    [java.awt Graphics2D RenderingHints]
    [java.awt.image BufferedImage]
    [javax.imageio ImageIO]))

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

(defn opacity [rgb alpha]
  (bit-or rgb (bit-shift-left (ubyte (bit-and 0xFF (int alpha))) 24)))

(defn alpha [rgba]
  (let [b (bit-shift-right rgba 24)]
    (if (neg? b) (+ b 0x100) b)))

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

