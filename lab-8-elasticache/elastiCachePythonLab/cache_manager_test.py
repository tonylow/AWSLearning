# Copyright 2017 Amazon Web Services, Inc. or its affiliates. All rights
# reserved.

import unittest
import warnings
import sys
import memcache
import cache_manager
import utils

CLUSTER_CONFIG_ENDPOINT = cache_manager.CLUSTER_CONFIG_ENDPOINT


class ElastiCacheTestCase(unittest.TestCase):
    drugName = "Ibuprofen"
    cacheMgr = memcache.Client([CLUSTER_CONFIG_ENDPOINT], debug=0)

    def test(self):
        warnings.simplefilter("ignore", ResourceWarning)
        try:
            drugName = ElastiCacheTestCase.drugName
            cacheMgr = ElastiCacheTestCase.cacheMgr
            cache_manager.setup()

            pharmaInfo = get_from_cache(cacheMgr, drugName)
            print("First Access - Item from cache", pharmaInfo)
            self.assertIsNone(pharmaInfo)

            pharmaInfo = cache_manager.get_pharma_info(drugName)
            self.assertIsNotNone(pharmaInfo)
            print("Item from cache or DB", pharmaInfo)

            pharmaInfo = get_from_cache(cacheMgr, drugName)
            self.assertIsNotNone(pharmaInfo)
            print("Next Access - Item from cache", pharmaInfo)
            delete_key_in_cache(cacheMgr, drugName)

        except Exception as err:
            print("Error message: {0}".format(err))
            sys.exit(1)
        cacheMgr = None


def get_from_cache(cacheMgr, cacheKey):
    try:
        pharmaInfo = None
        pharmaInfo = cacheMgr.get(cacheKey)
    except Exception as err:
        print("Error message: {0}".format(err))
    return pharmaInfo


def delete_key_in_cache(cacheMgr, cacheKey):
    try:
        cacheMgr.delete(cacheKey)
    except Exception as err:
        print("Error message: {0}".format(err))

if __name__ == '__main__':
    unittest.main()
