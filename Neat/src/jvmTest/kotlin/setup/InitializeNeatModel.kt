package setup

import neat.ActivationFunction
import neat.Identity
import neat.model.NeatMutator
import neat.model.NodeGene
import neat.model.NodeType
import neat.connectNodes
import neat.model.neatMutator
import neat.randomWeight
import kotlin.random.*

fun initializeCyclicConnectionsNeatModel(
    random: Random, outputActivation: ActivationFunction = Identity, hiddenActivation: ActivationFunction = Identity
): NeatMutator {
    return neatMutator(1, 1, random, function = outputActivation).apply {
        val nodeGene = NodeGene(2, NodeType.Hidden, hiddenActivation)
        addNode(nodeGene)
        addConnection(
            connectNodes(
                inputNodes[0], nodeGene, randomWeight(random), 2
            )
        )
        addConnection(
            connectNodes(
                nodeGene, outputNodes[0], randomWeight(random), 3
            )
        )
        addConnection(
            connectNodes(
                nodeGene, nodeGene, randomWeight(random), 4
            )
        )
    }
}

fun initializeNeatModel(random: Random): NeatMutator {
    return neatMutator(1, 1, random).apply {
        val node = NodeGene(2, NodeType.Hidden, Identity)
        val node2 = NodeGene(3, NodeType.Hidden, Identity)
        addNode(node)
        addNode(node2)
        val nodeSource = inputNodes.first()
        addConnection(
            connectNodes(
                nodeSource,
                node,
                randomWeight(random),
                2
            )
        )
        addConnection(
            connectNodes(
                nodeSource,
                node2,
                randomWeight(random),
                3
            )
        )
        addConnection(
            connectNodes(
                node,
                outputNodes.last(),
                randomWeight(random),
                4
            )
        )
        addConnection(
            connectNodes(
                node2,
                outputNodes.last(),
                randomWeight(random),
                5
            )
        )

    }
}