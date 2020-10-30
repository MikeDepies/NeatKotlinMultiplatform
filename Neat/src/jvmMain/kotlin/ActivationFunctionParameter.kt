import kotlinx.serialization.Serializable

@Serializable
data class ActivationFunctionParameter(
    val name: String,
    val displayName: String,
    val description: String,
    val enabled: Boolean
)