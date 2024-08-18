Stack Follies  comes with this absurd madness of mine to make item models animate-able.
Because I was too lazy to create entity models and wanted the ability to dynamically animate anything and everything.

I did my best at making it as stable as I could, but please do not hesitate to report any issue.
It was a pure headache to understand how to make it work, but now I should be able to fix issues much more easily.

## Requirements

In order to make it work, it is expected that a model matches the BlockBench json format.
This means, the Json is expected to contain the following fields additionally to the vanilla ones:
* Main Json:
  * `sf$computeGroups`: A boolean, required to be `true`, enables computing any other field in the model. If absent, the group processing will not be done for the current model file.
  * `texture_size`: An array of two integers, specifying the model's texture dimensions
  * `groups` An array of group elements. See [groups](#groups)
* Elements:
  * `name`: The element name, required to identify the element for targeted animations.<br>
    Beware, names must be unique both among elements and with the group ones in order to properly distinguish each element.

### Groups

Groups must be declared as such:
```json lines
{
  "name": "<the group's name>", // must be unique
  "origin": [x, y, z], // integers, the group's origin coordinates in the model, used as based upon referencing an animation's anchor point
  "children": [
    a, b, c, ... ,// integers, the indexes of the group's children in the main json's 'elements' array
    { "name":  ...}, ... // group(s), being registered as subgroups of the current one. This allows to manipulate multiple groups at once and do more complicated animations without having to calculate detailed variations
  ]
}
```

## How does it work

It theoretically just does.

More specifically, it retrieves the required element and group data from the model JSON, and adds its own layer
of parsing over the vanilla one.

Then, based on this data, it constructs a matching entity-like model, that can be tweaked and animated at will, for
more interesting rendering without the struggle of building entity models, as well as free customization via
resource-packs.