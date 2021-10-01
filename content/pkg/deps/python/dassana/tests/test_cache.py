from re import match
from time import sleep
from typing import List, Dict
from unittest import TestCase
from uuid import UUID, uuid4

from hypothesis import given, note, strategies as st, settings, assume, Verbosity
from hypothesis.strategies import lists

from dassana.common.aws_client import LambdaTestContext
from dassana.common.cache import generate_hash, configure_ttl_cache


class TestSession:
    class TestClient:
        def __init__(self, name):
            self.name = name
            self.id = uuid4()

    def __init__(self, name):
        self.name = name
        self.miss = 0
        self.hit = 0

    def create_client(self, otherName, *args, **kwargs):
        sleep(0.1)
        self.miss += 1
        return self.TestClient(otherName)


class DassanaCacheTest(TestCase):

    def __init__(self, *args, **kwargs):
        super(DassanaCacheTest, self).__init__(*args, **kwargs)
        name = "dassana-cache-test-lambda-context"
        self.session = TestSession(name)
        self.get_cached_client = configure_ttl_cache()
        self.hash_set = set()

    @given(lists(st.uuids(), min_size=3, max_size=3))
    def test_generate_hash_cross_account(self, ls: List[UUID]):
        keys = {
            'aws_access_key_id': ls[0],
            'aws_secret_access_key': ls[1],
            'aws_session_token': ls[2]
        }
        context = LambdaTestContext('name', env=keys)
        hash_result = generate_hash(lambda x: print('samschai'), context=context)
        matches = match('^[a-f0-9]{32}$', hash_result)
        assert matches.pos == 0 and matches.endpos == 32
        assert hash_result not in self.hash_set
        self.hash_set.add(hash_result)

    @given(st.sampled_from(['ReVe'] * 100))
    @settings(deadline=100)
    def test_client_caching_simple(self, otherName: str):
        self.get_cached_client(self.session.create_client, otherName=otherName)
        note(otherName)
        note(self.session.miss.__str__())
        assert self.session.miss == 1

    @given(
        st.dictionaries(
            st.sampled_from(['otherName', 'labels']),
            st.sampled_from(['ReVe', 'Midzy', 'VIP']),
            min_size=1
        ), st.dictionaries(
            st.sampled_from(['aws_access_key_id', 'aws_secret_access_key', 'aws_session_token']),
            st.sampled_from(['Army', 'SWITH', 'Blackjack'])
        )
    )
    @settings(deadline=100, derandomize=True, max_examples=1000, verbosity=Verbosity.verbose)
    def test_client_caching_complex(self, mapping: Dict, access_keys: Dict):
        assume(mapping.get('otherName') is not None)
        self.get_cached_client(self.session.create_client, **mapping,
                               context=LambdaTestContext('abc', env=access_keys))
        note(mapping.__str__())
        assert self.session.miss <= 500
