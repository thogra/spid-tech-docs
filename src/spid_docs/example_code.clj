(ns spid-docs.example-code
  (:require [clojure.set :refer [difference]]
            [clojure.string :as str]))

(def examples
  {"action" "???"
   "additionalReceiptInfo" "???"
   "address" "Street"
   "address_country" "Norway"
   "address_formatted" "Street 2, 0123 City, Norway"
   "address_locality" "???"
   "address_postalCode" "0123"
   "address_region" "City"
   "address_streetAddress" "Street"
   "addresses" "???"
   "agreementRef" "???"
   "allowMultiSales" "???"
   "amount" "99"
   "autoRenew" "???"
   "availableStart" "???"
   "availableStop" "???"
   "birthday" "1977-01-31"
   "birthyear" "???"
   "bundle" "???"
   "buyerUserId" "???"
   "campaignId" "???"
   "cancelUri" "???"
   "clientId" "42"
   "clientRef" "???"
   "clientReference" "???"
   "code" "???"
   "content" "???"
   "currency" "NOK"
   "deliveryAddress" "???"
   "description" "???"
   "displayName" "John"
   "email" "johnd@example.com"
   "emails" "???"
   "emails_regex" "???"
   "end_time" "???"
   "expires" "???"
   "familyName" "???"
   "fields" "???"
   "filters" "???"
   "from" "???"
   "fullName" "???"
   "gender" "???"
   "givenName" "???"
   "h" "???"
   "hash" "7374163eed7a0e88f9bf28e128d8da82"
   "hideItems" "???"
   "homeAddress" "???"
   "id" "1337"
   "id/userId/email" "???"
   "invoiceAddress" "???"
   "ip" "???"
   "items" "???"
   "jwt" "???"
   "key" "???"
   "limit" "???"
   "locale" "???"
   "metaData" "???"
   "name" "John Doe"
   "notifyUser" "???"
   "object" "User"
   "ocr" "???"
   "offset" "???"
   "orderId" "???"
   "orderItemId" "???"
   "parentProductId" "???"
   "password" "???"
   "paymentIdentifier" "???"
   "paymentOptions" "???"
   "phoneNumber" "???"
   "phoneNumbers" "???"
   "photo" "???"
   "preferredUsername" "johnd"
   "price" "400"
   "product" "???"
   "productId" "1337"
   "product_id" "???"
   "products" "???"
   "property" "???"
   "purchaseFlow" "???"
   "quantityLimit" "???"
   "query" "???"
   "redeem_limit" "???"
   "redirectUri" "???"
   "requestReference" "???"
   "requireAddress" "???"
   "requireVoucher" "???"
   "role" "???"
   "saleStart" "???"
   "saleStop" "???"
   "section" "???"
   "sellerUserId" "???"
   "since" "???"
   "sort" "???"
   "startDate" "???"
   "start_time" "???"
   "status" "???"
   "stopDate" "???"
   "subid" "???"
   "subscriptionAutoRenew" "???"
   "subscriptionAutoRenewDisabled" "???"
   "subscriptionAutoRenewLockPeriod" "???"
   "subscriptionEmailReceiptLimit" "???"
   "subscriptionFinalEndDate" "???"
   "subscriptionGracePeriod" "???"
   "subscriptionId" "???"
   "subscriptionPeriod" "???"
   "subscriptionRenewPeriod" "???"
   "subscriptionRenewPrice" "???"
   "subscriptionSurveyUrl" "???"
   "subtype" "???"
   "tag" "???"
   "template" "???"
   "templates" "???"
   "title" "???"
   "to" "???"
   "tokenName" "???"
   "trait" "???"
   "traits" "???"
   "trigger" "???"
   "type" "???"
   "unique" "???"
   "until" "???"
   "url" "???"
   "userId" "???"
   "userId/email" "???"
   "utcOffset" "???"
   "value" "???"
   "vat" "???"
   "voucherCode" "???"
   "voucherGroupId" "???"
   "w" "???"})

(defn- replace-path-parameters [url]
  (str/replace url #"\{([^}]+)\}" (fn [[_ match]] (examples match))))

(defn curl-example-code [{:keys [api-path method access-token-types]} params]
  (apply str "curl https://payment.schibsted.no" (replace-path-parameters api-path)
         (when (= "POST" method) " \\\n   -X POST")
         (when (seq access-token-types) " \\\n   -d \"oauth_token=[access token]\"")
         (map (fn [param] (str " \\\n   -d \"" (:name param) "=" (examples (:name param)) "\"")) params)))

(defn clojure-example-code [{:keys [method path]} params]
  (let [sdk-invocation (str "  (sdk/" method " \"" (replace-path-parameters path) "\"" (when (seq params) " {"))
        param-map-indentation (apply str (repeat (count sdk-invocation) " "))]
    (str "(ns example\n  (:require [spid-sdk-clojure.core :as sdk]))\n\n(-> (sdk/create-client \"[client-id]\" \"[secret]\")\n"
         sdk-invocation
         (when (seq params)
           (str (str/join (str "\n" param-map-indentation)
                          (map #(str "\"" (:name %) "\" \"" (examples (:name %)) "\"") params)) "}"))
         "))")))

(defn create-example-code [endpoint]
  (let [params (:parameters endpoint)
        all-params (filter #(= (:type %) :query) params)
        req-params (filter :required? all-params)
        optional-params (difference (set all-params) (set req-params))]
    {:curl {:minimal (curl-example-code endpoint req-params)
            :maximal (when (seq optional-params) (curl-example-code endpoint all-params))}
     :clojure {:minimal (clojure-example-code endpoint req-params)
               :maximal (when (seq optional-params) (clojure-example-code endpoint all-params))}}))
