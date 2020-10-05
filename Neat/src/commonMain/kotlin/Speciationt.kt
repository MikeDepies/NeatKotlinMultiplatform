import mutation.Mutation

fun adjustedFitnessCalculation(
    population: List<NeatMutator>,
    model: FitnessModel<NeatMutator>,
    distanceFunction: DistanceFunction,
    sharingFunction: SharingFunction
): Float {
    return model.score / (population.map { sharingFunction(distanceFunction(model.model, it)).toFloat() }.sum())
}
typealias SharingFunction = (Float) -> Int
typealias DistanceFunction = (NeatMutator, NeatMutator) -> Float

fun shFunction(deltaThreshold: Float): SharingFunction = { if (it < deltaThreshold) 1 else 0 }
infix fun Float.chanceToMutate(mutation: Mutation) = MutationEntry(rollFrom(this), mutation)

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

fun MutationEntry.mutate(neatExperiment: NeatExperiment, neatMutator: NeatMutator) {
    if (roll(neatExperiment)) {
        mutation(neatExperiment, neatMutator)
    }
}