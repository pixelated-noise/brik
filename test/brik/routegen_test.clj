(ns brik.routegen-test
  (:require [brik.routegen :refer :all]
            [clojure.test :refer :all]
            [malli.core :as m]
            [malli.util :as mu]
            [clojure.walk :as w]))

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
  [[(str "/test-model")
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
               :responses {200 "OK"}}}]]])

(defn render-schemas 
  "Compiled Malli schemas are not equal to each other according to 
   Clojure's = operator.
   So we coerce all the schemas back to their vector representation
   so that the tests can correctly compare them."
  [tree]
  (w/postwalk
   #(if (m/schema? %) (m/form %) %)
   tree))

(deftest validate-api
  (is (m/validate API TestAPI))
  (is (= (render-schemas expected-api) 
         (render-schemas (generate-api TestAPI)))))
