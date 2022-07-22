(ns brik.routegen
  (:require [malli.core :as m]
            [reitit.core :as r]
            [reitit.coercion.malli :as rcm]))

(def Method (m/schema [:enum :get :get* :post :put :patch :delete]))

(def Route (m/schema
            [:map
             [:name :string]
             [:model :any]
             [:methods [:vector Method]]
             [:index :keyword]]))

(def API (m/schema [:vector Route]))

(defn generate-api [api]
  [])
