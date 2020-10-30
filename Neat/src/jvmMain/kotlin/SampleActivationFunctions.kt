import neat.Activation
import neat.ActivationGene
import neat.toMap

fun sampleActivationFunctions(): Map<String, ActivationGene> {
    return listOf(
        Activation.identity, Activation.sigmoidal
    ).toMap { it.name }
}