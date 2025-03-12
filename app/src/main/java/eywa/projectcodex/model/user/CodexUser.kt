package eywa.projectcodex.model.user

data class CodexUser(
        private val capabilities: List<Capability> = emptyList(),
) {
    fun hasCapability(capability: Capability) = capabilities.contains(capability)
}
