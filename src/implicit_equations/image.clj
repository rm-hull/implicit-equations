(ns implicit-equations.image
  (:import
   [java.awt Graphics2D RenderingHints]
   [java.awt.image BufferedImage]
   [javax.imageio ImageIO]))

(defn opacity [rgb alpha]
  (bit-or rgb (bit-shift-left (unchecked-byte alpha) 24)))

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

(defn set-pixel [^BufferedImage img x y rgba]
  (let [a (alpha rgba)]
    (when (or (>= a 0x80) (> a (alpha (.getRGB img x y)))))
    (.setRGB img x y rgba)))

(defn write-png [^BufferedImage image filename]
  (ImageIO/write image "png" (clojure.java.io/file filename)))

