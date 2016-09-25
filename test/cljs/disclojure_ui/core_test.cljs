(ns disclojure-ui.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [disclojure-ui.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 2 2))))
