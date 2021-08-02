import glob
from yaml import load, FullLoader
from cfn_flip import load_yaml

failUnicode = u'\u2716'
fail = False

with open('content/pkg/template.yaml', 'r') as f:
    resources = load_yaml(f).get('Resources')
    function_names = set(map(lambda x: x,
                             filter(lambda x:
                                    resources[x]['Type']
                                    == 'AWS::Serverless::Function',
                                    resources)))
    for path in glob.glob('content/actions/**/dassana-action.yaml',
                          recursive=True):
        with open(path, 'r') as action:
            function_name = load(action, Loader=FullLoader).get('id')
            if not (function_name in function_names):
                fail = True
                print('%s\t%s\n\tThis action %s has not been included in SAM '
                      'Template' %
                      (failUnicode, path, function_name))
if fail:
    exit(1)
