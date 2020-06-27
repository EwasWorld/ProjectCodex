# Project Codex Code Styling Guides
So I can call myself a liar when I don't follow my own decisions

## General
- Max line length: 120 characters
- Favour having as few indents as possible (for example, use `if (condition) return \n <do something>` over `if (!condition) <do something>`)
- Favour `nullableItem?.let { }` over `if nullableItem == null { }`. The advantage with let is that everywhere inside the braces the `nullableItem` will be of a non-nullable type, whereas with the if statement, if `nullableItem` is mutable, it will require the addition of many `!!` to ensure `nullableItem` is still not null

## Styling Tools
- Follow Android Studio's recommendations for redundant type declarations and such like
- Use Android Studio's formatter (with the agreed settings), ensuring the option to format on commit is enabled
- Do not use the code rearranger (I don't like how it doesn't play fair with the version control system)

## Testing
- Each test should be testing ONE thing. To test several cases for one method, each case should have its own test
- Sanity checks like `check` can and should be left throughout helper methods such as those in TestData.kt (in actual code, other than initial checks, this should be checked using a test)

## Methods
- A section of code should in general only be extracted into a method if it is used more than once or if it is separating logic from ui
- Separate any initial `require` and `check` lines from the rest of the code with a new line

## SQL and DAOs
- If the SQL statement gets too long, it should be wrapped in triple quotes for a multi-line string with each of the SQL parts (`SELECT`, `FROM`, `WHERE`, etc.) on new lines (examples below). Similar sections should be grouped together
- SQL keywords should be in capitals
- If a query contains a JOIN or multiple tables, in order of preference it should be placed in the DAO of:
    1. The main parameter (if there are any). The first example below would go in `ArcherRoundsDao`
    2. The majority return type (if there is one). The second example below would also go in `ArcherRoundsDao` as its return type is mostly `ArcherRound`
    3. The table that is most affected

```kotlin
@Query(
        """
            SELECT rounds.* 
            FROM archer_rounds INNER JOIN rounds ON archer_rounds.roundId = rounds.roundId 
            WHERE archerRoundId = :archerRoundId
        """
)
fun getRoundInfo(archerRoundId: Int): LiveData<Round>

@Query(
        """
            SELECT archer_rounds.*, rounds.displayName AS roundName, round_sub_types.name AS roundSubTypeName
            FROM archer_rounds LEFT JOIN rounds ON archer_rounds.roundId = rounds.roundId
                               LEFT JOIN round_sub_types ON archer_rounds.roundSubTypeId = round_sub_types.subTypeId
                                                         AND archer_rounds.roundId = round_sub_types.roundId
        """
)
fun getAllArcherRoundsWithName(): LiveData<List<ArcherRoundWithName>>
```

## Types
- List should be used over Array [Reasoning][1] [Differences][2]
[1]: https://medium.com/@appmattus/effective-kotlin-item-28-prefer-lists-to-arrays-c597d8dfa335
[2]: https://stackoverflow.com/questions/36262305/difference-between-list-and-array-types-in-kotlin

## Parameters
- Parameters should in general be defined in the order of constants, `val`, `var`
- Constructor parameters should be in the order `val`, `var`, constructor-only

## Braces
- If statements should ALWAYS use braces unless they can be completely contained on one line e.g. val test = if (isAwesome) "yes" else "no"

## Comments
- Classes, methods, and member fields should be commented using [KDoc][3] /** comments
- Any throws should be documented with @throws in the method comment
- Methods with initial `require` and `check` should document these requirements in the method comment
- Block /* comments should be used to introduce sections of code and split up larger methods
- All other comments should be //

[3]: https://kotlinlang.org/docs/reference/kotlin-doc.html

## TODOs
- Should only be used when a specific piece of code needs to be highlighted for something that cannot be done now
- There should always be a relevant card on the Trello so that the work is not missed