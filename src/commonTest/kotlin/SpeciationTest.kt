import kotlin.math.*
import kotlin.test.*

class SpeciationTest {

    fun test() {
        .2f percentChanceToMutate uniformWeightPerturbation()
        .5f percentChanceToMutate uniformWeightPerturbation()
    }
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


data class NeatHyperParameters(
    val perturbWeights: Float,
    val modifyNode: Float,
    val addNode: Float,
    val addConnection: Float
)


infix fun Float.percentChanceToMutate(mutation: Mutation) = MutationEntry(rollFrom(this), mutation)

