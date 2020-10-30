import kotlinx.serialization.Serializable

@Serializable
data class XorExperimentParameters(
    val generations: Int,
    val populationSize: Int,
    val sharingThreshold: Float,
    val mateChance: Float,
    val survivalThreshold: Float,
    val activationFunctions: List<ActivationFunctionParameter>,
    val mutations: List<MutationParameter>
)