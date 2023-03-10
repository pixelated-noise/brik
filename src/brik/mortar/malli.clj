(ns brik.mortar.malli
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [brik.mortar :as m]
            [malli.core :as malli]
            [malli.registry :as mr]))

(defn- schema-ok? [key registry]
  (if-not (or (string? key) (and (qualified-keyword? key) ;;TODO maybe use malli/-reference? here?
                                 (-> key namespace (str/starts-with? "malli") not)))
    true
    (try
      (malli/schema [:schema key] {:registry registry})
      true
      (catch Exception e false))))

(defn registry-errors [registry]
  (let [schemas (-> registry
                    mr/registry
                    mr/schemas)]
    (->> (keys schemas)
         (map (fn [key] (when-not (schema-ok? key registry) key)))
         (remove nil?)
         (not-empty))))

;; TODO maybe we need a version of this that supports merging an arbitrary
;; number of schemas because a and b may have broken links when merged, but
;; a,b,c may be ok

(defn merge-schemas [a b]
  (when-let [conflicts (not-empty (set/intersection (keys a) (keys b)))]
    (throw (ex-info "Cannot merge malli schemas with conflicts" {:conflicts conflicts})))
  (let [merged (merge (malli/default-schemas) a b)]
    (when-let [errors (registry-errors merged)]
      (throw (ex-info "Cannot merge malli schemas, they contain unknown references" {:unknown errors})))
    merged))

(defn merge-into-module [module new-schema facet-key]
  (if-not new-schema
     module
     (update-in module [:facets facet-key :content] merge-schemas new-schema)))

(defmethod m/merge-facet* :malli/schemas [module new-facet facet-key]
  (merge-into-module module (:content new-facet) facet-key))
