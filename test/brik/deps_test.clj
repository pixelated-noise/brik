(ns brik.deps-test
  (:require [brik.deps :as sut]
            [rewrite-clj.zip :as z]
            [clojure.test :refer :all]))

(deftest add-mvn-dep-test
  (let [zloc (z/of-string "{:deps {}}\n")]
    (is (= "{:deps {foo/bar {:mvn/version \"0.5.222\"}}}\n"
           (-> zloc
               (sut/z-add-mvn-dep {:id 'foo/bar :version "0.5.222"})
               z/root-string))))
  (let [zloc (z/of-string
              (str
               "{:deps {org.clojure/clojure {:mvn/version \"1.10.3\"}\n"
               "        metosin/reitit      {:mvn/version \"0.5.13\"}\n"
               "        juxt/crux-core      {:mvn/version \"21.06-1.17.1-beta\"}\n"
               "        juxt/crux-rocksdb   {:mvn/version \"21.06-1.17.1-beta\"}}}\n"))]
    (is (= (str "{:deps {org.clojure/clojure {:mvn/version \"1.10.3\"}\n"
                "        metosin/reitit      {:mvn/version \"0.5.13\"}\n"
                "        juxt/crux-core      {:mvn/version \"21.06-1.17.1-beta\"}\n"
                "        juxt/crux-rocksdb   {:mvn/version \"21.06-1.17.1-beta\"} \n" ;;TODO weird little space here
                "        foo/bar {:mvn/version \"0.5.222\"}}}\n"))
     (-> zloc
         (sut/z-add-mvn-dep {:id 'foo/bar :version "0.5.222"})
         z/root-string))))
