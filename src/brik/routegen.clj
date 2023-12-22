(ns brik.routegen
  (:require [malli.core :as m]))

(def API (m/schema [:vector :any]))

(defn generate-api [api]
  (when (m/validate API api)
    (vec
     (for [model api]
       (let [name (:brik.api/name (m/properties model))
             index (->> model
                        m/children
                        (filter #(:brik.api/index (second %)))
                        ffirst)
             index-model (->> model
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
                     :responses {200 "OK"}}}]])))))
