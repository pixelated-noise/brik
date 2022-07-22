(ns brik.routegen
  (:require [malli.core :as m]
            [reitit.core :as r]
            [reitit.coercion.malli :as rcm]))

(def Method (m/schema [:enum :get :post :put :patch :delete]))

(def Route (m/schema
            [:map
             [:name :string]
             [:model :any]
             [:methods [:vector Method]]
             [:index :keyword]]))

(def API (m/schema [:vector Route]))

(defn generate-api [api]
  (when (m/validate API api)
    (for [{:keys [name model methods index] :as route} api]
      [(str "/" name)
       ["/" nil]
       [(str "/" index) (into {:parameters
                               {:path [index (->> model
                                                  m/entries
                                                  (into {})
                                                  index
                                                  m/children
                                                  first
                                                  m/type)]}}
                              (map (fn [m] {m nil}) methods))]])))
                         
