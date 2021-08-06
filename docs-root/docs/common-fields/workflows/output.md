The output of the workflow returned as name / value pairs.

#### `name`

The key name.

#### `value`

The value which to output for a specified key. This value can be plucked off of the input JSON or from a step's output (using [jq](https://stedolan.github.io/jq/)).

:::note

Even though it looks like the output syntax accepts name / value pairs as an array:

```yaml
output:
    - name: foo
      value: bar
    - name: jummie
      value: wummie
```

The output will actually be a JSON object like the following:

```json
{
	"foo": "bar",
	"jummie": "wummie"
}
```

:::
