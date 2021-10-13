from re import match
from time import sleep
from typing import List, Dict
from unittest import TestCase
from uuid import UUID, uuid4

from hypothesis import given, strategies as st
from hypothesis.strategies import lists

from dassana.common.aws_client import LambdaTestContext
from dassana.common.cache import generate_hash


class DassanaCacheTest(TestCase):

    def __init__(self, *args, **kwargs):
        super(DassanaCacheTest, self).__init__(*args, **kwargs)
        self.hash_set = set()

    @given(name=st.sampled_from(['ReVeLuv', 'VIP', 'Midzy', 'Blackjacks']),
           ls=lists(st.uuids(), min_size=3,
                    max_size=3))
    def test_generate_hash_cross_account(self, name: str, ls: List[UUID]):
        keys = {
            'aws_access_key_id': ls[0],
            'aws_secret_access_key': ls[1],
            'aws_session_token': ls[2]
        }
        print(name, ls)
        context = LambdaTestContext(name, env=keys)
        hash_result = generate_hash(name=name, context=context)
        matches = match('^[a-f0-9]{64}$', hash_result)
        print(self.hash_set)
        assert matches.pos == 0 and matches.endpos == 64
        assert hash_result not in self.hash_set
        self.hash_set.add(hash_result)
