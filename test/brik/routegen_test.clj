(ns brik.routegen-test
  (:require [brik.routegen :refer :all]
            [clojure.test :refer :all]
            [malli.core :as m]
            [reitit.coercion.malli :as rcm]))

(def TestModel
  (m/schema 
   [:map
    [:id :int]
    [:name :string]
    [:cat? :boolean]
    [:datetime [:or :string :int]]]))

(defn get* [_]
  {:status 200 :body []})

(defn get [_]
  {:status 200 :body {}})

(defn put [{:keys [parameters]}]
  {:status 200 :body (-> parameters :body)})

(def TestAPI
  [{:name "test-model"
    :model TestModel
    :methods [:get :get* :put]
    :index :id}])

(def expected-api
  [["/test-model/" get*]
   ["/test-model/:id" {:get get
                       :put put
                       :coercion reitit.coercion.malli/coercion
                       :parameters {:path [:id :int]}}]])

(deftest validate-api
  (is (m/validate API TestAPI))
  (is (= (generate-api TestAPI) expected-api)))
