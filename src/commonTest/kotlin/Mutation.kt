import kotlin.math.*
import kotlin.test.*

class MutationTest {
    @Test
    fun `uniformally perturb weights`() {
        
    }
}

private const val standardWeightPerturbationRange = .02f

/**
 * A Mutation that can be applied to a mutator in the context of a given experiment.
 */
typealias Mutation = NeatExperiment.(NeatMutator) -> Unit
/**
 * Rolls (as in probability dice sense) to see if a mutation can occur.
 */
typealias MutationRoll = NeatExperiment.() -> Boolean

/**
 * Pairing data structure between mutation condition trigger and the associated mutation.
 */
data class MutationEntry(val roll: MutationRoll, val mutation: Mutation)

/**
 * Helper function to generate a MutationRoll based on simple chance by rolling a number between 0 and 1.
 * If the roll is less than the provided chance, the event returns true. False otherwise.
 */
fun rollFrom(chance: Float): MutationRoll = {
    (random.nextFloat() <= chance)
}

fun uniformWeightPerturbation(range: Float = standardWeightPerturbationRange): Mutation = { neatMutator ->
    if (range < 0) error("range [$range] must be greater than 0")
    neatMutator.connections.forEach { connection ->
        val perturbation = weightPerturbation(range)
        connection.weight += perturbation
    }
}

private fun NeatExperiment.weightPerturbation(range: Float) = (random.nextFloat() * (range * 2)) - range


fun mutateNodeActivationFunction(activationFunctions: List<ActivationFunction>): Mutation = { neatMutator ->
    val nodeGene = neatMutator.nodes.filter { it.nodeType != NodeType.Input }.random(random)
    nodeGene.activationFunction = (activationFunctions - nodeGene.activationFunction).random(random)
}

fun mutatePerturbBiasConnections(biasNode: Int = 0, range: Float = standardWeightPerturbationRange): Mutation =
    { neatMutator ->
        val biasNode = neatMutator.inputNodes[biasNode]
        neatMutator.connectionsFrom(biasNode).forEach { connectionGene ->
            val weightPerturbation = weightPerturbation(range)
            connectionGene.weight += weightPerturbation
        }
    }