// Copyright 2017 Amazon Web Services, Inc. or its affiliates. All rights reserved.

using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Diagnostics;
using Enyim.Caching;

namespace Lab8
{
    [TestClass]
    public class CacheTests
    {
        private static string drugName = "Ibuprofen";

        [TestMethod]
        public void TestElastiCacheMethod()
        {
            try
            {
                CacheManager.Init();
                string pharmaInfo = null;

                pharmaInfo = GetFromCache(CacheManager.memcachedClient, drugName);
                Debug.WriteLine(String.Format(">>>>> First time: - Value returned from cache: {0} ",  pharmaInfo));
                Assert.IsNull(pharmaInfo, "Test failed: found an item in cache - first try should have been null.");

                pharmaInfo = CacheManager.GetPharmaInfo(drugName);
                Assert.IsNotNull(pharmaInfo, "Failed to find " + pharmaInfo);
                Debug.WriteLine(String.Format(">>>>> Value returned by CacheManager.GetPharmaInfo method: {0}", pharmaInfo));

                pharmaInfo = GetFromCache(CacheManager.memcachedClient, drugName);
                Assert.IsNotNull(pharmaInfo, "Failed to find " + pharmaInfo);
                Debug.WriteLine(String.Format(">>>>> After first time: - Value returned from cache: {0}", pharmaInfo));

                DeleteKeyInCache(CacheManager.memcachedClient, drugName);
            }
            catch (Exception)
            {
                throw;
            }
            Debug.WriteLine("Test method passed.");
        }

        private static string GetFromCache(MemcachedClient memcachedClient, string key)
        {
            string returnValue = null;
            object value = memcachedClient.Get(key);
            if (value != null)
            {
                returnValue = (string)value;
            }
            return returnValue;
        }

        private static void DeleteKeyInCache(MemcachedClient memcachedClient, string key)
        {
            memcachedClient.Remove(key);
        }
    }
}
