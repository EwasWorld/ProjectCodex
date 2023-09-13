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
}
