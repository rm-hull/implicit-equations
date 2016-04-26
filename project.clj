(defproject rm-hull/implicit-equations "0.0.1"
  :description "A graphing library for implicit equations, in Clojure"
  :url "https://github.com/rm-hull/implicit-equations"
  :license {
    :name "The MIT License (MIT)"
    :url "http://opensource.org/licenses/MIT"}
  :dependencies [
    [org.clojure/clojure "1.8.0"]
    [rm-hull/infix "0.2.6"]]
  :scm {:url "git@github.com:rm-hull/implicit-equations.git"}
  :plugins [
    [codox "0.9.1"] ]
  :source-paths ["src"]
  :jar-exclusions [#"(?:^|/).git"]
  :codox {
    :sources ["src"]
    :output-dir "doc/api"
    :src-dir-uri "http://github.com/rm-hull/implicit-equations/blob/master/"
    :src-linenum-anchor-prefix "L" }
  :min-lein-version "2.5.3"
  :profiles {
    :dev {
      :global-vars {*warn-on-reflection* true}
      :plugins [
        [lein-cloverage "1.0.6"]]}})
