(ns brik.rewrite
  (:require [rewrite-clj.zip :as z]))

(defn z-find [zloc v]
  (z/find-value zloc z/next v))

(defn z-rewind
  "Rewind the zipper to the beginning so it can be reused."
  [zloc]
  (if (:end? zloc)
    zloc
    (let [p (z/up zloc)]
      (if p
        (recur p)
        zloc))))

(defn z-child-locs [zloc]
  (loop [zloc (z/down zloc)
         c    []]
    (if zloc
      (recur (z/right zloc) (conj c zloc))
      c)))

(defn z-column [zloc]
  (-> zloc z/node meta :col dec)) ;; columns are 1-based in meta

(defn z-assoc [zloc k v]
  (-> zloc
      (z/append-child k)
      (z/append-child v)))

(defn unterleave
  "Opposite of interleave (assumming 2 colls)"
  [coll]
  [(take-nth 2 coll)
   (take-nth 2 (rest coll))])

(defn z-aligned-map?
  "Whether the keys and values are each aligned vertically with whitespace"
  [zloc]
  (when-not (map? (z/sexpr zloc))
    (throw (ex-info "zloc is not pointing to a map" {:zloc zloc :sexpr (z/sexpr zloc)})))
  (every? (partial apply =) (unterleave (map z-column (z-child-locs zloc)))))

(defn align-map [zloc]
  )

(comment
  (-> (z/of-string (slurp "/Users/sideris/devel/dummy/deps.edn"))
      (z-find :deps)
      (z/next)
      (z-aligned-map?)))
