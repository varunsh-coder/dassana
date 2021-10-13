from re import match
from typing import List
from unittest import TestCase
from uuid import UUID

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
        context = LambdaTestContext(name, env=keys)
        hash_result_1 = generate_hash(name=name, context=context)
        matches = match('^[a-f0-9]{64}$', hash_result_1)
        # First pass, no hits all misses
        assert matches.pos == 0 and matches.endpos == 64
        assert hash_result_1 not in self.hash_set
        self.hash_set.add(hash_result_1)

        # Second pass, all hits no misses
        hash_result_2 = generate_hash(name=name, context=context)
        assert hash_result_2 == hash_result_1
