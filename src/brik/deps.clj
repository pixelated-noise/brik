(ns brik.deps
  (:require [rewrite-clj.zip :as z]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]
            [ancient-clj.core :as ancient]
            [taoensso.timbre :as log])
  (:refer-clojure :exclude [find]))

(def nicknames
  {'clojure    'org.clojure/clojure
   'core.async 'org.clojure/core.async
   'reitit     'metosin/reitit
   'timbre     'com.taoensso/timbre})

(defn find [zloc v]
  (z/find-value zloc z/next v))

(defn z-rewind [zloc]
  (if (:end? zloc)
    zloc
    (let [p (z/up zloc)]
      (if p
        (recur p)
        zloc))))

(defn spaces [n]
  (apply str (repeat n " ")))

(defn z-column [zloc]
  (-> zloc z/node meta :col dec)) ;; columns are 1-based in meta

(defn resolve-nickname [id]
  (if-let [nick (get nicknames id)]
    (do
      (log/infof "Resolved dep nickname '%s' to '%s'" id nick)
      nick)
    id))

(defn resolve-version [id version]
  (log/info "No version provided, finding latest...")
  (if-not version
    (let [ver (ancient/latest-version-string! (str id))]
      (log/infof "Latest %s version: %s" id ver)
      ver)
    version))

;; TODO use extra to include extra keys to map
;; TODO make it work with empty/non-existent aliases
;; TODO add warning for existing deps (but replace them)
(defn z-add-mvn-dep [zloc {:keys [id version alias extra] :as options}]
  (let [id      (resolve-nickname id)
        alias   (or alias :deps)
        version (resolve-version id version)
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
    (log/infof "Adding '%s' version %s to %s" id version alias)
    (log/info "")
    (-> map-loc
        (z/append-child id)
        (z/append-child {:mvn/version version})
        (z-rewind))))

(defn add-mvn-dep* [{:keys [id version alias extra] :as options}]
  (let [id      (if-not (sequential? id) [id] id)
        zloc    (z/of-string (slurp "deps.edn"))
        new-loc (reduce (fn [loc id] (z-add-mvn-dep loc (assoc options :id id))) zloc id)]
    (spit "deps.edn" (z/root-string new-loc))))

(defn add-mvn-dep [options]
  (add-mvn-dep* options)
  (shutdown-agents)) ;; this is necessary because of ancient - see https://clojuredocs.org/clojure.core/future

(comment
  clojure -Sdeps '{:deps {brik/brik {:local/root "/Users/sideris/devel/brik"}}}' -X brik.deps/add-mvn-dep :id foo/bar :version '"0.5.111"' :path '"pix-erp"'

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
