// Copyright 2017 Amazon Web Services, Inc. or its affiliates. All rights reserved.

using System.Diagnostics;
using Amazon.ElastiCacheCluster;
using Enyim.Caching;
using Enyim.Caching.Memcached;

namespace Lab8
{
    public static class CacheManager
    {
        public static MemcachedClient memcachedClient = null;

        public static void Main()
        {
            Init();
        }

        public static void Init()
        {
            // Sets up the DynamoDB table with sample data about pharmaceutical usage
            DynamoDBManager.Init();
            NewMemcachedClient();
        }

        public static string GetPharmaInfo(string drugName)
        {
            string pharmaInfoStr = null;
            object pharmaInfo = GetCacheItem(drugName);
            if (pharmaInfo == null)
            {
                pharmaInfo = DynamoDBManager.GetPharmaInfo(drugName);
                SetCacheItem(drugName, pharmaInfo);
            }
            if (pharmaInfo != null)
            {
                pharmaInfoStr = pharmaInfo.ToString();
            }

            return pharmaInfoStr;
        }

        /**
         * Set the memcachedClient instance variable to a new instance of the
         * MemcachedClient class
         */
        private static void NewMemcachedClient()
        {
            // STUDENT TODO 1: Replace the solution with your own code
            memcachedClient = Solution.NewMemcachedClient();
        }

        /**
         * For the given key, retrieve value from cache
         *
         * @param key     Key of the value to get
         * @return        Value of the key
         */
        private static object GetCacheItem(string key)
        {
            // STUDENT TODO 2: Replace the solution with your own code
            return Solution.GetCacheItem(memcachedClient, key);
        }

        /**
         * Store the given key-value pair in cache with an expiration
         * value of 3600 seconds
         *
         * @param key     Key of the value to set
         * @param val     The value of the key to set
         */
        private static void SetCacheItem(string key, object val)
        {
            // STUDENT TODO 3: Replace the solution with your own code
            Solution.SetCacheItem(memcachedClient, key, val);
        }
    }
}
