# Copyright 2017 Amazon Web Services, Inc. or its affiliates. All rights
# reserved.


def get_from_cache(client, key):
    print(
        "\nRUNNING SOLUTION CODE:",
        "get_from_cache!",
        "Follow the steps in the lab guide to replace this method with your own implementation.")
    return client.get(key)


def create_memcached_client(memcache, clusterEndpoint):
    print(
        "\nRUNNING SOLUTION CODE:",
        "create_memcached_client!",
        "Follow the steps in the lab guide to replace this method with your own implementation.")
    return memcache.Client([clusterEndpoint], debug=0)


def store_in_cache(client, key, value):
    print(
        "\nRUNNING SOLUTION CODE:",
        "store_in_cache!",
        "Follow the steps in the lab guide to replace this method with your own implementation.")
    client.set(key, value, 3600)
