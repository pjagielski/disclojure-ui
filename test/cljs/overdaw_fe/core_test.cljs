(ns overdaw-fe.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [overdaw-fe.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 2 2))))
