from cachetools import TTLCache, LRUCache

CACHE_MAX_SIZE = 1024

lru_cache = LRUCache(CACHE_MAX_SIZE)
