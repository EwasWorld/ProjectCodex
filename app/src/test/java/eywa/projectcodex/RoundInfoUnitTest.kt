package eywa.projectcodex

import org.junit.Assert.assertEquals
import org.junit.Test

class RoundInfoUnitTest {
    private val testData = """{
  "rounds": [
    {
      "roundName": "York",
      "outdoor": true,
      "isMetric": false,
      "fiveArrowEnd": false,
      "permittedFaces": [],
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
          "roundSubTypeId": 6,
          "subTypeName": "Bristol V",
          "gentsUnder": 0,
          "ladiesUnder": 12
        }
      ],
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
      ],
      "roundSubTypeCounts": [
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
          "roundSubTypeId": 6,
          "distance": 30
        },
        {
          "distanceNumber": 2,
          "roundSubTypeId": 6,
          "distance": 20
        }
      ]
    },
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
      "roundSubTypeCounts": [
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
  ]
}
    """

    @Test
    fun parseDefaultRounds() {
        val parsedData = fromJson(testData)

        assertEquals(2, parsedData.size)

        /*
         * York
         */
        val york = parsedData[0]
        assertEquals(true, york.isOutdoor)
        assertEquals(false, york.isMetric)
        assertEquals(false, york.fiveArrowEnd)
        assertEquals("York", york.roundName)
        assertEquals(0, york.permittedFaces.size)

        assertEquals(4, york.roundSubTypes.size)
        assertEquals(
                listOf("York", "Hereford (Bristol I)", "Bristol II", "Bristol V"),
                york.roundSubTypes.map { it.subTypeName })
        assertEquals(1, york.roundSubTypes.filter { it.gentsUnder == null }.size)
        assertEquals(1, york.roundSubTypes.filter { it.gentsUnder == 0 }.size)
        assertEquals(2, york.roundSubTypes.filter { it.ladiesUnder == null }.size)
        assertEquals(0, york.roundSubTypes.filter { it.ladiesUnder == 0 }.size)

        assertEquals(2, york.roundArrowCount.size)
        checkDistanceInfoMatches(york.roundSubTypes, york.roundArrowCount, york.roundSubTypeCount)

        /*
         * St George
         */
        val stGeorge = parsedData[1]
        assertEquals(true, stGeorge.isOutdoor)
        assertEquals(true, stGeorge.isMetric)
        assertEquals(true, stGeorge.fiveArrowEnd)
        assertEquals("St. George", stGeorge.roundName)
        assertEquals(2, stGeorge.permittedFaces.size)
        assertEquals(listOf("NO_TRIPLE", "FIVE_CENTRE"), stGeorge.permittedFaces)
        assertEquals(2, stGeorge.roundSubTypes.size)
        assertEquals(2, stGeorge.roundArrowCount.size)
        checkDistanceInfoMatches(stGeorge.roundSubTypes, stGeorge.roundArrowCount, stGeorge.roundSubTypeCount)
    }

    private fun checkDistanceInfoMatches(
            subTypes: List<RoundInfo.RoundInfoSubType>,
            arrowCounts: List<RoundInfo.RoundInfoArrowCount>,
            distances: List<RoundInfo.RoundInfoSubTypeCount>
    ) {
        assertEquals(subTypes.size, subTypes.distinctBy { it.id }.size)
        assertEquals(arrowCounts.size, arrowCounts.distinctBy { it.distanceNumber }.size)
        assertEquals(subTypes.size * arrowCounts.size, distances.size)

        for (length in subTypes) {
            val lengthDistances = distances.filter { distance -> distance.roundSubTypeId == length.id }
            // Every distance for a length type must be unique: i.e. York cannot have 80 and 80
            assertEquals(lengthDistances.size, lengthDistances.distinctBy { it.distance }.size)
            assertEquals(arrowCounts.map { it.distanceNumber }, lengthDistances.map { it.distanceNumber })
        }
    }
}