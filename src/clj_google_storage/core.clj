(ns clj-google-storage.core
  (:require [clj-http.client :as http]
            [cemerick.url :refer [url url-encode]]
            [clojure.data.json :as json]
            [clj-google.auth :refer [*access-token*]]
            [clojure.java.io :as io]))

(def ^:private storage-base-url "https://www.googleapis.com")
(def ^:private storage-api-version "v1")
(def ^:private default-upload-content-type :application/octet-stream)

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

(defn get-file
  ([bucket file-name]
   (get-file bucket file-name {}))
  ([bucket file-name query-string]
   (let [retrieve-url (storage-retrieve-url bucket file-name)
         request-url (str (assoc retrieve-url :query query-string))
         request-data {:oauth-token  *access-token*
                       :content-type :json
                       :accept       :json}]

     (if-let [json-response (json-data http/get request-url request-data)]
       json-response))))

(defn get-content-file
  ([bucket file-name]
   (get-content-file bucket file-name :string))
  ([bucket file-name content-as]
   (let [retrieve-url (storage-retrieve-url bucket file-name)
         request-url (str (assoc retrieve-url :query {:alt "media"}))
         request-data {:oauth-token *access-token*
                       :accept      :json
                       :as          content-as}]

     (if-let [response (http/get request-url request-data)]
       (response :body)))))

(defn download-file
  [bucket file-name destination-file]
  (if-let [file-content-stream (get-content-file bucket file-name :stream)]
    (with-open [out-stream (io/output-stream destination-file)]
      (io/copy file-content-stream out-stream)
      (byte-array (.length destination-file)))))

(defn upload-file
  ([bucket source destination-path]
   (upload-file bucket source destination-path :media {}))
  ([bucket source destination-path upload-type query-string]
   (let [upload-url (storage-upload-url bucket)
         query-string (merge query-string {:uploadType (name upload-type)
                                           :name       destination-path})

         source-file (get source :file source)
         request-url (str (assoc upload-url :query query-string))
         request-data {:content-length (.length source-file)
                       :content-type   (get source :content-type default-upload-content-type)
                       :body           source-file}]

     (if-let [json-response (json-data http/post request-url request-data)]
       json-response))))