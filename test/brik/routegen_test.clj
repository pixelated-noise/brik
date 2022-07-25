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

(def TestAPI
  [{:name "test-model"
    :model TestModel
    :index :id}])

(def expected-api
  [(str "/test-model")
   ["/" {:get {:responses {200 [TestModel]}}
         :post {:parameters {:body TestModel}
                :responses {200 TestModel}}}]
   [(str "/:id")
    {:get {:parameters {:path [:id :int]}
           :responses {200 TestModel}}
     :patch {:parameters {:path [:id :int]
                          :body TestModel}
             :responses {200 TestModel}}
     :delete {:parameters {:path [:id :int]}
              :responses {200 "OK"}}}]])

(deftest validate-api
  (is (m/validate API TestAPI))
  (is (= expected-api (vec (generate-api TestAPI)))))
