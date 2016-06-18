(ns implicit-equations.scheduler
  (:import
    [java.util.concurrent ForkJoinPool ForkJoinWorkerThread ForkJoinTask RecursiveTask]))

(def ^:private ^ForkJoinPool pool (ForkJoinPool.))

(defn submit [^ForkJoinTask fjtask]
  (if (instance? ForkJoinWorkerThread (Thread/currentThread))
    (.fork fjtask)
    (.execute pool fjtask))
  fjtask)

(defmacro task [& body]
  `(submit
    (proxy [RecursiveTask] []
      (compute ([] (do ~@body))))))

(defn join [^ForkJoinTask task]
  (.join task))
