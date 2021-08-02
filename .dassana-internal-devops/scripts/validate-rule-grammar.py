import glob
import yaml
import requests
import sys
import json

URL = 'https://2mjkqkzhrj.execute-api.us-east-1.amazonaws.com/'

failUnicode = u'\u2716'
fail = False


def validate_rule(rule, path):
    global fail
    try:
        resp = requests.post(URL,
                             headers={
                                 'x-dassana-rule': rule.rstrip(),
                                 'x-dassana-check-type': 'grammar',
                                 'Content-Type': 'application/json'
                             }, data=json.dumps({}))
        if resp.status_code != 200:
            print('%s\t%s\n\tRule is not valid: \n%s' % (failUnicode, path,
                                                         resp.content))
            fail = True
    except RuntimeError:
        fail = True


# Validate grammar under vendors / normalization
for path in glob.glob('content/workflows/vendors/**/*.yaml', recursive=True):
    with open(path, 'r') as f:
        for rule in yaml.load(f, Loader=yaml.FullLoader)['filter']['rules']:
            validate_rule(rule, path)


# Validate grammar under vendor policies / contextualization
for path in glob.glob('content/workflows/policies/csp/**/*.yaml',
                      recursive=True):
    with open(path, 'r') as f:
        for vendor in yaml.load(f, Loader=yaml.FullLoader)['filter']['vendors']:
            for rule in vendor['rules']:
                validate_rule(rule, path)

if fail:
    sys.exit(1)
