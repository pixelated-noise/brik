(ns brik.mortar
  (:require [malli.core :as m]
            [malli.registry :as mr]
            [malli.dev.pretty :as pretty]
            [malli.util :as mu]))

;; (->> (mr/schemas m/default-registry) keys (map class) distinct)
;; symbols, keywords, classes, functions

(mr/set-default-registry!
 (mr/registry
  (merge
   (m/default-schemas)
   {:Country   [:map
                [:name [:enum :FI :PO]]]
    :Burger    [:map
                [:name string?]
                [:description {:optional true} string?]
                [:origin [:map
                          [:bar [:map
                                 [:foo :Country2]]]]]
                [:price pos-int?]]
    :OrderLine [:map
                [:burger :Burger]
                [:amount int?]]
    :Order     [:map
                [:lines [:vector :OrderLine]]
                [:delivery [:map
                            [:delivered boolean?]
                            [:address [:map
                                       [:street string?]
                                       [:zip int?]
                                       [:country :Country]]]]]]})))


(comment
  (m/validate :Country {})
  (pretty/explain :Country {:name :vv})
  (m/schema :Burger)
  (m/schema :Country)
  )

(def Address2
  [:schema
   {:registry {:Country   [:map
                           [:name [:enum :FI :PO]]
                           [:neighbors [:vector [:ref :Country]]]]
               :Burger    [:map
                           [:name string?]
                           [:description {:optional true} string?]
                           [:origin [:maybe :Country]]
                           [:price pos-int?]]
               :OrderLine [:map
                           [:burger :Burger]
                           [:amount int?]]
               :Order     [:map
                           [:lines [:vector :OrderLine]]
                           [:delivery [:map
                                       [:delivered boolean?]
                                       [:address [:map
                                                  [:street string?]
                                                  [:zip int?]
                                                  [:country :Country]]]]]]}}
   "Order"])

(defn validate-malli [registries]
  ;;;
  )
