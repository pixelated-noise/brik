(ns brik.routegen-test
  (:require [brik.routegen :refer :all]
            [clojure.test :refer :all]
            [malli.core :as m]))

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
    :methods [:get :put]
    :index :id}])

(def expected-api
  [["/test-model"
    ["/" nil]
    ["/:id" {:parameters {:path [:id :int]}
             :get nil
             :put nil}]]])

(deftest validate-api
  (is (m/validate API TestAPI))
  (is (= expected-api (vec (generate-api TestAPI)))))
