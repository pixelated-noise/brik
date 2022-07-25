(ns brik.routegen-test
  (:require [brik.routegen :refer :all]
            [clojure.test :refer :all]
            [clojure.walk :as w]
            [malli.core :as m]
            [reitit.coercion :as coercion]
            reitit.coercion.malli
            [reitit.core :as r]
            [reitit.ring :as ring]
            [reitit.ring.spec :as rrs]))

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
    {:coercion reitit.coercion.malli/coercion}
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

(def generated-api (generate-api TestAPI))
(def router (ring/router generated-api
                         {:validate rrs/validate
                          :compile coercion/compile-request-coercers}))

(defn match-by-path-and-coerce! [path]
  (when-let [match (r/match-by-path router path)]
    (assoc match :parameters (coercion/coerce! match))))

(deftest validate-api
  (is (m/validate API TestAPI))
  (is (= (render-schemas expected-api)
         (render-schemas generated-api)))
  (is (= [:coercion :get :post]
         (-> router
             (r/match-by-path "/test-model/")
             :data
             keys)))
  (is (= [:coercion :get :patch :delete]
         (-> router
             (r/match-by-path "/test-model/4")
             :data
             keys)))
  (is (nil? (r/match-by-path router "/test-model/foo")))
  (is (= 4
         (-> (match-by-path-and-coerce! "/test-model/4")
             :parameters
             :id))))
