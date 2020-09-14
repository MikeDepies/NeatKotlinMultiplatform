import kotlin.random.*

data class NodeGene(var node: Int, val nodeType: NodeType, var activationFunction: (Float) -> Float) {
//    var value: Float = 0f
}

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
    val random: Random
    fun mutateAddConnection(neatMutator: NeatMutator)
    fun mutateAddNode(neatMutator: NeatMutator)
    fun nextInnovation(): Int
    fun crossover(parent1: FitnessModel<NeatMutator>, parent2: FitnessModel<NeatMutator>): NeatMutator
    fun nextNode(): Int
}

data class FitnessModel<T>(val model: T, val score: Float)

fun <T> identity(): (T) -> T = { it }
interface NeatMutator {
    val nodes: List<NodeGene>
    val connections: List<ConnectionGene>
    val hiddenNodes: List<NodeGene>
    val outputNodes: List<NodeGene>
    val inputNodes: List<NodeGene>
    val connectableNodes: List<PotentialConnection>

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
        simpleNeatMutator.addNode(NodeGene(nodeNumber++, NodeType.Input, identity()))
    }
//    val hiddenNode = NodeGene(nodeNumber++, NodeType.Hidden)
//    simpleNeatMutator.addNode(hiddenNode)
    repeat(outputNumber) {
        simpleNeatMutator.addNode(NodeGene(nodeNumber++, NodeType.Output, identity()))
    }
    var innovation = 0
    for (input in simpleNeatMutator.inputNodes) {
//        val weight = random.nextFloat();
        for (output in simpleNeatMutator.outputNodes) {
            val weight = random.nextFloat()
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
    private val _connectableNodes = resolvePotentialConnections(_nodes, _connections).toMutableList()

    private fun resolvePotentialConnections(
        _nodes: List<NodeGene>,
        _connections: List<ConnectionGene>
    ): List<PotentialConnection> {
        return _nodes.flatMap { sourceNode ->
            eligibleNodes(_nodes, sourceNode).map { targetNode ->
                PotentialConnection(sourceNode.node, targetNode.node)
            }
        }.filter { it.alreadyConnected(_connections) }
    }

    private fun eligibleNodes(
        _nodes: List<NodeGene>,
        sourceNode: NodeGene
    ) = _nodes.filterNot {
        val bothAreOutputs = bothAreOutputNodes(sourceNode, it)
        val bothAreInputs = bothAreInputs(sourceNode, it)
        bothAreInputs || bothAreOutputs
    }

    private fun bothAreInputs(sourceNode: NodeGene, it: NodeGene) =
        sourceNode.nodeType == NodeType.Input && it.nodeType == NodeType.Input

    private fun bothAreOutputNodes(sourceNode: NodeGene, it: NodeGene) =
        sourceNode.nodeType == NodeType.Output && it.nodeType == NodeType.Output

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
    override val connectableNodes: List<PotentialConnection>
        get() = _connectableNodes.toList()

    override fun addConnection(connectionGene: ConnectionGene) {
        if (nodes.none { it.node == connectionGene.inNode })
            error("No matching node to connect to ${connectionGene.inNode}")
        if (nodes.none { it.node == connectionGene.outNode })
            error("No matching node to connect to ${connectionGene.outNode}")
        if (connections.any { it.inNode == connectionGene.inNode && it.outNode == connectionGene.outNode })
            error("Can not add a connection gene between already connected nodes")
        connections += connectionGene
        _connectableNodes -= PotentialConnection(connectionGene.inNode, connectionGene.outNode)
    }

    override fun addNode(node: NodeGene) {
        nodes += node
        eligibleNodes(nodes, node).forEach {
            _connectableNodes.add(PotentialConnection(node.node, it.node))
        }
    }

    override fun connectionsTo(first: NodeGene): List<ConnectionGene> {
        return connections.filter { it.outNode == first.node }
    }

    override fun connectionsFrom(first: NodeGene): List<ConnectionGene> {
        return connections.filter { it.inNode == first.node }
    }

}

private fun PotentialConnection.alreadyConnected(_connections: List<ConnectionGene>): Boolean {
    return _connections.any { it.inNode == sourceNode && it.outNode == targetNode }
}

data class PotentialConnection(val sourceNode: Int, val targetNode: Int)