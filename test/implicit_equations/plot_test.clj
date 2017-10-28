(ns implicit-equations.plot-test
  (:require
   [clojure.test :refer :all]
   [implicit-equations.plot :refer :all]))

(deftest check-expand-bounds
  (is (= [10 10 -10 -10] (expand-bounds 10)))
  (is (= [10 7 -10 -7] (expand-bounds [10 7])))
  (is (= [10 7 5 -7] (expand-bounds [10 7 5])))
  (is (= [10 7 2 6] (expand-bounds [10 7 2 6])))
  (is (= [0 1 2 3] (expand-bounds (range 100)))))

