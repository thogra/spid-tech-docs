(ns spid-docs.sample-responses-test
  (:require [midje.sweet :refer :all]
            [spid-docs.sample-responses :refer :all]
            [test-with-files.core :refer [with-tmp-dir tmp-dir]]))

(fact "It formats sample response"
      (-> {:code "201" :data {:clientId "666" :url "http://vg.no"}}
          format-sample-response ) => "{\"clientId\":\"[Your client ID]\", \"url\":\"http://vg.no\"}\n")

(fact
 (inject-deps {} {} :keyword) => :keyword
 (inject-deps {} {} 42) => 42
 (inject-deps {} {} '(42)) => '(42)
 (inject-deps {:jane {:response {:data 42}}} {'user :jane} 'user) => 42
 (inject-deps {:jane {:response {:data 42}}} {'user :jane} '(user)) => '(42)
 (inject-deps {:jane {:response {:data 42}}} {'user :jane} {:id 'user}) => {:id 42}
 (inject-deps {:jane {:response {:data 42}}} {'user :jane} {:stuff [13 'user]}) => {:stuff [13 42]}
 (->> [13 '(:name user)]
      (inject-deps {:jane {:response {:data {:name "Jane"}}}} {'user :jane})
      eval) => [13 "Jane"])

(fact "Makes dependencies available as bindings path params"
      (let [sample-def {:method :GET
                        :path "/somewhere"
                        :path-params '{:name (:name user)}
                        :dependencies {'user :john}}
            loaded-samples [{:id :john
                             :response {:success? true
                                        :data {:name "Dude"}}}]
            interpolated (interpolate-sample-def sample-def loaded-samples)]
        (:path-params interpolated) => {:name "Dude"}))

(fact "Makes dependencies available as bindings for request params"
      (let [sample-def {:method :GET
                        :path "/somewhere"
                        :params '{:name (:name user)}
                        :dependencies {'user :john}}
            loaded-samples [{:id :john
                             :response {:success? true
                                        :data {:name "Dude"}}}]
            interpolated (interpolate-sample-def sample-def loaded-samples)]
        (:params interpolated) => {:name "Dude"}))

(fact "Interpolates parameters in path"
      (let [sample-def {:method :GET
                        :path "/somewhere/{userId}"
                        :path-params {:userId 42}}]
        (:path (interpolate-sample-def sample-def)) => "/somewhere/42"))

(fact "Interpolates dependency injected parameters in path"
      (let [sample-def {:method :GET
                        :path "/somewhere/{userId}"
                        :path-params {:userId '(:id user)}
                        :dependencies {'user :john}}
            loaded-samples [{:id :john
                             :response {:data {:id 42}}}]
            interpolated (interpolate-sample-def sample-def loaded-samples)]
        (:path interpolated) => "/somewhere/42"))




(fact "It generates sample response files."
      (with-tmp-dir
        (with-redefs [spid-docs.api-client/GET (fn [path] {:data {:status 123, :path path}})
                      target-directory tmp-dir]
          (generate-sample-response {:path "/status", :method :GET})
          (slurp (str tmp-dir "/status-get.json")) => "{\"status\":123, \"path\":\"/status\"}\n"
          (slurp (str tmp-dir "/status-get.jsonp")) => "callback({\"status\":123, \"path\":\"/status\"});\n")))
