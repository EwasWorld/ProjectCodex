package eywa.projectcodex

import com.beust.klaxon.KlaxonException
import eywa.projectcodex.database.UpdateType
import eywa.projectcodex.database.entities.Round
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance
import eywa.projectcodex.database.entities.RoundSubType
import eywa.projectcodex.logic.DefaultRoundInfo
import eywa.projectcodex.logic.checkDefaultRounds
import eywa.projectcodex.logic.roundsFromJson
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import java.io.File

@RunWith(Suite::class)
@Suite.SuiteClasses(
        DefaultRoundInfoUnitTest.RoundsFromJsonUnitTests::class,
        DefaultRoundInfoUnitTest.CheckDefaultRoundsUnitTests::class
)
class DefaultRoundInfoUnitTest {
    class RoundsFromJsonUnitTests {
        private class TestData {
            companion object {
                const val START = """{"rounds": ["""
                const val END = """]}"""
                const val YORK_MAIN = """
              "roundName": "York",
              "outdoor": true,
              "isMetric": false,
              "fiveArrowEnd": false,
              "permittedFaces": []
            """
                const val YORK_SUB_TYPES = """
              "roundSubTypes": [
                {
                  "roundSubTypeId": 1,
                  "subTypeName": "York",
                  "gentsUnder": null,
                  "ladiesUnder": null
                },
                {
                  "roundSubTypeId": 2,
                  "subTypeName": "Hereford (Bristol I)",
                  "gentsUnder": 18,
                  "ladiesUnder": null
                },
                {
                  "roundSubTypeId": 3,
                  "subTypeName": "Bristol II",
                  "gentsUnder": 16,
                  "ladiesUnder": 18
                },
                {
                  "roundSubTypeId": 4,
                  "subTypeName": "Bristol V",
                  "gentsUnder": 0,
                  "ladiesUnder": 12
                }
              ]
            """
                const val YORK_ARROW_COUNTS = """
              "roundArrowCounts": [
                {
                  "distanceNumber": 1,
                  "faceSizeInCm": 122,
                  "arrowCount": 72
                },
                {
                  "distanceNumber": 2,
                  "faceSizeInCm": 122,
                  "arrowCount": 48
                }
              ]
            """
                const val YORK_DISTANCES = """
              "roundDistances": [
                {
                  "distanceNumber": 1,
                  "roundSubTypeId": 1,
                  "distance": 100
                },
                {
                  "distanceNumber": 2,
                  "roundSubTypeId": 1,
                  "distance": 80
                },
                {
                  "distanceNumber": 1,
                  "roundSubTypeId": 2,
                  "distance": 80
                },
                {
                  "distanceNumber": 2,
                  "roundSubTypeId": 2,
                  "distance": 60
                },
                {
                  "distanceNumber": 1,
                  "roundSubTypeId": 3,
                  "distance": 60
                },
                {
                  "distanceNumber": 2,
                  "roundSubTypeId": 3,
                  "distance": 50
                },
                {
                  "distanceNumber": 1,
                  "roundSubTypeId": 4,
                  "distance": 30
                },
                {
                  "distanceNumber": 2,
                  "roundSubTypeId": 4,
                  "distance": 20
                }
              ]
            """
                const val ST_GEORGE = """
            {
              "roundName": "St. George",
              "outdoor": true,
              "isMetric": true,
              "fiveArrowEnd": true,
              "permittedFaces": [
                "NO_TRIPLE",
                "FIVE_CENTRE"
              ],
              "roundSubTypes": [
                {
                  "roundSubTypeId": 1,
                  "subTypeName": "St. George",
                  "gentsUnder": null,
                  "ladiesUnder": null
                },
                {
                  "roundSubTypeId": 2,
                  "subTypeName": "Albion",
                  "gentsUnder": null,
                  "ladiesUnder": null
                }
              ],
              "roundArrowCounts": [
                {
                  "distanceNumber": 1,
                  "faceSizeInCm": 122,
                  "arrowCount": 36
                },
                {
                  "distanceNumber": 2,
                  "faceSizeInCm": 122,
                  "arrowCount": 36
                }
              ],
              "roundDistances": [
                {
                  "distanceNumber": 1,
                  "roundSubTypeId": 1,
                  "distance": 100
                },
                {
                  "distanceNumber": 2,
                  "roundSubTypeId": 1,
                  "distance": 80
                },
                {
                  "distanceNumber": 1,
                  "roundSubTypeId": 2,
                  "distance": 80
                },
                {
                  "distanceNumber": 2,
                  "roundSubTypeId": 2,
                  "distance": 60
                }
              ]
            }
            """
            }
        }

        /**
         * Testing that there are no errors when parsing the default rounds resource file
         */
        @Test
        fun defaultRoundsFileTest() {
            val json = File("src/main/res/raw/default_rounds_data.json").readText()
            roundsFromJson(json)
        }

        /**
         * Testing two correct rounds give the correct RoundInfo, which produces correct database objects
         */
        @Test
        fun clean() {
            val json = """
            ${TestData.START}
                {
                    ${TestData.YORK_MAIN},
                    ${TestData.YORK_SUB_TYPES},
                    ${TestData.YORK_ARROW_COUNTS},
                    ${TestData.YORK_DISTANCES}
                },
                ${TestData.ST_GEORGE}
            ${TestData.END}
        """
            val parsedData = roundsFromJson(json)
            assertEquals(2, parsedData.size)

            /*
         * York
         */
            val york = parsedData[0]
            val yorkRound = york.getRound()
            assertEquals("york", yorkRound.name)
            assertEquals("York", yorkRound.displayName)
            assertEquals(true, yorkRound.isOutdoor)
            assertEquals(false, yorkRound.isMetric)
            assertEquals(false, yorkRound.fiveArrowEnd)
            assertEquals(0, yorkRound.permittedFaces.size)

            val yorkSubTypes = york.getRoundSubTypes(1)
            assertEquals(4, yorkSubTypes.size)
            assertEquals(
                    listOf("York", "Hereford (Bristol I)", "Bristol II", "Bristol V"),
                    yorkSubTypes.map { it.name })
            assertEquals(1, yorkSubTypes.filter { it.gents == null }.size)
            assertEquals(1, yorkSubTypes.filter { it.gents == 0 }.size)
            assertEquals(2, yorkSubTypes.filter { it.ladies == null }.size)
            assertEquals(0, yorkSubTypes.filter { it.ladies == 0 }.size)

            val yorkArrowCounts = york.getRoundArrowCounts(1)
            assertEquals(setOf(72, 48), yorkArrowCounts.map { it.arrowCount }.toSet())
            assertEquals(listOf(122.0), yorkArrowCounts.map { it.faceSizeInCm }.distinct())

            val yorkDistances = york.getRoundDistances(1)
            assertEquals(setOf(100, 80, 80, 60, 60, 50, 30, 20), yorkDistances.map { it.distance }.toSet())

            /*
         * St George
         */
            val stGeorge = parsedData[1]
            val stGeorgeRound = stGeorge.getRound()
            assertEquals("stgeorge", stGeorgeRound.name)
            assertEquals("St. George", stGeorgeRound.displayName)
            assertEquals(true, stGeorgeRound.isOutdoor)
            assertEquals(true, stGeorgeRound.isMetric)
            assertEquals(true, stGeorgeRound.fiveArrowEnd)
            assertEquals(2, stGeorgeRound.permittedFaces.size)
            assertEquals(listOf("NO_TRIPLE", "FIVE_CENTRE"), stGeorgeRound.permittedFaces)
            assertEquals(2, stGeorge.getRoundSubTypes(1).size)
            assertEquals(2, stGeorge.getRoundArrowCounts(1).size)
        }

        /**
         * Testing equivalent round names are rejected
         */
        @Test(expected = IllegalArgumentException::class)
        fun duplicateRoundName() {
            val json = """
            ${TestData.START}
                {
                   "roundName": "stgeoRge.",
                   "outdoor": true,
                   "isMetric": false,
                   "fiveArrowEnd": false,
                   "permittedFaces": [],
                    ${TestData.YORK_SUB_TYPES},
                    ${TestData.YORK_ARROW_COUNTS},
                    ${TestData.YORK_DISTANCES}
                },
                ${TestData.ST_GEORGE}
            ${TestData.END}
        """
            roundsFromJson(json)
        }

        @Test(expected = KlaxonException::class)
        fun invalidJson() {
            // Missing { for the round
            val json = """
            ${TestData.START}
                    ${TestData.YORK_MAIN},
                    ${TestData.YORK_SUB_TYPES},
                    ${TestData.YORK_ARROW_COUNTS},
                    ${TestData.YORK_DISTANCES}
                }
            ${TestData.END}
        """
            roundsFromJson(json)
        }

        /**
         * Testing rejection of an Integer where there should be an Array in the JSON
         */
        @Test(expected = ClassCastException::class)
        fun badJsonType_distances() {
            // Round distances is an array
            val json = """
            ${TestData.START}
                {
                    ${TestData.YORK_MAIN},
                    ${TestData.YORK_SUB_TYPES},
                    ${TestData.YORK_ARROW_COUNTS},
                    "roundDistances": 7
                }
            ${TestData.END}
        """
            roundsFromJson(json)
        }

        /**
         * Testing size of distances array is validated
         */
        @Test(expected = IllegalArgumentException::class)
        fun badDistancesSize() {
            val json = """
            ${TestData.START}
                {
                    ${TestData.YORK_MAIN},
                    ${TestData.YORK_SUB_TYPES},
                    ${TestData.YORK_ARROW_COUNTS},
                    "roundDistances": [
                        {
                            "distanceNumber": 1,
                            "roundSubTypeId": 1,
                            "distance": 100
                        }
                    ]  
                }
            ${TestData.END}
        """
            roundsFromJson(json)
        }

        @Test(expected = IllegalArgumentException::class)
        fun duplicateSubTypeName() {
            val json = """
            ${TestData.START}
                {
                    ${TestData.YORK_MAIN},
                    "roundSubTypes": [
                        {
                            "roundSubTypeId": 1,
                            "subTypeName": "York",
                            "gentsUnder": null,
                            "ladiesUnder": null
                        },
                        {
                            "roundSubTypeId": 2,
                            "subTypeName": "york!",
                            "gentsUnder": 18,
                            "ladiesUnder": null
                        },
                        {
                            "roundSubTypeId": 3,
                            "subTypeName": "Bristol II",
                            "gentsUnder": 16,
                            "ladiesUnder": 18
                        },
                        {
                            "roundSubTypeId": 4,
                            "subTypeName": "Bristol V",
                            "gentsUnder": 0,
                            "ladiesUnder": 12
                        }
                    ],
                    ${TestData.YORK_ARROW_COUNTS},
                    ${TestData.YORK_DISTANCES}
                }
            ${TestData.END}
        """
            roundsFromJson(json)
        }

        @Test(expected = IllegalArgumentException::class)
        fun duplicateSubTypeId() {
            val json = """
            ${TestData.START}
                {
                    ${TestData.YORK_MAIN},
                    "roundSubTypes": [
                        {
                            "roundSubTypeId": 1,
                            "subTypeName": "York",
                            "gentsUnder": null,
                            "ladiesUnder": null
                        },
                        {
                            "roundSubTypeId": 1,
                            "subTypeName": "Hereford (Bristol I)",
                            "gentsUnder": 18,
                            "ladiesUnder": null
                        },
                        {
                            "roundSubTypeId": 3,
                            "subTypeName": "Bristol II",
                            "gentsUnder": 16,
                            "ladiesUnder": 18
                        },
                        {
                            "roundSubTypeId": 4,
                            "subTypeName": "Bristol V",
                            "gentsUnder": 0,
                            "ladiesUnder": 12
                        }
                    ],
                    ${TestData.YORK_ARROW_COUNTS},
                    ${TestData.YORK_DISTANCES}
                }
            ${TestData.END}
        """
            roundsFromJson(json)
        }

        @Test(expected = IllegalArgumentException::class)
        fun duplicateDistanceNumber_arrowCounts() {
            val json = """
            ${TestData.START}
                {
                    ${TestData.YORK_MAIN},
                    ${TestData.YORK_SUB_TYPES},
                    "roundArrowCounts": [
                        {
                            "distanceNumber": 1,
                            "faceSizeInCm": 122,
                            "arrowCount": 72
                        },
                        {
                            "distanceNumber": 1,
                            "faceSizeInCm": 122,
                            "arrowCount": 48
                        }
                    ],
                    ${TestData.YORK_DISTANCES}
                }
            ${TestData.END}
        """
            roundsFromJson(json)
        }

        /**
         * Testing rejection of a duplicate of a distanceNumber/roundSubTypeId pair
         */
        @Test(expected = IllegalArgumentException::class)
        fun duplicateKey_distances() {
            val json = """
            ${TestData.START}
                {
                    ${TestData.YORK_MAIN},
                    ${TestData.YORK_SUB_TYPES},
                    ${TestData.YORK_ARROW_COUNTS},
                    "roundDistances": [
                        {
                            "distanceNumber": 1,
                            "roundSubTypeId": 1,
                            "distance": 100
                        },
                        {
                            "distanceNumber": 1,
                            "roundSubTypeId": 1,
                            "distance": 80
                        },
                        {
                            "distanceNumber": 1,
                            "roundSubTypeId": 2,
                            "distance": 80
                        },
                        {
                            "distanceNumber": 2,
                            "roundSubTypeId": 2,
                            "distance": 60
                        },
                        {
                            "distanceNumber": 1,
                            "roundSubTypeId": 3,
                            "distance": 60
                        },
                        {
                            "distanceNumber": 2,
                            "roundSubTypeId": 3,
                            "distance": 50
                        },
                        {
                            "distanceNumber": 1,
                            "roundSubTypeId": 4,
                            "distance": 30
                        },
                        {
                            "distanceNumber": 2,
                            "roundSubTypeId": 4,
                            "distance": 20
                        }
                    ]
                }
            ${TestData.END}
        """
            roundsFromJson(json)
        }

        /**
         * Testing rejection of a later distance entry with a higher actual distance (e.g. first distance is 100 yards,
         *   second distance is 200 yards)
         */
        @Test(expected = IllegalArgumentException::class)
        fun nonDescendingDistances() {
            val json = """
            ${TestData.START}
                {
                    ${TestData.YORK_MAIN},
                    ${TestData.YORK_SUB_TYPES},
                    ${TestData.YORK_ARROW_COUNTS},
                    "roundDistances": [
                        {
                            "distanceNumber": 1,
                            "roundSubTypeId": 1,
                            "distance": 100
                        },
                        {
                            "distanceNumber": 2,
                            "roundSubTypeId": 1,
                            "distance": 80
                        },
                        {
                            "distanceNumber": 1,
                            "roundSubTypeId": 2,
                            "distance": 80
                        },
                        {
                            "distanceNumber": 2,
                            "roundSubTypeId": 2,
                            "distance": 60
                        },
                        {
                            "distanceNumber": 1,
                            "roundSubTypeId": 3,
                            "distance": 60
                        },
                        {
                            "distanceNumber": 2,
                            "roundSubTypeId": 3,
                            "distance": 50
                        },
                        {
                            "distanceNumber": 1,
                            "roundSubTypeId": 4,
                            "distance": 30
                        },
                        {
                            "distanceNumber": 2,
                            "roundSubTypeId": 4,
                            "distance": 120
                        }
                    ]
                }
            ${TestData.END}
        """
            roundsFromJson(json)
        }

        /**
         * One of the keys in roundDistances doesn't match those of of subTypes or arrowCounts
         */
        @Test(expected = IllegalArgumentException::class)
        fun distancesKeysMismatch() {
            val json = """
            ${TestData.START}
                {
                    ${TestData.YORK_MAIN},
                    ${TestData.YORK_SUB_TYPES},
                    ${TestData.YORK_ARROW_COUNTS},
                    "roundDistances": [
                        {
                            "distanceNumber": 17,
                            "roundSubTypeId": 1,
                            "distance": 100
                        },
                        {
                            "distanceNumber": 2,
                            "roundSubTypeId": 17,
                            "distance": 80
                        },
                        {
                            "distanceNumber": 1,
                            "roundSubTypeId": 2,
                            "distance": 80
                        },
                        {
                            "distanceNumber": 2,
                            "roundSubTypeId": 2,
                            "distance": 60
                        },
                        {
                            "distanceNumber": 1,
                            "roundSubTypeId": 3,
                            "distance": 60
                        },
                        {
                            "distanceNumber": 2,
                            "roundSubTypeId": 3,
                            "distance": 50
                        },
                        {
                            "distanceNumber": 1,
                            "roundSubTypeId": 4,
                            "distance": 30
                        },
                        {
                            "distanceNumber": 2,
                            "roundSubTypeId": 4,
                            "distance": 20
                        }
                    ]
                }
            ${TestData.END}
        """
            roundsFromJson(json)
        }
    }

    class CheckDefaultRoundsUnitTests {
        private val defaultRound = DefaultRoundInfo(
                "York", true, true, true, listOf("Boop"),
                listOf(
                        DefaultRoundInfo.RoundInfoSubType(
                                1, "one", null, null
                        ),
                        DefaultRoundInfo.RoundInfoSubType(
                                2, "two", null, null
                        )
                ),
                listOf(
                        DefaultRoundInfo.RoundInfoArrowCount(1, 122.0, 36),
                        DefaultRoundInfo.RoundInfoArrowCount(2, 122.0, 36)
                ),
                listOf(
                        DefaultRoundInfo.RoundInfoDistance(1, 1, 100),
                        DefaultRoundInfo.RoundInfoDistance(1, 2, 80),
                        DefaultRoundInfo.RoundInfoDistance(2, 1, 80),
                        DefaultRoundInfo.RoundInfoDistance(2, 2, 60)
                )
        )

        /**
         * Should return an empty map if no changes need to be made
         */
        @Test
        fun noChange() {
            assert(
                    checkDefaultRounds(
                            listOf(defaultRound), listOf(defaultRound.getRound(1)),
                            defaultRound.getRoundArrowCounts(1),
                            defaultRound.getRoundSubTypes(1), defaultRound.getRoundDistances(1)
                    ).isEmpty()
            )
        }

        /**
         * Test rejection of a database default round with no associated arrow counts (indicates inputs are wrong)
         */
        @Test(expected = IllegalArgumentException::class)
        fun noArrowCountsInput() {
            checkDefaultRounds(
                    listOf(defaultRound), listOf(defaultRound.getRound(1)), listOf(), listOf(),
                    defaultRound.getRoundDistances(1)
            )
        }

        /**
         * Test rejection of a database default round with no associated distances (indicates inputs are wrong)
         */
        @Test(expected = IllegalArgumentException::class)
        fun noDistancesInput() {
            checkDefaultRounds(
                    listOf(defaultRound), listOf(defaultRound.getRound(1)), defaultRound.getRoundArrowCounts(1),
                    listOf(), listOf()
            )
        }

        /*
         * Should correctly detect new, update, and delete items
         */
        @Test
        fun newRound() {
            val response = checkDefaultRounds(
                    listOf(defaultRound), listOf(), listOf(), listOf(), listOf()
            )
            // Round + 2 subtypes + 2 arrow counts + 4 distances
            assertEquals(9, response.size)
            assertEquals(1, response.filter { it.key is Round }.size)
            assertEquals(2, response.filter { it.key is RoundArrowCount }.size)
            assertEquals(2, response.filter { it.key is RoundSubType }.size)
            assertEquals(4, response.filter { it.key is RoundDistance }.size)
            assertEquals(setOf(UpdateType.NEW), response.map { it.value }.toSet())
        }

        @Test
        fun updateRound() {
            val response = checkDefaultRounds(
                    listOf(defaultRound),
                    listOf(Round(1, "york", "York", true, true, listOf("Potato"), isDefaultRound = true)),
                    defaultRound.getRoundArrowCounts(1), defaultRound.getRoundSubTypes(1),
                    defaultRound.getRoundDistances(1)
            )
            assertEquals(1, response.size)
            assert(response.filter { it.key is Round }.isNotEmpty())
            assertEquals(setOf(UpdateType.UPDATE), response.map { it.value }.toSet())
        }

        @Test
        fun deleteRound() {
            val defaultRound2 = DefaultRoundInfo(
                    "St. George", true, true, true, listOf("Boop"),
                    listOf(
                            DefaultRoundInfo.RoundInfoSubType(
                                    1, "one", null, null
                            ),
                            DefaultRoundInfo.RoundInfoSubType(
                                    2, "two", null, null
                            )
                    ),
                    listOf(
                            DefaultRoundInfo.RoundInfoArrowCount(
                                    1, 122.0, 36
                            ),
                            DefaultRoundInfo.RoundInfoArrowCount(
                                    2, 122.0, 36
                            )
                    ),
                    listOf(
                            DefaultRoundInfo.RoundInfoDistance(1, 1, 100),
                            DefaultRoundInfo.RoundInfoDistance(1, 2, 80),
                            DefaultRoundInfo.RoundInfoDistance(2, 1, 80),
                            DefaultRoundInfo.RoundInfoDistance(2, 2, 60)
                    )
            )
            val response = checkDefaultRounds(
                    listOf(defaultRound2),
                    listOf(defaultRound.getRound(1), defaultRound2.getRound(2)),
                    defaultRound.getRoundArrowCounts(1) + defaultRound2.getRoundArrowCounts(2),
                    defaultRound.getRoundSubTypes(1) + defaultRound2.getRoundSubTypes(2),
                    defaultRound.getRoundDistances(1) + defaultRound2.getRoundDistances(2)
            )
            assertEquals(1, response.size)
            assert(response.filter { it.key is Round }.isNotEmpty())
            assertEquals(setOf(UpdateType.DELETE), response.map { it.value }.toSet())
        }

        @Test
        fun newSubType() {
            val subTypes = defaultRound.getRoundSubTypes(1)
            val response = checkDefaultRounds(
                    listOf(defaultRound), listOf(defaultRound.getRound(1)), defaultRound.getRoundArrowCounts(1),
                    subTypes.subList(0, subTypes.size - 1), defaultRound.getRoundDistances(1)
            )
            assertEquals(1, response.size)
            assert(response.filter { it.key is RoundSubType }.isNotEmpty())
            assertEquals(setOf(UpdateType.NEW), response.map { it.value }.toSet())
        }

        @Test
        fun updateSubType() {
            val subTypes = defaultRound.getRoundSubTypes(1)
            val subType = subTypes[0]
            val newSubTypes = subTypes.subList(1, subTypes.size).plus(
                    RoundSubType(subType.roundId, subType.subTypeId, subType.name, 10, 12)
            )

            val response = checkDefaultRounds(
                    listOf(defaultRound), listOf(defaultRound.getRound(1)), defaultRound.getRoundArrowCounts(1),
                    newSubTypes, defaultRound.getRoundDistances(1)
            )
            assertEquals(1, response.size)
            assert(response.filter { it.key is RoundSubType }.isNotEmpty())
            assertEquals(setOf(UpdateType.UPDATE), response.map { it.value }.toSet())
        }

        @Test
        fun deleteSubType() {
            val response = checkDefaultRounds(
                    listOf(defaultRound), listOf(defaultRound.getRound(1)),
                    defaultRound.getRoundArrowCounts(1),
                    defaultRound.getRoundSubTypes(1).plus(RoundSubType(1, 7)),
                    defaultRound.getRoundDistances(1)
            )
            assertEquals(1, response.size)
            assert(response.filter { it.key is RoundSubType }.isNotEmpty())
            assertEquals(setOf(UpdateType.DELETE), response.map { it.value }.toSet())
        }

        @Test
        fun newArrowCount() {
            val arrowCounts = defaultRound.getRoundArrowCounts(1)
            val response = checkDefaultRounds(
                    listOf(defaultRound), listOf(defaultRound.getRound(1)),
                    arrowCounts.subList(0, arrowCounts.size - 1), defaultRound.getRoundSubTypes(1),
                    defaultRound.getRoundDistances(1)
            )
            assertEquals(1, response.size)
            assert(response.filter { it.key is RoundArrowCount }.isNotEmpty())
            assertEquals(setOf(UpdateType.NEW), response.map { it.value }.toSet())
        }

        @Test
        fun updateArrowCount() {
            val arrowCounts = defaultRound.getRoundArrowCounts(1)
            val arrowCount = arrowCounts[0]
            val newArrowCounts = arrowCounts.subList(1, arrowCounts.size).plus(
                    RoundArrowCount(arrowCount.roundId, arrowCount.distanceNumber, arrowCount.faceSizeInCm, 1000)
            )

            val response = checkDefaultRounds(
                    listOf(defaultRound), listOf(defaultRound.getRound(1)), newArrowCounts,
                    defaultRound.getRoundSubTypes(1), defaultRound.getRoundDistances(1)
            )
            assertEquals(1, response.size)
            assert(response.filter { it.key is RoundArrowCount }.isNotEmpty())
            assertEquals(setOf(UpdateType.UPDATE), response.map { it.value }.toSet())
        }

        @Test
        fun deleteArrowCount() {
            val response = checkDefaultRounds(
                    listOf(defaultRound), listOf(defaultRound.getRound(1)),
                    defaultRound.getRoundArrowCounts(1).plus(RoundArrowCount(1, 7, 122.0, 36)),
                    defaultRound.getRoundSubTypes(1),
                    defaultRound.getRoundDistances(1)
            )
            assertEquals(1, response.size)
            assert(response.filter { it.key is RoundArrowCount }.isNotEmpty())
            assertEquals(setOf(UpdateType.DELETE), response.map { it.value }.toSet())
        }

        @Test
        fun newDistance() {
            val distances = defaultRound.getRoundDistances(1)
            val response = checkDefaultRounds(
                    listOf(defaultRound), listOf(defaultRound.getRound(1)), defaultRound.getRoundArrowCounts(1),
                    defaultRound.getRoundSubTypes(1), distances.subList(0, distances.size - 1)
            )
            assertEquals(1, response.size)
            assert(response.filter { it.key is RoundDistance }.isNotEmpty())
            assertEquals(setOf(UpdateType.NEW), response.map { it.value }.toSet())
        }

        @Test
        fun updateDistance() {
            val distances = defaultRound.getRoundDistances(1)
            val distance = distances[0]
            val newDistances = distances.subList(1, distances.size).plus(
                    RoundDistance(distance.roundId, distance.distanceNumber, distance.subTypeId, distance.distance + 10)
            )

            val response = checkDefaultRounds(
                    listOf(defaultRound), listOf(defaultRound.getRound(1)), defaultRound.getRoundArrowCounts(1),
                    defaultRound.getRoundSubTypes(1), newDistances
            )
            assertEquals(1, response.size)
            assert(response.filter { it.key is RoundDistance }.isNotEmpty())
            assertEquals(setOf(UpdateType.UPDATE), response.map { it.value }.toSet())
        }

        @Test
        fun deleteDistance() {
            val response = checkDefaultRounds(
                    listOf(defaultRound), listOf(defaultRound.getRound(1)),
                    defaultRound.getRoundArrowCounts(1), defaultRound.getRoundSubTypes(1),
                    defaultRound.getRoundDistances(1).plus(RoundDistance(1, 7, 1, 50))
            )
            assertEquals(1, response.size)
            assert(response.filter { it.key is RoundDistance }.isNotEmpty())
            assertEquals(setOf(UpdateType.DELETE), response.map { it.value }.toSet())
        }
    }
}