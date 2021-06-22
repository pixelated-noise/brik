(ns brik.deps
  (:require [rewrite-clj.zip :as z]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n])
  (:refer-clojure :exclude [find]))

(defn find [zloc v]
  (z/find-value zloc z/next v))

(defn spaces [n]
  (apply str (repeat n " ")))

(defn z-column [zloc]
  (-> zloc z/node meta :col dec)) ;; columns are 1-based in meta

;; TODO use extra to include extra keys to map
;; TODO make it work with empty/non-existent aliases
(defn z-add-mvn-dep [zloc {:keys [id version alias extra] :as options}]
  (let [alias   (or alias :deps)
        map-loc (-> zloc (find alias) z/next)
        _       (when-not map-loc
                  (throw (ex-info (format "Alias %s not found" alias) options)))
        ;; don't do whitespace stuff if this the first dep
        map-loc (if (empty? (z/sexpr map-loc))
                  map-loc
                  (-> map-loc
                      (z/append-child (n/newline-node "\n"))
                      (z/append-child (-> map-loc
                                          z-column
                                          inc
                                          spaces
                                          n/whitespace-node))))]
    (-> map-loc
        (z/append-child id)
        (z/append-child {:mvn/version version}))))

(defn add-mvn-dep [{:keys [id version alias extra path] :as options}]
  (let [zloc (z/of-string (slurp (str path "/deps.edn")))]
    (spit (str path "/deps.edn") (z/root-string (z-add-mvn-dep zloc options)))))

(comment
  clojure -Sdeps '{:deps {brik/brik {:local/root "/Users/sideris/devel/brik"}}}' -X:add-mvn-dep :id foo/bar :version '"0.5.111"'

  (def zloc (z/of-string (slurp "/Users/sideris/devel/work/pix-erp/deps.edn")))

  (-> (z-add-mvn-dep zloc {:id 'foo/bar :version "0.5.222"})
      z/root-string
      println)

  (-> zloc
      (find :deps)
      z/next
      (z/append-child (n/newline-node "\n"))
      (z/append-child (n/whitespace-node "        "))
      (z/append-child 'foo/bar)
      (z/append-child {:mvn/version "0.5.66"})
      z/root-string
      println)

  (-> zloc
      (find :deps)
      z/next
      z/node
      meta))
