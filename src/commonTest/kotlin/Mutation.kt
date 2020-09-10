import kotlin.math.*

class MutationTest {

}

private const val standardWeightPerturbationRange = .02f
typealias Mutation = NeatExperiment.(NeatMutator) -> Unit
typealias MutationRoll = NeatExperiment.() -> Boolean

data class MutationEntry(val roll: MutationRoll, val mutation: Mutation)

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

/**
 * As found in Stanely NEAT paper.
 */

