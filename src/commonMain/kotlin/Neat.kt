import kotlin.random.*

data class NodeGene(var node: Int, val nodeType: NodeType, var activationFunction: (Float) -> Float)
data class ConnectionGene(
    var inNode: Int,
    var outNode: Int,
    var weight: Float,
    var enabled: Boolean,
    var innovation: Int
)

enum class NodeType {
    Input, Hidden, Output
}

/**
 * Random/configuration part of the neat algorithm. This will utilize a Mutator that can peform the desire operation,
 * where as the experiment decides on the type of operations
 */
interface NeatExperiment {
    val random : Random
    fun mutateAddConnection(neatMutator: NeatMutator)
    fun mutateAddNode(neatMutator: NeatMutator)
    fun nextInnovation(): Int
    fun crossover(parent1: FitnessModel<NeatMutator>, parent2: FitnessModel<NeatMutator>): NeatMutator
}

data class FitnessModel<T>(val model: T, val score: Float)

interface NeatMutator {
    val nodes: List<NodeGene>
    val connections: List<ConnectionGene>
    val hiddenNodes: List<NodeGene>
    val outputNodes: List<NodeGene>
    val inputNodes: List<NodeGene>

    val lastNode: NodeGene
    fun addConnection(connectionGene: ConnectionGene)
    fun addNode(node: NodeGene)
    fun connectionsTo(first: NodeGene): List<ConnectionGene>
    fun connectionsFrom(first: NodeGene): List<ConnectionGene>
}

fun neatMutator(inputNumber: Int, outputNumber: Int, random: Random = Random): NeatMutator {
    val simpleNeatMutator = SimpleNeatMutator(listOf(), listOf())
    var nodeNumber = 0
    repeat(inputNumber) {
        simpleNeatMutator.addNode(NodeGene(nodeNumber++, NodeType.Input,))
    }
//    val hiddenNode = NodeGene(nodeNumber++, NodeType.Hidden)
//    simpleNeatMutator.addNode(hiddenNode)
    repeat(outputNumber) {
        simpleNeatMutator.addNode(NodeGene(nodeNumber++, NodeType.Output,))
    }
    var innovation = 0
    for (input in simpleNeatMutator.inputNodes) {
//        val weight = random.nextFloat();
        for (output in simpleNeatMutator.outputNodes) {
            val weight = random.nextFloat();
            simpleNeatMutator.addConnection(
                ConnectionGene(
                    input.node,
                    output.node,
                    weight,
                    true,
                    innovation++
                )
            )
        }
    }

    return simpleNeatMutator
}

class SimpleNeatMutator(_nodes: List<NodeGene>, _connections: List<ConnectionGene>) : NeatMutator {
    override val nodes = _nodes.toMutableList()
    override val connections = _connections.toMutableList()
    override val hiddenNodes: List<NodeGene>
        get() = nodes.filter { it.nodeType == NodeType.Hidden }
    override val outputNodes: List<NodeGene>
        get() = nodes.filter { it.nodeType == NodeType.Output }
    override val inputNodes: List<NodeGene>
        get() = nodes.filter { it.nodeType == NodeType.Input }
    override val lastNode: NodeGene
        get() = nodes.last()

    override fun addConnection(connectionGene: ConnectionGene) {
        if (nodes.none { it.node == connectionGene.inNode })
            error("No matching node to connect to ${connectionGene.inNode}")
        if (nodes.none { it.node == connectionGene.outNode })
            error("No matching node to connect to ${connectionGene.outNode}")
        connections += connectionGene
    }

    override fun addNode(node: NodeGene) {
        nodes += node
    }

    override fun connectionsTo(first: NodeGene): List<ConnectionGene> {
        return connections.filter { it.outNode == first.node }
    }

    override fun connectionsFrom(first: NodeGene): List<ConnectionGene> {
        return connections.filter { it.inNode == first.node }
    }

}