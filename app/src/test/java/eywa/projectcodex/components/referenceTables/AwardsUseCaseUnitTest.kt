package eywa.projectcodex.components.referenceTables

import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.components.referenceTables.awards.AwardsUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class AwardsUseCaseUnitTest {
    @Test
    fun test() {
        var entries = AwardsUseCase.getAgbAwards(ClassificationBow.RECURVE, null)
        assertEquals(6, entries.size)

        entries = AwardsUseCase.getAgbAwards(ClassificationBow.LONGBOW, null)
        assertEquals(6, entries.size)

        entries = AwardsUseCase.getAgbAwards(ClassificationBow.BAREBOW, null)
        assertEquals(7, entries.size)

        entries = AwardsUseCase.getAgbAwards(ClassificationBow.COMPOUND, null)
        assertEquals(7, entries.size)
    }
}
