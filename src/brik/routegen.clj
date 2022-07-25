(ns brik.routegen
  (:require [malli.core :as m]
            [reitit.core :as r]
            [reitit.coercion.malli :as rcm]))

(def Route (m/schema
            [:map
             [:name :string]
             [:model :any]
             [:index :keyword]]))

(def API (m/schema [:vector Route]))

(defn generate-api [api]
  (when (m/validate API api)
    (for [{:keys [name model index] :as route} api]
      (let [index-model (->> model
                             m/entries
                             (into {})
                             index
                             m/children
                             first
                             m/type)]
        [(str "/" name)
         ["/" {:get {:responses {200 [model]}}
               :post {:parameters {:body model}
                      :responses {200 model}}}]
         [(str "/" index) 
          {:get {:parameters {:path [index index-model]}
                 :responses {200 model}}
           :patch {:parameters {:path [index index-model]
                                :body model}
                   :responses {200 model}}
           :delete {:parameters {:path [index index-model]}
                    :responses {200 "OK"}}}]]))))
