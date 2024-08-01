package eywa.projectcodex.common.sharedUi.previewHelpers

import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType

object RoundPreviewHelper {
    val outdoorImperialRoundData = FullRoundInfo(
            round = Round(
                    roundId = 1,
                    name = "york",
                    displayName = "York",
                    isOutdoor = true,
                    isMetric = false,
            ),
            roundSubTypes = listOf(
                    RoundSubType(
                            roundId = 1,
                            subTypeId = 1,
                            name = "York",
                    ),
                    RoundSubType(
                            roundId = 1,
                            subTypeId = 2,
                            name = "Hereford",
                    ),
            ),
            roundArrowCounts = listOf(
                    RoundArrowCount(
                            roundId = 1,
                            distanceNumber = 1,
                            faceSizeInCm = 120.0,
                            arrowCount = 36,
                    ),
                    RoundArrowCount(
                            roundId = 1,
                            distanceNumber = 2,
                            faceSizeInCm = 120.0,
                            arrowCount = 24,
                    ),
            ),
            roundDistances = listOf(
                    RoundDistance(
                            roundId = 1,
                            distanceNumber = 1,
                            subTypeId = 1,
                            distance = 100,
                    ),
                    RoundDistance(
                            roundId = 1,
                            distanceNumber = 2,
                            subTypeId = 1,
                            distance = 80,
                    ),
                    RoundDistance(
                            roundId = 1,
                            distanceNumber = 1,
                            subTypeId = 2,
                            distance = 80,
                    ),
                    RoundDistance(
                            roundId = 1,
                            distanceNumber = 2,
                            subTypeId = 2,
                            distance = 60,
                    ),
            ),
    )

    val indoorMetricRoundData = FullRoundInfo(
            round = Round(
                    roundId = 2,
                    name = "wa",
                    displayName = "WA",
                    isOutdoor = false,
                    isMetric = true,
            ),
            roundSubTypes = listOf(
                    RoundSubType(
                            roundId = 2,
                            subTypeId = 1,
                            name = "WA 18m",
                    ),
                    RoundSubType(
                            roundId = 2,
                            subTypeId = 2,
                            name = "WA 25m",
                    ),
            ),
            roundArrowCounts = listOf(
                    RoundArrowCount(
                            roundId = 2,
                            distanceNumber = 1,
                            faceSizeInCm = 60.0,
                            arrowCount = 60,
                    ),
            ),
            roundDistances = listOf(
                    RoundDistance(
                            roundId = 2,
                            distanceNumber = 1,
                            subTypeId = 1,
                            distance = 18,
                    ),
                    RoundDistance(
                            roundId = 2,
                            distanceNumber = 1,
                            subTypeId = 2,
                            distance = 25,
                    ),
            ),
    )

    val singleSubtypeRoundData = FullRoundInfo(
            round = Round(
                    roundId = 3,
                    name = "portsmouth",
                    displayName = "Portsmouth",
                    isOutdoor = false,
                    isMetric = false,
            ),

            roundSubTypes = listOf(
                    RoundSubType(
                            roundId = 3,
                            subTypeId = 1,
                    ),
            ),
            roundArrowCounts = listOf(
                    RoundArrowCount(
                            roundId = 3,
                            distanceNumber = 1,
                            faceSizeInCm = 80.0,
                            arrowCount = 60,
                    ),
            ),
            roundDistances = listOf(
                    RoundDistance(
                            roundId = 3,
                            distanceNumber = 1,
                            subTypeId = 1,
                            distance = 20,
                    ),
            ),
    )

        val yorkRoundData = FullRoundInfo(
                round = Round(
                        roundId = 4,
                        name = "york",
                        displayName = "York",
                        isOutdoor = true,
                        isMetric = false,
                        defaultRoundId = 1,
                ),
                roundSubTypes = listOf(
                        RoundSubType(
                                roundId = 4,
                                subTypeId = 1,
                                name = "York",
                        ),
                        RoundSubType(
                                roundId = 4,
                                subTypeId = 2,
                                name = "Hereford",
                        ),
                ),
                roundArrowCounts = listOf(
                        RoundArrowCount(
                                roundId = 4,
                                distanceNumber = 1,
                                faceSizeInCm = 122.0,
                                arrowCount = 72,
                        ),
                        RoundArrowCount(
                                roundId = 4,
                                distanceNumber = 2,
                                faceSizeInCm = 122.0,
                                arrowCount = 48,
                        ),
                        RoundArrowCount(
                                roundId = 4,
                                distanceNumber = 3,
                                faceSizeInCm = 122.0,
                                arrowCount = 24,
                        ),
                ),
                roundDistances = listOf(
                        RoundDistance(
                                roundId = 4,
                                distanceNumber = 1,
                                subTypeId = 1,
                                distance = 100,
                        ),
                        RoundDistance(
                                roundId = 4,
                                distanceNumber = 2,
                                subTypeId = 1,
                                distance = 80,
                        ),
                        RoundDistance(
                                roundId = 4,
                                distanceNumber = 3,
                                subTypeId = 1,
                                distance = 60,
                        ),
                        RoundDistance(
                                roundId = 4,
                                distanceNumber = 1,
                                subTypeId = 2,
                                distance = 80,
                        ),
                        RoundDistance(
                                roundId = 4,
                                distanceNumber = 2,
                                subTypeId = 2,
                                distance = 60,
                        ),
                        RoundDistance(
                                roundId = 4,
                                distanceNumber = 3,
                                subTypeId = 2,
                                distance = 50,
                        ),
                ),
        )
        val wa25RoundData = FullRoundInfo(
                round = Round(
                        roundId = 5,
                        name = "wa25",
                        displayName = "WA 25",
                        isOutdoor = false,
                        isMetric = true,
                        defaultRoundId = 25,
                ),
                roundArrowCounts = listOf(
                        RoundArrowCount(
                                roundId = 5,
                                distanceNumber = 1,
                                faceSizeInCm = 60.0,
                                arrowCount = 60,
                        ),
                ),
                roundDistances = listOf(
                        RoundDistance(
                                roundId = 5,
                                distanceNumber = 1,
                                subTypeId = 1,
                                distance = 25,
                        ),
                ),
        )
        val wa1440RoundData = FullRoundInfo(
                round = Round(
                        roundId = 6,
                        name = "wa1440",
                        displayName = "WA 1440",
                        isOutdoor = true,
                        isMetric = true,
                        defaultRoundId = 8,
                ),
                roundSubTypes = listOf(
                        RoundSubType(
                                roundId = 6,
                                subTypeId = 1,
                                name = "Gents",
                        ),
                        RoundSubType(
                                roundId = 6,
                                subTypeId = 2,
                                name = "Ladies / Metric I",
                        ),
                        RoundSubType(
                                roundId = 6,
                                subTypeId = 3,
                                name = "Metric II",
                        ),
                        RoundSubType(
                                roundId = 6,
                                subTypeId = 4,
                                name = "Metric III",
                        ),
                        RoundSubType(
                                roundId = 6,
                                subTypeId = 5,
                                name = "Metric IV",
                        ),
                        RoundSubType(
                                roundId = 6,
                                subTypeId = 6,
                                name = "Metric V",
                        ),
                ),
                roundArrowCounts = listOf(
                        RoundArrowCount(
                                roundId = 6,
                                distanceNumber = 1,
                                faceSizeInCm = 122.0,
                                arrowCount = 36,
                        ),
                        RoundArrowCount(
                                roundId = 6,
                                distanceNumber = 2,
                                faceSizeInCm = 122.0,
                                arrowCount = 36,
                        ),
                        RoundArrowCount(
                                roundId = 6,
                                distanceNumber = 3,
                                faceSizeInCm = 80.0,
                                arrowCount = 36,
                        ),
                        RoundArrowCount(
                                roundId = 6,
                                distanceNumber = 4,
                                faceSizeInCm = 80.0,
                                arrowCount = 36,
                        ),
                ),
                roundDistances = listOf(
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 1,
                                subTypeId = 1,
                                distance = 90,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 2,
                                subTypeId = 1,
                                distance = 70,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 3,
                                subTypeId = 1,
                                distance = 50,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 4,
                                subTypeId = 1,
                                distance = 30,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 1,
                                subTypeId = 2,
                                distance = 70,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 2,
                                subTypeId = 2,
                                distance = 60,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 3,
                                subTypeId = 2,
                                distance = 50,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 4,
                                subTypeId = 2,
                                distance = 30,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 1,
                                subTypeId = 3,
                                distance = 60,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 2,
                                subTypeId = 3,
                                distance = 50,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 3,
                                subTypeId = 3,
                                distance = 40,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 4,
                                subTypeId = 3,
                                distance = 30,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 1,
                                subTypeId = 4,
                                distance = 50,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 2,
                                subTypeId = 4,
                                distance = 40,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 3,
                                subTypeId = 4,
                                distance = 30,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 4,
                                subTypeId = 4,
                                distance = 20,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 1,
                                subTypeId = 5,
                                distance = 40,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 2,
                                subTypeId = 5,
                                distance = 30,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 3,
                                subTypeId = 5,
                                distance = 20,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 4,
                                subTypeId = 5,
                                distance = 10,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 1,
                                subTypeId = 6,
                                distance = 30,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 2,
                                subTypeId = 6,
                                distance = 20,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 3,
                                subTypeId = 6,
                                distance = 15,
                        ),
                        RoundDistance(
                                roundId = 6,
                                distanceNumber = 4,
                                subTypeId = 6,
                                distance = 10,
                        ),
                ),
        )

        val wa70RoundData = FullRoundInfo(
                round = Round(
                        roundId = 7,
                        name = "wa70",
                        displayName = "WA 70",
                        isOutdoor = true,
                        isMetric = true,
                        defaultRoundId = 13,
                ),
                roundArrowCounts = listOf(
                        RoundArrowCount(
                                roundId = 7,
                                distanceNumber = 1,
                                faceSizeInCm = 122.0,
                                arrowCount = 72,
                        ),
                ),
                roundDistances = listOf(
                        RoundDistance(
                                roundId = 7,
                                distanceNumber = 1,
                                subTypeId = 1,
                                distance = 70,
                        ),
                ),
        )
}
