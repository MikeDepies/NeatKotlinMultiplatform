fun adjustedFitnessCalculation(
    population: List<NeatMutator>,
    model: FitnessModel<NeatMutator>,
    deltaFunction: DeltaFunction,
    sharingFunction: SharingFunction
) {
    population.map { sharingFunction(deltaFunction(model.model, it)) }
}typealias SharingFunction = (Float) -> Int
typealias DeltaFunction = (NeatMutator, NeatMutator) -> Float

infix fun Float.percentChanceToMutate(mutation: Mutation) = MutationEntry(rollFrom(this), mutation)