import kotlinx.serialization.Serializable

@Serializable
data class MutationParameter(
    val name: String,
    val displayName: String,
    val description: String,
    val percentChance: Float,
    val enabled: Boolean
)