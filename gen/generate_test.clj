(ns generate-test
  (:require [clojure.test :as t]
            [generate :as g]))

(defn read-test-source
  [service-name]
  (-> (format "gen/test-data/%s-api-v1.34.28.go" service-name)
      slurp))

(def dynamodb-v1.34.28-fns
  ["BatchGetItem"
   "BatchWriteItem"
   "CreateBackup"
   "CreateGlobalTable"
   "CreateTable"
   "DeleteBackup"
   "DeleteItem"
   "DeleteTable"
   "DescribeBackup"
   "DescribeContinuousBackups"
   "DescribeContributorInsights"
   "DescribeEndpoints"
   "DescribeGlobalTable"
   "DescribeGlobalTableSettings"
   "DescribeLimits"
   "DescribeTable"
   "DescribeTableReplicaAutoScaling"
   "DescribeTimeToLive"
   "GetItem"
   "ListBackups"
   "ListContributorInsights"
   "ListGlobalTables"
   "ListTables"
   "ListTagsOfResource"
   "PutItem"
   "Query"
   "RestoreTableFromBackup"
   "RestoreTableToPointInTime"
   "Scan"
   "TagResource"
   "TransactGetItems"
   "TransactWriteItems"
   "UntagResource"
   "UpdateContinuousBackups"
   "UpdateContributorInsights"
   "UpdateGlobalTable"
   "UpdateGlobalTableSettings"
   "UpdateItem"
   "UpdateTable"
   "UpdateTableReplicaAutoScaling"
   "UpdateTimeToLive"])

(def s3-v1.34.28-fns
  ["AbortMultipartUpload"
   "CompleteMultipartUpload"
   "CopyObject"
   "CreateBucket"
   "CreateMultipartUpload"
   "DeleteBucket"
   "DeleteBucketAnalyticsConfiguration"
   "DeleteBucketCors"
   "DeleteBucketEncryption"
   "DeleteBucketInventoryConfiguration"
   "DeleteBucketLifecycle"
   "DeleteBucketMetricsConfiguration"
   "DeleteBucketPolicy"
   "DeleteBucketReplication"
   "DeleteBucketTagging"
   "DeleteBucketWebsite"
   "DeleteObject"
   "DeleteObjectTagging"
   "DeleteObjects"
   "DeletePublicAccessBlock"
   "GetBucketAccelerateConfiguration"
   "GetBucketAcl"
   "GetBucketAnalyticsConfiguration"
   "GetBucketCors"
   "GetBucketEncryption"
   "GetBucketInventoryConfiguration"
   "GetBucketLifecycle"
   "GetBucketLifecycleConfiguration"
   "GetBucketLocation"
   "GetBucketLogging"
   "GetBucketMetricsConfiguration"
   "GetBucketPolicy"
   "GetBucketPolicyStatus"
   "GetBucketReplication"
   "GetBucketRequestPayment"
   "GetBucketTagging"
   "GetBucketVersioning"
   "GetBucketWebsite"
   "GetObject"
   "GetObjectAcl"
   "GetObjectLegalHold"
   "GetObjectLockConfiguration"
   "GetObjectRetention"
   "GetObjectTagging"
   "GetObjectTorrent"
   "GetPublicAccessBlock"
   "HeadBucket"
   "HeadObject"
   "ListBucketAnalyticsConfigurations"
   "ListBucketInventoryConfigurations"
   "ListBucketMetricsConfigurations"
   "ListBuckets"
   "ListMultipartUploads"
   "ListObjectVersions"
   "ListObjects"
   "ListObjectsV2"
   "ListParts"
   "PutBucketAccelerateConfiguration"
   "PutBucketAcl"
   "PutBucketAnalyticsConfiguration"
   "PutBucketCors"
   "PutBucketEncryption"
   "PutBucketInventoryConfiguration"
   "PutBucketLifecycle"
   "PutBucketLifecycleConfiguration"
   "PutBucketLogging"
   "PutBucketMetricsConfiguration"
   "PutBucketNotification"
   "PutBucketNotificationConfiguration"
   "PutBucketPolicy"
   "PutBucketReplication"
   "PutBucketRequestPayment"
   "PutBucketTagging"
   "PutBucketVersioning"
   "PutBucketWebsite"
   "PutObject"
   "PutObjectAcl"
   "PutObjectLegalHold"
   "PutObjectLockConfiguration"
   "PutObjectRetention"
   "PutObjectTagging"
   "PutPublicAccessBlock"
   "RestoreObject"
   "SelectObjectContent"
   "UploadPart"
   "UploadPartCopy"])

(t/deftest clj-fn-name-test
  (t/is (= (g/clj-fn-name "ListObjectsV2")
           "list-objects-v2"))
  (t/is (= (g/clj-fn-name "BatchGetItem")
           "batch-get-item")))

(t/deftest find-fns-test
  (let [dyn-api-source (read-test-source "dynamodb")]
    (t/is (= (g/find-fns dyn-api-source)
             dynamodb-v1.34.28-fns)))

  (let [s3-api-source (read-test-source "s3")]
    (t/is (= (g/find-fns s3-api-source)
             s3-v1.34.28-fns))))

(t/deftest find-services-fns-test
  (with-redefs [g/read-service-source read-test-source]
    (t/is (= (g/find-services-fns ["dynamodb" "s3"])
             (sorted-map "dynamodb" dynamodb-v1.34.28-fns
                         "s3" s3-v1.34.28-fns)))))
