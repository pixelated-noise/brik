(ns brik.mortar
  (:require [clojure.set :as set]))

(defmulti merge-facet* (fn [module new-facet facet-key] (:type new-facet)))

(defn merge-facet [module new-facet facet-key]
  (let [orig-facet (get-in module [:facets facet-key])]
    (cond (and orig-facet
               (not= (:type orig-facet) (:type new-facet)))
          (throw (ex-info "Facets don't have compatible types" {:original-type (:type orig-facet)
                                                                :novelty-type  (:type new-facet)}))

          (not orig-facet)
          (assoc-in module [:facets facet-key] new-facet)

          :else
          (merge-facet* module new-facet facet-key))))

(defn merge-module
  "Merges module m2 into module m1 by merging all their facets."
  [m1 m2]
  (reduce-kv (fn [m1 facet-key new-facet]
               (merge-facet m1 new-facet facet-key))
             m1 (:facets m2)))

(defn rename-facets [module kmap]
  (update module :facets #(set/rename-keys % kmap)))


(comment
  (def user-management
    {:name   :brik/user-management
     :facets {:routes  {:type    :reitit/routes
                        :content ["/api/user"
                                  ["/create" {:post (fn [])}]
                                  ["/update" {:post (fn [])}]
                                  ["/delete" {:post (fn [])}]]}}})

  (def recipe-management
    {:name   :brik/recipe-management
     :facets {:routes  {:type    :reitit/routes
                        :content ["/api/recipe"
                                  ["/create" {:post (fn [])}]
                                  ["/update" {:post (fn [])}]
                                  ["/delete" {:post (fn [])}]]}}})

  (def app
    {:name :my-app})

  (merge-module app user-management))
