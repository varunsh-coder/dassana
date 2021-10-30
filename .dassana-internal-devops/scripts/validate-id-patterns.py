from glob import glob
from yaml import load, FullLoader
import re


failUnicode = u'\u2716'
fail = False


pattern = re.compile("^[a-z0-9-]+$")
steps_pattern = re.compile("^[a-zA-Z0-9-_]+$")

# Validates workflow id against pattern ^[a-z0-9-]
def validate_workflow_id(id, path):
    global fail
    if pattern.match(id):
        pass
    else:
        print('%s\t%s\n\tWorkflow id [\'%s\'] does not match pattern [a-z0-9-]' %
              (failUnicode, path, id))
        fail = True


# Validates steps id against pattern ^[a-z0-9-]
def validate_steps_id(steps, path):
    global fail
    if steps is None or len(steps) == 0:
        return
    steps_id_list = list(map(lambda x: x['id'], steps))

    ids = set()
    error_ids = [x for x in steps_id_list
                 if (not steps_pattern.match(x)) or (ids.add(x) or False)]

    if len(error_ids) == 0:
        pass
    else:
        print('%s\t%s\n\tSteps id[s] %s do[es] not match pattern [a-zA-Z0-9-_]' %
              (failUnicode, path, error_ids))
        fail = True


# Validates steps id against pattern ^[a-z0-9-]
def validate_risk_config_id(rules, path):
    global fail
    if rules is None or len(rules) == 0:
        return
    steps_id_list = list(map(lambda x: x['id'], rules))

    ids = set()
    error_ids = [x for x in steps_id_list
                 if (not pattern.match(x)) or (ids.add(x) or False)]

    if len(error_ids) == 0:
        pass
    else:
        print('%s\t%s\n\tRisk-config id[s] %s do[es] not match pattern [a-z0-9-]' %
              (failUnicode, path, error_ids))
        fail = True


## parse through all
for path in glob('content/workflows/**/*.yaml',
                 recursive=True):
    with open(path, 'r') as f:
        id = load(f, Loader=FullLoader).get('id')
        validate_workflow_id(id, path)

    with open(path, 'r') as g:
        steps = load(g, Loader=FullLoader).get('steps')
        validate_steps_id(steps, path)

    with open(path, 'r') as h:
        rules_main = load(h, Loader=FullLoader).get('risk-config')
        if rules_main is not None and len(rules_main) != 0:
            rules = rules_main.get('rules')

            # checks the subrule ids pattern
            subrules = rules_main.get('subrules')
            validate_risk_config_id(subrules, path)
        else:
            rules = None
        validate_risk_config_id(rules, path)

if fail:
    exit(1)
