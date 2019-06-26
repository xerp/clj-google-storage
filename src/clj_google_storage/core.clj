(ns clj-google-storage.core
  (:require [clj-http.client :as http]
            [cemerick.url :refer [url url-encode]]
            [clojure.data.json :as json]
            [clj-google.auth :refer [*access-token*]]
            [clojure.java.io :as io])
  (:import (java.io File)))

(def ^:private storage-base-url "https://www.googleapis.com")
(def ^:private storage-api-version "v1")

(defn- storage-retrieve-url
  [bucket-name object-name]
  (let [bucket-name-encoded (url-encode bucket-name)
        object-name-encoded (url-encode object-name)]
    (apply url [storage-base-url "storage" storage-api-version "b" bucket-name-encoded "o" object-name-encoded])))

(defn- storage-upload-url
  [bucket-name]
  (let [bucket-name-encoded (url-encode bucket-name)]
    (apply url [storage-base-url "upload/storage" storage-api-version "b" bucket-name-encoded "o"])))

(defn- json-data
  [http-fn request-url data]
  (if-let [response (http-fn request-url data)]
    (let [json-response (json/read-str (response :body) :key-fn keyword)]
      json-response)))

(defn get-object
  ([bucket object-name]
   (get-object bucket object-name {}))
  ([bucket object-name query]
   (let [retrieve-url (storage-retrieve-url bucket object-name)
         request-url (str (assoc retrieve-url :query query))
         request-data {:oauth-token  *access-token*
                       :content-type :json
                       :accept       :json}]
     (if-let [json-response (json-data http/get request-url request-data)]
       json-response))))

(defn download-content-object
  [bucket object-name]
  (let [retrieve-url (storage-retrieve-url bucket object-name)
        request-url (str (assoc retrieve-url :query {:alt "media"}))
        request-data {:oauth-token *access-token*
                      :accept      :json}]
    (if-let [response (http/get request-url request-data)]
      (response :body))))

(defn download-object
  [bucket object-name destination-file]
  (if-let [file-content (download-content-object bucket object-name)]
    (with-open [writer (io/writer destination-file)]
      (.write writer file-content)
      true)))