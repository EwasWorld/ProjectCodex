package eywa.projectcodex

import org.junit.Assert.assertEquals
import org.junit.Test

class RoundInfoUnitTest {
    private val testData = """{
  "rounds": [
    {
      "roundId": 5,
      "outdoor": true,
      "isMetric": false,
      "fiveArrowEnd": false,
      "roundName": "York",
      "permittedFaces": [],
      "roundLengths": [
        {
          "roundLengthId": 1,
          "lengthName": "York",
          "gentsUnder": null,
          "ladiesUnder": null
        },
        {
          "roundLengthId": 2,
          "lengthName": "Hereford (Bristol I)",
          "gentsUnder": 18,
          "ladiesUnder": null
        },
        {
          "roundLengthId": 3,
          "lengthName": "Bristol II",
          "gentsUnder": 16,
          "ladiesUnder": 18
        },
        {
          "roundLengthId": 6,
          "lengthName": "Bristol V",
          "gentsUnder": 0,
          "ladiesUnder": 12
        }
      ],
      "roundProgression": [
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
      "roundDistances": [
        {
          "distanceNumber": 1,
          "roundLengthId": 1,
          "distance": 100
        },
        {
          "distanceNumber": 2,
          "roundLengthId": 1,
          "distance": 80
        },
        {
          "distanceNumber": 1,
          "roundLengthId": 2,
          "distance": 80
        },
        {
          "distanceNumber": 2,
          "roundLengthId": 2,
          "distance": 60
        },
        {
          "distanceNumber": 1,
          "roundLengthId": 3,
          "distance": 60
        },
        {
          "distanceNumber": 2,
          "roundLengthId": 3,
          "distance": 50
        },
        {
          "distanceNumber": 1,
          "roundLengthId": 6,
          "distance": 30
        },
        {
          "distanceNumber": 2,
          "roundLengthId": 6,
          "distance": 20
        }
      ]
    },
    {
      "roundId": 6,
      "outdoor": true,
      "isMetric": true,
      "fiveArrowEnd": true,
      "roundName": "St. George",
      "permittedFaces": [
        "NO_TRIPLE",
        "FIVE_CENTRE"
      ],
      "roundLengths": [
        {
          "roundLengthId": 1,
          "lengthName": "St. George",
          "gentsUnder": null,
          "ladiesUnder": null
        },
        {
          "roundLengthId": 2,
          "lengthName": "Albion",
          "gentsUnder": null,
          "ladiesUnder": null
        }
      ],
      "roundProgression": [
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
          "roundLengthId": 1,
          "distance": 100
        },
        {
          "distanceNumber": 2,
          "roundLengthId": 1,
          "distance": 80
        },
        {
          "distanceNumber": 1,
          "roundLengthId": 2,
          "distance": 80
        },
        {
          "distanceNumber": 2,
          "roundLengthId": 2,
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
        assertEquals(5, york.id)
        assertEquals(true, york.isOutdoor)
        assertEquals(false, york.isMetric)
        assertEquals(false, york.fiveArrowEnd)
        assertEquals("York", york.roundName)
        assertEquals(0, york.permittedFaces.size)

        assertEquals(4, york.roundLengths.size)
        assertEquals(
                listOf("York", "Hereford (Bristol I)", "Bristol II", "Bristol V"),
                york.roundLengths.map { it.lengthName })
        assertEquals(1, york.roundLengths.filter { it.gentsUnder == null }.size)
        assertEquals(1, york.roundLengths.filter { it.gentsUnder == 0 }.size)
        assertEquals(2, york.roundLengths.filter { it.ladiesUnder == null }.size)
        assertEquals(0, york.roundLengths.filter { it.ladiesUnder == 0 }.size)

        assertEquals(2, york.roundProgression.size)
        checkDistanceInfoMatches(york.roundLengths, york.roundProgression, york.roundDistances)

        /*
         * St George
         */
        val stGeorge = parsedData[1]
        assertEquals(6, stGeorge.id)
        assertEquals(true, stGeorge.isOutdoor)
        assertEquals(true, stGeorge.isMetric)
        assertEquals(true, stGeorge.fiveArrowEnd)
        assertEquals("St. George", stGeorge.roundName)
        assertEquals(2, stGeorge.permittedFaces.size)
        assertEquals(listOf("NO_TRIPLE", "FIVE_CENTRE"), stGeorge.permittedFaces)
        assertEquals(2, stGeorge.roundLengths.size)
        assertEquals(2, stGeorge.roundProgression.size)
        checkDistanceInfoMatches(stGeorge.roundLengths, stGeorge.roundProgression, stGeorge.roundDistances)
    }

    private fun checkDistanceInfoMatches(
            lengths: List<RoundInfo.RoundInfoLength>,
            progressions: List<RoundInfo.RoundInfoProgression>,
            distances: List<RoundInfo.RoundInfoDistance>
    ) {
        assertEquals(lengths.size, lengths.distinctBy { it.id }.size)
        assertEquals(progressions.size, progressions.distinctBy { it.distanceNumber }.size)
        assertEquals(lengths.size * progressions.size, distances.size)

        for (length in lengths) {
            val lengthDistances = distances.filter { distance -> distance.roundLengthId == length.id }
            // Every distance for a length type must be unique: i.e. York cannot have 80 and 80
            assertEquals(lengthDistances.size, lengthDistances.distinctBy { it.distance }.size)
            assertEquals(progressions.map { it.distanceNumber }, lengthDistances.map { it.distanceNumber })
        }
    }
}