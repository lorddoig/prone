(ns prone.middleware-test
  (:require [prone.middleware :refer :all]
            [prone.debug :refer [debug *debug-data*]]
            [clojure.test :refer :all]))

(deftest resets-debug-on-every-request-test

  (declare inner-debug-data)

  ((wrap-exceptions (fn [req] (def inner-debug-data @*debug-data*))) {})
  (is (= [] inner-debug-data))

  ((wrap-exceptions (fn [req]
                      (debug "Oh noes")
                      (def inner-debug-data @*debug-data*))) {})
  (is (= ["Oh noes"] (map :message inner-debug-data)))

  ((wrap-exceptions (fn [req]
                      (debug "Oh noes")
                      (debug "Halp!")
                      (def inner-debug-data @*debug-data*))) {})
  (is (= ["Oh noes" "Halp!"] (map :message inner-debug-data))))

(deftest renders-debug-page-on-debug
  (is (= 203 (:status ((wrap-exceptions (fn [req]
                                          (debug "I need help")
                                          {:status 200})) {})))))

(deftest excludes-unwanted-clients
  (are [status headers] (= status (:status
                                   ((wrap-exceptions
                                     (fn [req]
                                       (debug "I need help")
                                       {:status 200})
                                     {:skip-prone? (fn [req]
                                                     (contains? (:headers req) "postman-token"))})
                                    {:headers headers})))
       200 {"postman-token" "12345"
            "other" "value"}
       203 {"random" "string"}
       203 {}))
