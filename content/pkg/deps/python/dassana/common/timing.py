import logging
from functools import wraps
from time import time
from typing import Dict, List


def timing(f, measurements: Dict[frozenset, List], args_measure_func=lambda x: None, kw_measure_func=lambda y: None):
    @wraps(f)
    def wrap(*args, **kw):
        nonlocal measurements
        ts = time()
        result = f(*args, **kw)
        te = time()
        measurement_time = te - ts
        logging.log(1, 'func:%r args:%r; kw: %r] took: %2.4f sec', args, kw, te - ts)
        freeze = frozenset([f.__name__, *args_measure_func(args), *kw_measure_func(kw)])
        if freeze in measurements:
            msur = measurements[freeze]
            msur.append(measurement_time)
        else:
            measurements[freeze] = [measurement_time]
        return result

    return wrap
