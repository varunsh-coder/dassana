from glob import glob
from yaml import load, FullLoader

failUnicode = u'\u2716'
fail = False


def validate_unique_id(steps):
    global fail
    if steps is None or len(steps) == 0:
        return
    steps_id_list = list(map(lambda x: x['id'], steps))

    visited = set()
    duplicate = [x for x in steps_id_list
                 if x in visited or (visited.add(x) or False)]

    if len(duplicate) == 0:
        pass
    else:
        print('%s\t%s\n\tFound duplicate IDs under steps: %s' %
              (failUnicode, path, duplicate))
        fail = True


for path in glob('content/workflows/**/*.yaml',
                 recursive=True):
    with open(path, 'r') as f:
        steps = load(f, Loader=FullLoader).get('steps')
        validate_unique_id(steps)

if fail:
    exit(1)
