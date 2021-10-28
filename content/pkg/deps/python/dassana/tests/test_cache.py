from re import match
from time import sleep
from typing import List, Dict
from unittest import TestCase
from uuid import UUID, uuid4

from hypothesis import given, note, strategies as st, settings, assume, Verbosity
from hypothesis.strategies import lists

from dassana.common.aws_client import LambdaTestContext
from dassana.common.cache import generate_hash


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
        matches = match('^[a-f0-9]{64}$', hash_result)
        assert matches.pos == 0 and matches.endpos == 64
        assert hash_result not in self.hash_set
        self.hash_set.add(hash_result)
