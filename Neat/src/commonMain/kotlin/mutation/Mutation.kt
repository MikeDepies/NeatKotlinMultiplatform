package mutation

import ActivationFunction
import NeatExperiment
import NeatMutator
import NodeType

const val standardWeightPerturbationRange = .02f

/**
 * A mutation.Mutation that can be applied to a mutator in the context of a given experiment.
 */
typealias Mutation = NeatExperiment.(NeatMutator) -> Unit

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


val mutateConnections: Mutation = { neatMutator ->
    neatMutator.connections.forEach { connectionGene ->
        mutateConnectionWeight(connectionGene)
    }
}

val mutateAddConnection: Mutation = { mutateAddConnection(it) }
val mutateDisableConnection: Mutation = { neatMutator ->
    val activeConnections = neatMutator.connections.filter { it.enabled }
    if (activeConnections.isNotEmpty()) {
        val randomActiveConnection = activeConnections.random(random)
        randomActiveConnection.enabled = false
    }
}
val mutateToggleConnection: Mutation = { neatMutator ->
    with(neatMutator.connections.random(random)) {
        enabled = !enabled
    }
}
val mutateAddNode: Mutation = { mutateAddNode(it) }