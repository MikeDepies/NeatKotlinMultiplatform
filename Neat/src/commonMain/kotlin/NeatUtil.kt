import kotlin.math.*
import kotlin.random.*

fun connectNodes(nodeSource: NodeGene, nodeTarget: NodeGene, weight: Float, innovation: Int): ConnectionGene {
    return ConnectionGene(nodeSource.node, nodeTarget.node, weight, true, innovation)
}

fun excess(parent1: NeatMutator, parent2: NeatMutator): List<ConnectionGene> {
    val lastSharedInnovation = min(lastInnovation(parent1), lastInnovation(parent2))
    fun genesAfterInnovation(neatMutator: NeatMutator) =
        neatMutator.connections.filter { it.innovation > lastSharedInnovation }
    return genesAfterInnovation(parent1) + genesAfterInnovation(parent2)
}

fun disjoint(parent1: NeatMutator, parent2: NeatMutator): DisjointResult {
    val lastSharedInnovation = min(lastInnovation(parent1), lastInnovation(parent2))
    fun findDisjointedNodes(parent1: NeatMutator, parent2: NeatMutator): List<ConnectionGene> {
        return parent1.connections
            .filter { c1 ->
                val noMatchingInnovation = parent2.connections.none { c2 -> c1.innovation == c2.innovation }
                c1.innovation <= lastSharedInnovation && noMatchingInnovation
            }
    }

    val disjoint1 = findDisjointedNodes(parent1, parent2)
    val disjoint2 = findDisjointedNodes(parent2, parent1)
    return DisjointResult(disjoint1, disjoint2)
}

data class DisjointResult(val disjoint1: List<ConnectionGene>, val disjoint2: List<ConnectionGene>)
data class ExcessResult(val excess1: List<ConnectionGene>, val excess2: List<ConnectionGene>)

fun compatibilityDifference(
    excess: Int,
    disjoint: Int,
    averageSharedWeightsDelta: Float,
    numberOfGenes: Int,
    excessWeight: Float,
    disjointWeight: Float,
    weightDeltaWeight: Float
): Float {
    return (excessWeight * excess).div(numberOfGenes) +
            (disjointWeight * disjoint).div(numberOfGenes) +
            (weightDeltaWeight * averageSharedWeightsDelta)
}

fun lastInnovation(neatMutator: NeatMutator) = neatMutator.connections.last().innovation
fun randomWeight(random: Random): Float {
    return (random.nextFloat() * 2) - 1
}

fun <T> FitnessModel<T>.isLessFitThan(model: FitnessModel<T>) = score < model.score
fun <T> FitnessModel<T>.isMoreFitThan(model: FitnessModel<T>) = score > model.score
fun <T> FitnessModel<T>.equallyFit(model: FitnessModel<T>) = score == model.score
fun <A> Pair<A, A>.random(random: Random = Random) = if (random.nextBoolean()) first else second
interface ActivatableNetwork {
    fun evaluate(input: List<Float>) : Unit
    fun output(): List<Float>
}