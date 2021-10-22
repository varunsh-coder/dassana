from os import environ, path

WORKFLOW_DIR = '%s/**/*.yaml' % environ.get('WORKFLOW_DIR') if environ.get('WORKFLOW_DIR', None) else \
    '/opt/aws/**/*.yaml'
INPUT_JSON_PATH = '%s/../../input.json' % path.dirname(path.abspath(__file__))
