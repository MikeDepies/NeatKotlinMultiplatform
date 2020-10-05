import mutation.mutateAddConnection
import mutation.mutateAddNode
import mutation.mutateConnections
import mutation.mutateToggleConnection
import kotlin.random.Random

fun mutationDictionary(activationFunctions: List<ActivationFunction>): List<MutationEntry> {
    return listOf(
        .3f chanceToMutate mutateConnections,
        .04f chanceToMutate mutateAddNode,
        .1f chanceToMutate mutateAddConnection,
//            .1f chanceToMutate mutatePerturbBiasConnections(),
        .11f chanceToMutate mutateToggleConnection,
//            .1f chanceToMutate mutateNodeActivationFunction(activationFunctions),

    )
}

fun runNeatExample() {
    val activationFunctions = listOf(sigmoidalTransferFunction, Identity)
    val dataFunction: DistanceFunction = { a, b -> compatibilityDistance(a, b, 1f, 1f, 1f) }
    val sharingFunction = shFunction(3f)
    val simpleNeatExperiment = simpleNeatExperiment(Random(0), 0, 0, activationFunctions)
    val population = simpleNeatExperiment.generateInitialPopulation(1000)
    val speciationController = SpeciationController(0, standardCompatibilityTest(sharingFunction, dataFunction))
    val generationRules = GenerationRules(
        speciationController = speciationController,
        adjustedFitness = adjustedFitnessCalculation(speciationController, dataFunction, sharingFunction),
        reproductionStrategy = weightedReproduction(mutationDictionary(activationFunctions), .11f),
        populationEvaluator = {
            val inputOutput = setupEnvironment().map { it() }
            evaluatePopulation(it, inputOutput)
        }
    )
    val speciesScoreKeeper = SpeciesScoreKeeper()
    val neat = Neat(generationRules) {
        val species = speciesScoreKeeper.bestSpecies()
        val modelScore = speciesScoreKeeper.getModelScore(species)
//        println("$species - ${modelScore?.fitness}")
    }
    neat.process(20, population, speciesScoreKeeper, SpeciesLineage(listOf()), simpleNeatExperiment)
}


fun weightedReproduction(
    mutationEntries: List<MutationEntry>,
    mateChance: Float
): NeatExperiment.(SpeciationController, List<ModelScore>) -> List<NeatMutator> {
    return { speciationController, modelScoreList ->
        populateNextGeneration(speciationController, modelScoreList, mutationEntries, this, mateChance)
    }
}

fun setupEnvironment(): List<EnvironmentQuery> {
    return XORTruthTable()
}


fun adjustedFitnessCalculation(
    speciationController: SpeciationController,
    df: DistanceFunction,
    sharingFunction: SharingFunction
): AdjustedFitnessCalculation =
    { it -> adjustedFitnessCalculation(speciationController.population(), it, df, sharingFunction) }

fun NeatExperiment.generateInitialPopulation(populationSize: Int): List<NeatMutator> {
    val neatMutator = createNeatMutator(3, 1, random, sigmoidalTransferFunction)
    return (0 until populationSize).map {
        val clone = neatMutator.clone()
        mutateConnections(this, clone)
        clone
    }
}

fun standardCompatibilityTest(
    sharingFunction: SharingFunction,
    df: DistanceFunction
): CompatibilityTest = { neat1, neat2 -> sharingFunction(df(neat1, neat2)) == 1 }

fun XORTruthTable(): List<() -> Pair<List<Float>, List<Float>>> {
    return listOf(
        { listOf(0f, 0f) to listOf(0f) },
        { listOf(0f, 1f) to listOf(0f) },
        { listOf(1f, 0f) to listOf(1f) },
        { listOf(1f, 1f) to listOf(1f) },
    )
}