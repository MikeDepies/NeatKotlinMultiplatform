import kotlin.random.*

data class CrossOverCandidate(val parent1: NeatMutator, val parent2: NeatMutator)

fun createTwoRelatedGenomes(random: Random): CrossOverCandidate {
    fun weight() = randomWeight(random)
    val nodeGenes1 = listOf(
        NodeGene(1, NodeType.Input),
        NodeGene(2, NodeType.Input),
        NodeGene(3, NodeType.Input),
        NodeGene(4, NodeType.Output),
        NodeGene(5, NodeType.Hidden)
    )

    val connectionGenes1 = listOf(
        ConnectionGene(1, 4, weight(), true, 1),
        ConnectionGene(2, 4, weight(), false, 2),
        ConnectionGene(3, 4, weight(), true, 3),
        ConnectionGene(2, 5, weight(), true, 4),
        ConnectionGene(5, 4, weight(), true, 5),
        ConnectionGene(1, 5, weight(), true, 8)
    )


    val nodeGenes2 = listOf(
        NodeGene(1, NodeType.Input),
        NodeGene(2, NodeType.Input),
        NodeGene(3, NodeType.Input),
        NodeGene(4, NodeType.Output),
        NodeGene(5, NodeType.Hidden),
        NodeGene(6, NodeType.Hidden)
    )

    val connectionGenes2 = listOf(
        ConnectionGene(1, 4, weight(), true, 1),
        ConnectionGene(2, 4, weight(), false, 2),
        ConnectionGene(3, 4, weight(), true, 3),
        ConnectionGene(2, 5, weight(), true, 4),
        ConnectionGene(5, 4, weight(), false, 5),
        ConnectionGene(5, 6, weight(), true, 6),
        ConnectionGene(6, 4, weight(), true, 7),
        ConnectionGene(3, 4, weight(), true, 9),
        ConnectionGene(1, 6, weight(), true, 10)
    )
    return CrossOverCandidate(
        SimpleNeatMutator(nodeGenes1, connectionGenes1),
        SimpleNeatMutator(nodeGenes2, connectionGenes2)
    )
}