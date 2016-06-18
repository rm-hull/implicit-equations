(defproject rm-hull/implicit-equations "0.0.1"
  :description "A graphing library for implicit equations, in Clojure"
  :url "https://github.com/rm-hull/implicit-equations"
  :license {
    :name "The MIT License (MIT)"
    :url "http://opensource.org/licenses/MIT"}
  :dependencies [
    [org.clojure/clojure "1.8.0"]
    [rm-hull/loose-labels "0.1.0"]
    [rm-hull/infix "0.2.9"]]
  :scm {:url "git@github.com:rm-hull/implicit-equations.git"}
  :source-paths ["src"]
  :jar-exclusions [#"(?:^|/).git"]
  :codox {
    :source-paths ["src"]
    :output-path "doc/api"
    :source-uri "http://github.com/rm-hull/implicit-equations/blob/master/{filepath}#L{line}" }
  :min-lein-version "2.6.1"
  :profiles {
    :dev {
      :global-vars {*warn-on-reflection* true}
      :plugins [
        [lein-codox "0.9.5"]
        [lein-cloverage "1.0.6"]]}})
