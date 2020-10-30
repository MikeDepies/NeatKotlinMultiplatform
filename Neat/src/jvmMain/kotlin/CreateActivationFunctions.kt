import neat.ActivationGene

fun createActivationFunctions(
    activationFunctionMap: Map<String, ActivationGene>,
    activationFunctions: List<ActivationDefinition>
) = activationFunctions.map { activationFunctionMap.getValue(it.name) }