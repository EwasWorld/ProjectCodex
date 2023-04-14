package eywa.projectcodex.database

import eywa.projectcodex.database.archerRound.ArcherRoundsFilter
import eywa.projectcodex.database.archerRound.ArcherRoundsFilter.*
import org.junit.Assert.*
import org.junit.Test

class FiltersTest {
    @Test
    fun testPlusMinus() {
        var filters = Filters<ArcherRoundsFilter>()

        filters = filters.plus(PersonalBests)
        assertEquals(Filters<ArcherRoundsFilter>(listOf(PersonalBests)), filters)
        filters = filters.plus(PersonalBests)
        assertEquals(Filters<ArcherRoundsFilter>(listOf(PersonalBests)), filters)

        filters = filters.plus(Round(1, 1))
        assertEquals(Filters(listOf(PersonalBests, Round(1, 1))), filters)
        filters = filters.plus(Round(2, 2))
        assertEquals(Filters(listOf(PersonalBests, Round(2, 2))), filters)

        filters = filters.minus<Round>()
        assertEquals(Filters<ArcherRoundsFilter>(listOf(PersonalBests)), filters)
        filters = filters.minus<Round>()
        assertEquals(Filters<ArcherRoundsFilter>(listOf(PersonalBests)), filters)
    }

    @Test
    fun testContains() {
        val filters = Filters<ArcherRoundsFilter>(listOf(PersonalBests))
        assertTrue(filters.contains<PersonalBests>())
        assertFalse(filters.contains<DateRange>())
    }

    @Test
    fun testGet() {
        var filters = Filters<ArcherRoundsFilter>()
        filters = filters.plus(Round(1, 1))
        assertEquals(Filters<ArcherRoundsFilter>(listOf(Round(1, 1))), filters)
        assertEquals(Round(1, 1), filters.get<Round>())

        filters = filters.plus(Round(2, 2))
        assertEquals(Filters<ArcherRoundsFilter>(listOf(Round(2, 2))), filters)
        assertEquals(Round(2, 2), filters.get<Round>())
    }
}
