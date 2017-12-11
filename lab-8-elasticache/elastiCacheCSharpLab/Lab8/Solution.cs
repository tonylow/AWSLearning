// Copyright 2017 Amazon Web Services, Inc. or its affiliates. All rights reserved.

using Amazon.ElastiCacheCluster;
using Enyim.Caching;
using Enyim.Caching.Memcached;
using System;
using System.Diagnostics;

namespace Lab8
{
    class Solution
    {
        public static MemcachedClient NewMemcachedClient()
        {
            Debug.WriteLine(String.Format("RUNNING SOLUTION CODE: {0}! Follow the steps in the lab guide to replace this method with your own implementation.\n", "NewMemcachedClient"));
            ElastiCacheClusterConfig config = new ElastiCacheClusterConfig();
            MemcachedClient memcachedClient = new MemcachedClient(config);
            return memcachedClient;
        }

        public static object GetCacheItem(MemcachedClient memcachedClient, string key)
        {
            Debug.WriteLine(String.Format("RUNNING SOLUTION CODE: {0}! Follow the steps in the lab guide to replace this method with your own implementation.\n", "GetCacheItem"));
            object valueFromCache = null;
            valueFromCache = memcachedClient.Get(key);
            return valueFromCache;
        }

        public static void SetCacheItem(MemcachedClient memcachedClient, string key, object val)
        {
            Debug.WriteLine(String.Format("RUNNING SOLUTION CODE: {0}! Follow the steps in the lab guide to replace this method with your own implementation.\n", "SetCacheItem"));
            System.TimeSpan validFor = System.TimeSpan.FromSeconds(3600);
            memcachedClient.Store(StoreMode.Set, key, val, validFor);
        }
    }
}
