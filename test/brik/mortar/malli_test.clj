(ns brik.mortar.malli-test
  (:require [clojure.test :refer [deftest is testing]]
            [brik.mortar.malli :as sut]
            [malli.core :as malli]))


(def schema1
    {::id      int?
     ::country string?})

(def schema2
  {::secondary-id ::id
   ::age          int?})

(def schema3
  {::secondary-id ::DOES-NOT-EXIST
   ::age          int?})


(deftest merge-schemas-test
  (testing ""
    (is (nil?
         (sut/check-registry
          (merge
           (malli/default-schemas)
           schema1
           schema2)))))
  (testing ""
    (is (= [::secondary-id]
           (sut/check-registry
            (merge
             (malli/default-schemas)
             schema1
             schema3))))))
