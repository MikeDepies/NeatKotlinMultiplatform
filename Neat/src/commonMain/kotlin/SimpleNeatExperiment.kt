import kotlin.jvm.JvmName
import kotlin.random.*

fun simpleNeatExperiment(
    random: Random,
    innovation: Int,
    nodeInnovation: Int,
    activationFunctions: List<ActivationFunction>
): NeatExperiment {
    return SimpleNeatExperiment(innovation, nodeInnovation, activationFunctions, random)
}

fun matchingGenes(
    parent1: NeatMutator,
    parent2: NeatMutator
): List<Pair<ConnectionGene, ConnectionGene>> {
    return parent1.connections.filter { c1 ->
        parent2.connections.any { c2 -> c2.innovation == c1.innovation }
    }.map { c1 -> c1 to parent2.connections.first { c2 -> c2.innovation == c1.innovation } }
}


class SimpleNeatExperiment(
    private var innovation: Int,
    private var nodeInnovation: Int,
    val activationFunctions: List<ActivationFunction>,
    override val random: Random,
) : NeatExperiment {

//    private var innovation = innovation
//    private var nodeInnovation = nodeInnovation
//    override val random: Random get() = random

    fun connectionGene(potentialConnection: PotentialConnection): ConnectionGene {
        val (sourceNode, targetNode, type) = potentialConnection
        return when (type) {
            ConnectionType.UniDirectional -> connectionGene(sourceNode, targetNode)
            ConnectionType.BiDirectional -> when (random.nextBoolean()) {
                true -> connectionGene(sourceNode, targetNode)
                false -> connectionGene(targetNode, sourceNode)
            }
        }
    }

    private fun connectionGene(sourceNode: Int, targetNode: Int): ConnectionGene {
        return ConnectionGene(
            sourceNode,
            targetNode,
            randomWeight(random),
            true,
            nextInnovation()
        )
    }

    override fun mutateAddConnection(neatMutator: NeatMutator) {
        val potentialConnection = neatMutator.connectableNodes.random(random)
        neatMutator.addConnection(connectionGene(potentialConnection))
    }

    override fun mutateAddNode(neatMutator: NeatMutator) {
        val randomConnection = neatMutator.connections.random(random)
        val node = NodeGene(nextNode(), NodeType.Hidden, activationFunctions.random(random))
        val copiedConnection = randomConnection.copy(innovation = nextInnovation(), inNode = node.node)
        val newEmptyConnection = ConnectionGene(randomConnection.inNode, node.node, 1f, true, nextInnovation())
        println("\tMUTATE ADD NODE")
        println("\t${neatMutator.connections.condensedString()}\t${neatMutator.nodes.condensedString()}")
        randomConnection.enabled = false
        neatMutator.apply {
            addNode(node)
            addConnection(copiedConnection)
            addConnection(newEmptyConnection)
        }
        println("\t${neatMutator.connections.condensedString()}\t${neatMutator.nodes.condensedString()}")
    }

    override fun nextInnovation(): Int {
        return this.innovation++
    }

    override fun nextNode(): Int {
        return this.nodeInnovation++
    }

    override fun crossover(parent1: FitnessModel<NeatMutator>, parent2: FitnessModel<NeatMutator>): NeatMutator {
        val (disjoint1, disjoint2) = disjoint(parent1.model, parent2.model)
        val excess = excess(parent1.model, parent2.model)
        val matchingGenes = matchingGenes(parent1.model, parent2.model)
        val offSpringConnections = when {
            parent1.isLessFitThan(parent2) -> {
                (matchingGenes.map { it.random(random) } + disjoint2 + excess).sortedBy { it.innovation }
            }
            parent1.isMoreFitThan(parent2) -> {
                (matchingGenes.map { it.random(random) } + disjoint1 + excess).sortedBy { it.innovation }
            }
            else -> {
                (matchingGenes.map { it.random(random) } + (disjoint1 + disjoint2 + excess).filter { random.nextBoolean() }).sortedBy { it.innovation }
            }
        }.map { it.copy() }
        val nodes = if (parent1.isMoreFitThan(parent2)) parent1.model.nodes else parent2.model.nodes
        return simpleNeatMutator(nodes.toList(), offSpringConnections.toList())
    }


}

fun NeatExperiment.newNode(activationFunction: ActivationFunction): NodeGene {
    return NodeGene(nextNode(), NodeType.Hidden, activationFunction)
}

fun List<ConnectionGene>.condensedString(): String {
    fun enabled(connectionGene: ConnectionGene) = if (connectionGene.enabled) "" else "!"
    fun weight(it: ConnectionGene) = "= ${it.weight}"

    return joinToString("\t") { "${enabled(it)}[${it.inNode},${it.outNode}]" }
}


@JvmName("condensedNodeGeneString")
fun List<NodeGene>.condensedString(): String {
    return "[${joinToString(", ") { "${it.node}" }}]"
}
