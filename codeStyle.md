# Project Codex Code Styling Guides

## TODOs
- Should only be used when a specific piece of code needs to be highlighted for something that cannot be done now
- There should always be a relevant card on the Trello so that the work is not missed

## Types
- List should be used over Array [Reasoning][1] [Differences][2]
[1]: https://medium.com/@appmattus/effective-kotlin-item-28-prefer-lists-to-arrays-c597d8dfa335
[2]: https://stackoverflow.com/questions/36262305/difference-between-list-and-array-types-in-kotlin

## Braces
- If statements should ALWAYS use braces unless they can be completely contained on one line e.g. val test = if (isAwesome) "yes" else "no"

## Comments
- Classes, methods, and member fields should be commented using doc /** comments
- Any throws should be documented with @throws in the method comment
- Block /* comments should be used to introduce sections of code and split up larger methods
- All other comments should be //
