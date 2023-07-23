package eywa.projectcodex.common

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogState
import eywa.projectcodex.database.RoundFace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SelectRoundFaceDialogStateUnitTest {
    @Test
    fun testInit() {
        SelectRoundFaceDialogState()

        SelectRoundFaceDialogState(
                round = RoundPreviewHelper.indoorMetricRoundData.round,
                distances = listOf(10),
        )
        SelectRoundFaceDialogState(
                round = RoundPreviewHelper.indoorMetricRoundData.round,
                distances = listOf(10, 20),
        )

        assertThrows(IllegalStateException::class.java) {
            SelectRoundFaceDialogState(round = RoundPreviewHelper.indoorMetricRoundData.round)
        }
        assertThrows(IllegalStateException::class.java) {
            SelectRoundFaceDialogState(
                    round = RoundPreviewHelper.indoorMetricRoundData.round,
                    distances = listOf(),
            )
        }
        assertThrows(IllegalStateException::class.java) {
            SelectRoundFaceDialogState(distances = listOf())
        }
        assertThrows(IllegalStateException::class.java) {
            SelectRoundFaceDialogState(distances = listOf(10))
        }

        SelectRoundFaceDialogState(selectedFaces = listOf())
        SelectRoundFaceDialogState(selectedFaces = listOf(RoundFace.TRIPLE))

        assertThrows(IllegalStateException::class.java) {
            SelectRoundFaceDialogState(
                    selectedFaces = listOf(RoundFace.TRIPLE, RoundFace.FULL),
            )
        }

        SelectRoundFaceDialogState(
                round = RoundPreviewHelper.indoorMetricRoundData.round,
                distances = listOf(20, 10),
                selectedFaces = listOf(RoundFace.TRIPLE, RoundFace.FULL),
        )

        assertThrows(IllegalStateException::class.java) {
            SelectRoundFaceDialogState(
                    round = RoundPreviewHelper.indoorMetricRoundData.round,
                    distances = listOf(20),
                    selectedFaces = listOf(RoundFace.TRIPLE, RoundFace.FULL),
            )
        }
    }

    @Test
    fun testFinalFaces() {
        // Null
        assertEquals(
                null,
                SelectRoundFaceDialogState().finalFaces,
        )
        assertEquals(
                null,
                SelectRoundFaceDialogState(selectedFaces = listOf(RoundFace.FULL)).finalFaces,
        )
        assertEquals(
                null,
                SelectRoundFaceDialogState(
                        round = RoundPreviewHelper.indoorMetricRoundData.round,
                        distances = listOf(20, 10),
                        selectedFaces = listOf(RoundFace.FULL, RoundFace.FULL),
                ).finalFaces,
        )

        // Same
        assertEquals(
                listOf(RoundFace.TRIPLE),
                SelectRoundFaceDialogState(
                        round = RoundPreviewHelper.indoorMetricRoundData.round,
                        distances = listOf(20, 10),
                        selectedFaces = listOf(RoundFace.TRIPLE, RoundFace.TRIPLE),
                ).finalFaces,
        )

        // Different
        assertEquals(
                listOf(RoundFace.FULL, RoundFace.TRIPLE),
                SelectRoundFaceDialogState(
                        round = RoundPreviewHelper.indoorMetricRoundData.round,
                        distances = listOf(20, 10),
                        selectedFaces = listOf(RoundFace.FULL, RoundFace.TRIPLE),
                ).finalFaces,
        )
    }

    @Test
    fun testFirstFaceAsSingleton() {
        assertEquals(
                null,
                SelectRoundFaceDialogState().firstFaceAsSingleton,
        )
        assertEquals(
                null,
                SelectRoundFaceDialogState(selectedFaces = listOf()).firstFaceAsSingleton,
        )
        assertEquals(
                listOf(RoundFace.TRIPLE),
                SelectRoundFaceDialogState(selectedFaces = listOf(RoundFace.TRIPLE)).firstFaceAsSingleton,
        )
        assertEquals(
                listOf(RoundFace.TRIPLE),
                SelectRoundFaceDialogState(
                        round = RoundPreviewHelper.indoorMetricRoundData.round,
                        distances = listOf(20, 10),
                        selectedFaces = listOf(RoundFace.TRIPLE, RoundFace.HALF),
                ).firstFaceAsSingleton,
        )
    }
}
