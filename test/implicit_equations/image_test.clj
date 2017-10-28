(ns implicit-equations.image-test
  (:import
   [java.awt Graphics2D RenderingHints]
   [java.awt.image BufferedImage])
  (:require
   [clojure.test :refer :all]
   [implicit-equations.image :refer :all]))

(deftest check-opacity
  (is (=  0x0EADBABE (opacity 0xADBABE 0x0E)))
  (is (= -0x00000001 (opacity 0xFFFFFF 0xFF)))
  (is (= -0x01000000 (opacity 0x000000 0xFF)))
  (is (= -0x00010000 (opacity 0xFF0000 0xFF))))

(deftest check-alpha
  (is (= 0x0E (alpha  0x0EADBABE)))
  (is (= 0xFF (alpha -0x00000001)))
  (is (= 0xFF (alpha -0x01000000)))
  (is (= 0xFF (alpha -0x00010000))))

(deftest check-create-image
  (let [img (create-image 34 97)]
    (is (= 34 (.getWidth img)))
    (is (= 97 (.getHeight img)))
    (is (= BufferedImage/TYPE_INT_ARGB (.getType img)))))

(deftest check-create-graphics
  (let [img (create-image 91 388)
        g2d (create-graphics img)]
    (is (= RenderingHints/VALUE_STROKE_NORMALIZE (.getRenderingHint g2d RenderingHints/KEY_STROKE_CONTROL)))
    (is (= RenderingHints/VALUE_ANTIALIAS_ON (.getRenderingHint g2d RenderingHints/KEY_ANTIALIASING)))
    (is (= RenderingHints/VALUE_RENDER_QUALITY (.getRenderingHint g2d RenderingHints/KEY_RENDERING)))))

