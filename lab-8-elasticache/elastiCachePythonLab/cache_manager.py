# Copyright 2017 Amazon Web Services, Inc. or its affiliates. All rights
# reserved.

import memcache
import sys
import utils as dynamoDB_manager
import solution

# STUDENT TODO 1: Set the cluster configuration endpoint
CLUSTER_CONFIG_ENDPOINT = "<ElastiCache-Configuration-Endpoint>"

# Welcome to the AWS Python SDK! Let's build a Pharmaceutical drug listing!


def get_cache_item(client, cacheKey):
    pharma_info = None
    try:
        pharma_info = client.get(cacheKey)
    except Exception as err:
        print("Error message: {0}".format(err))
    return pharma_info

# Returns pharmaceutical usage information for the given drug.
# Attempts to retrieve item from cache.
# If the item is not found in cache, retrieves the information from
# DynamoDB and updates cache.


def get_pharma_info(drugName, clusterEndpoint=CLUSTER_CONFIG_ENDPOINT):
    # Retrieves pharmaceutical usage information from cache for the given drug
    # name. Set the cache if not available
    pharma_info = None
    try:
        mclient = memcache.Client([clusterEndpoint], debug=0)
        pharma_info = get_cache_item(mclient, drugName)
        if not pharma_info:
            pharma_info = dynamoDB_manager.get_info(drugName)
            if not pharma_info:
                print("Given drug name not available in the table")
                return None
            set_pharma_item(mclient, drugName, pharma_info)
            pharma_info = get_cache_item(mclient, drugName)
            if not pharma_info:
                print(
                    "Unable to set the cache for the given DrugName:{0}".format(drugName))
    except Exception as err:
        print("Error message: {0}".format(err))
    return pharma_info


def set_pharma_item(client, cacheKey, usageInfo):
    # Retrieves usage information from DynamoDB and then populates the cache
    # with that information
    try:
        client.set(cacheKey, usageInfo, 3600)
    except Exception as err:
        print("Error message: {0}".format(err))


def setup():
    dynamoDB_manager.setup()


def create_memcached_client(memcache, clusterEndpoint):
    """Create a client for Memcached

    Keyword arguments:
    memcache -- the memcache object
    clusterEndpoint -- memcached cluster endpoint
    """

    # STUDENT TODO 2: Replace the solution with your own code
    return solution.create_memcached_client(memcache, clusterEndpoint)


def get_from_cache(client, key):
    """Gets a value from the cache

    Keyword arguments:
    client -- memcached client
    key -- key of value to get from cache
    """

    # STUDENT TODO 3: Replace the solution with your own code
    return solution.get_from_cache(client, key)


def store_in_cache(client, key, value):
    """Store a value in the cache

    Keyword arguments:
    client -- memcached client
    key -- key to store in cache
        value -- value to store in cache
    """

    # STUDENT TODO 4: Replace the solution with your own code
    solution.store_in_cache(client, key, value)


def main(drugName):
        # Setting up DynamoDB
    setup()
    pharma_info = get_pharma_info(drugName)
    return pharma_info

if __name__ == '__main__':
    drugName = 'Octinoxate'
    if len(sys.argv) > 1:
        drugName = sys.argv[1]
    pharma_info = main(drugName)
    print("Retrieved pharmaceutical information:")
    print("Given DrugName: {0}".format(drugName))
    print("Retrieved DrugInfo: {0}".format(str(pharma_info)))
