package eywa.projectcodex.components.sightMarks.menu


sealed class SightMarksMenuIntent {
    @Deprecated(message = "Temp, to remove")
    object SwitchDataset : SightMarksMenuIntent()
    object FlipDiagram : SightMarksMenuIntent()
    object ArchiveAll : SightMarksMenuIntent()
}
