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

typealias ConnectionMutation = ConnectionGene.() -> Unit

fun NeatExperiment.perturbConnectionWeight(range: Float = standardWeightPerturbationRange): ConnectionMutation {
    if (range < 0) error("range [$range] must be greater than 0")
    return {
        val perturbation = weightPerturbation(range)
        weight += perturbation
    }
}

fun NeatExperiment.assignConnectionRandomWeight(): ConnectionMutation = { weight = randomWeight(random) }

inline fun NeatExperiment.ifElseConnectionMutation(
    crossinline mutationRoll: MutationRoll,
    crossinline onRollSuccess: ConnectionMutation,
    crossinline onRollFailure: ConnectionMutation
): ConnectionMutation = {
    if (mutationRoll()) {
        onRollSuccess()
    } else {
        onRollFailure()
    }
}

/**
 * A configuration found on the web.
 * https://github.com/GabrielTavernini/NeatJS/blob/master/src/connection.js#L12
 */
val NeatExperiment.mutateConnectionWeight
    get() = ifElseConnectionMutation(
        rollFrom(.05f),
        assignConnectionRandomWeight(),
        perturbConnectionWeight()
    )

//fun ConnectionMutation.toMutation() : Mutation {
//    this()
//    return {
//        (it) }
//}

fun uniformWeightPerturbation(connectionMutation: ConnectionMutation): Mutation = { neatMutator ->
    neatMutator.connections.forEach { connection ->
        connectionMutation(connection)
    }
}


fun NeatExperiment.weightPerturbation(range: Float) = (random.nextFloat() * (range * 2)) - range


fun mutateNodeActivationFunction(activationFunctions: List<ActivationFunction>): Mutation = { neatMutator ->
    val nodeGene = neatMutator.nodes.filter { it.nodeType != NodeType.Input }.random(random)
    nodeGene.activationFunction = (activationFunctions - nodeGene.activationFunction).random(random)
}

fun mutatePerturbBiasConnections(biasNode: Int = 0, range: Float = standardWeightPerturbationRange): Mutation =
    { neatMutator ->
        val node = neatMutator.inputNodes[biasNode]
        neatMutator.connectionsFrom(node).forEach { connectionGene ->
            val weightPerturbation = weightPerturbation(range)
            connectionGene.weight += weightPerturbation
        }
    }