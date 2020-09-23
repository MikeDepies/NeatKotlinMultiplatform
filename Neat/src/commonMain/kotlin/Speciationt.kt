fun adjustedFitnessCalculation(
    population: List<NeatMutator>,
    model: FitnessModel<NeatMutator>,
    deltaFunction: DeltaFunction,
    sharingFunction: SharingFunction
): Float {
    return model.score / (population.map { sharingFunction(deltaFunction(model.model, it)).toFloat() }.sum())
}
typealias SharingFunction = (Float) -> Int
typealias DeltaFunction = (NeatMutator, NeatMutator) -> Float

fun shFunction(deltaThreshold: Float): SharingFunction = { if (it < deltaThreshold) 1 else 0 }
infix fun Float.chanceToMutate(mutation: Mutation) = MutationEntry(rollFrom(this), mutation)