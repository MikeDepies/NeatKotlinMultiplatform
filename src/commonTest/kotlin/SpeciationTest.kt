import kotlin.math.*
import kotlin.test.*

class SpeciationTest {
    @Test
    fun `modified sig function x=0`() {
        assertEquals(.5f, sigmoidalTransferFunction(Float.MIN_VALUE))
    }

    @Test
    fun `modified sig function approaches -0`() {
        assertEquals(1f, sigmoidalTransferFunction(Float.MAX_VALUE))
    }

    fun test() {
        .2f percentChanceToMutate uniformWeightPerturbation()
        .5f percentChanceToMutate uniformWeightPerturbation()
    }
}

fun mutateNodeActivationFunction(activationFunctions: List<ActivationFunction>): Mutation = { neatMutator ->
    val nodeGene = neatMutator.nodes.filter { it.nodeType != NodeType.Input }.random(random)
    nodeGene.activationFunction = (activationFunctions - nodeGene.activationFunction).random(random)
}

fun adjustedFitnessCalculation(
    population: List<NeatMutator>,
    model: FitnessModel<NeatMutator>,
    deltaFunction: DeltaFunction,
    sharingFunction: SharingFunction
) {
    population.map { sharingFunction(deltaFunction(model.model, it)) }
}

typealias SharingFunction = (Float) -> Int
typealias DeltaFunction = (NeatMutator, NeatMutator) -> Float
typealias ActivationFunction = (Float) -> Float

/**
 * As found in Stanely NEAT paper.
 */
fun sigmoidalTransferFunction(x: Float): Float = 1.div(1 + exp(-4.9f * x))
typealias Mutation = NeatExperiment.(NeatMutator) -> Unit
typealias MutationRoll = NeatExperiment.() -> Boolean

data class NeatHyperParameters(
    val perturbWeights: Float,
    val modifyNode: Float,
    val addNode: Float,
    val addConnection: Float
)

data class MutationEntry(val roll: MutationRoll, val mutation: Mutation)

infix fun Float.percentChanceToMutate(mutation: Mutation) = MutationEntry(rollFrom(this), mutation)

fun rollFrom(chance: Float): MutationRoll = {
    (random.nextFloat() <= chance)
}

fun uniformWeightPerturbation(range: Float = .1f): Mutation = { neatMutator ->
    if (range < 0) error("range [$range] must be greater than 0")
    neatMutator.connections.forEach { connection ->
        val perturbation = (random.nextFloat() * (range * 2)) - range
        connection.weight += perturbation
    }
}

