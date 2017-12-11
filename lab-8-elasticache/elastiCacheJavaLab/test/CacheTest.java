// Copyright 2017 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import static org.junit.Assert.*;

import net.spy.memcached.MemcachedClient;
import org.junit.Test;

public class CacheTest {

  private static String drugName = "Ibuprofen";

  @Test
  public void test() throws Exception {
    try {
      CacheManager.init();
      String pharmaInfo = null;
      pharmaInfo = getFromCache(CacheManager.memcachedClient, drugName);
      System.out.printf(">>>>> First time: - Value returned from cache: %s%n", pharmaInfo);
      assertNull(pharmaInfo);

      pharmaInfo = CacheManager.getPharmaInfo(drugName);
      assertNotNull(pharmaInfo);
      System.out.printf(
          ">>>>> Value returned by CacheManager.getPharmaInfo method: %s%n", pharmaInfo);

      pharmaInfo = getFromCache(CacheManager.memcachedClient, drugName);
      assertNotNull(pharmaInfo);
      System.out.printf(">>>>> After first time: - Value returned from cache: %s%n", pharmaInfo);

      deleteKeyInCache(CacheManager.memcachedClient, drugName);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  private static String getFromCache(MemcachedClient memcachedClient, String key) {
    String returnValue = null;
    Object value = memcachedClient.get(key);
    if (value instanceof String) {
      returnValue = (String) value;
    }
    return returnValue;
  }

  private static void deleteKeyInCache(MemcachedClient memcachedClient, String key) {
    memcachedClient.delete(key);
  }
}
