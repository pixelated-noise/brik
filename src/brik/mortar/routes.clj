(ns brik.mortar.routes
  (:require [clojure.set :as set]
            [brik.mortar :as m]
            [reitit.core :as reitit]
            [reitit.trie :as rt]))

(defn- routes->map
  "Converts reitit routes to a map of path -> route data."
  [routes]
  (->> routes
       (reitit/router)
       (reitit/routes)
       (into {})))

(defn- map->routes
  "Unused"
  [m]
  (->> m
       (rt/insert)
       (rt/compile)
       (rt/pretty)))

(defn merge-routes
  "Merges two trees of reitit routes into one. Throws an exception if there are
  any routes that are declared in both trees."
  [a b]
  (let [m1 (routes->map a)
        m2 (routes->map b)]
    (when-let [conflicts (not-empty (set/intersection (set (keys m1)) (set (keys m2))))]
      (throw (ex-info "Cannot merge routes with conflicts" {:conflicts conflicts})))
    (->> (merge m1 m2)
         (into []))))

(defn merge-into-module
  "Merges new-routes into the :routes facet of the module. You can merge the
  routes into a custom facet by using the 3-arity version."
  ([module new-routes]
   (if-not new-routes
     module
     (merge-into-module module new-routes :routes)))
  ([module new-routes facet-key]
   (update-in module [:facets facet-key :content] merge-routes new-routes)))

(defmethod m/merge-facet* :reitit/routes [module new-facet facet-key]
  (merge-into-module module (:content new-facet) facet-key))

(comment
  (defn handler [_]
    {:status 200, :body "ok"})

  (def routes1
    [["/yo" [["/man" :man] ["/manman" :manman]]]
     ["/foo" [] [[]]
      ["/bar" {:get handler}]
      ["/baz" ["/quux" :quux] [["/qux0" :qux0] [["/qux1" :qux1]]]]
      ["/ba" ["/zz" ["/bazz" :bazz]] ["/baq" :baq]]]])

  (def routes2
    [["/yo" [["/man1" :man] ["/manman1" :manman]]]
     ["/foo" [] [[]]
      ["/bar" :bar]
      ["/baz1" ["/quux" :quux] [["/qux0" :qux0] [["/qux1" :qux1]]]]
      ["/ba1" ["/zz" ["/bazz" :bazz]] ["/baq" :baq]]]])

  (def routes3
    [["/foo"
      ["/baz"
       ["/NEW" :new]]]])

  (-> routes1 routes->map map->routes)

  (merge-routes routes1 routes2)

  (merge-routes routes1 routes3))
