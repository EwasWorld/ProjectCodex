package eywa.projectcodex.components.sightMarks.menu


sealed class SightMarksMenuIntent {
    object FlipDiagram : SightMarksMenuIntent()
    object ArchiveAll : SightMarksMenuIntent()
}
