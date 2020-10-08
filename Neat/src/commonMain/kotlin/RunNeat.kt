import mutation.*
import kotlin.math.absoluteValue
import kotlin.random.Random

fun mutationDictionary(activationFunctions: List<ActivationFunction>): List<MutationEntry> {
    return listOf(
        .8f chanceToMutate mutateConnections,
        .4f chanceToMutate mutateAddNode,
        .4f chanceToMutate mutateAddConnection,
        .1f chanceToMutate mutatePerturbBiasConnections(),
        .11f chanceToMutate mutateToggleConnection,
        .4f chanceToMutate mutateNodeActivationFunctionWithConnectionInnovations(),

        )
}
typealias NeatCreator = (Int, Int) -> Neat

fun NeatBuilder.generationRules(): GenerationRules {
    val speciationController =
        SpeciationController(0, standardCompatibilityTest(sharingFunction, distanceFunction))
    return GenerationRules(
        speciationController,
        adjustedFitnessCalculation(speciationController, distanceFunction, sharingFunction),
        reproductionStrategy,
        evaluationFunction
    )
}

fun neat(mutationEntries: List<MutationEntry>, builder: NeatBuilder.() -> Unit): Neat {
    val neatBuilder = NeatBuilder(mutationEntries)
    neatBuilder.builder()
    return Neat(neatBuilder.generationRules())
}

class NeatBuilder(mutationEntries: List<MutationEntry>) {
    var sharingFunction: SharingFunction = shFunction(3f)
    var distanceFunction: DistanceFunction = compatibilityDistanceFunction(1f, 1f, 1f)
    var reproductionStrategy: ReproductionStrategy = weightedReproduction(mutationEntries, .4f)
    var evaluationFunction: PopulationEvaluator = { error("need to provide a evaluator function") }
}

fun compatibilityDistanceFunction(c1: Float, c2: Float, c3: Float): DistanceFunction = { a, b ->
    compatibilityDistance(a, b, c1, c2, c3)
}

//data class Population(val size : Int, val input : Int, val output : Int)
fun runNeatExample(
    distanceFunction: DistanceFunction,
    compatibilityThreshold: Float,
    activationFunctions: List<ActivationFunction>,
    reproductionStrategy: ReproductionStrategy,
    evaluationFunction: PopulationEvaluator,
    populationSize: Int
): Pair<SpeciesScoreKeeper, SpeciesLineage> {
    val sharingFunction = shFunction(compatibilityThreshold)
    val simpleNeatExperiment = simpleNeatExperiment(Random(0), 0, 0, activationFunctions)
    val population = simpleNeatExperiment.generateInitialPopulation(populationSize, 3, 1, sigmoidalTransferFunction)
    val speciationController = SpeciationController(0, standardCompatibilityTest(sharingFunction, distanceFunction))
    val generationRules = GenerationRules(
        speciationController = speciationController,
        adjustedFitness = adjustedFitnessCalculation(speciationController, distanceFunction, sharingFunction),
        reproductionStrategy = reproductionStrategy,
        populationEvaluator = evaluationFunction
    )
    val speciesScoreKeeper = SpeciesScoreKeeper()
    val newGenerationHandler: (SpeciesMap) -> Unit = {
        val species = speciesScoreKeeper.bestSpecies()
        val modelScore = speciesScoreKeeper.getModelScore(species)
        println("$species - ${modelScore?.fitness}")
    }
    val neat = Neat(generationRules)
    val speciesLineage = SpeciesLineage(listOf())
    neat.process(200, population, speciesScoreKeeper, speciesLineage, simpleNeatExperiment)
    return speciesScoreKeeper to speciesLineage
}

fun runNodeCountExample() {
    val activationFunctions = listOf(sigmoidalTransferFunction, Identity)
    val distanceFunction: DistanceFunction = { a, b -> compatibilityDistance(a, b, 1f, 1f, .4f) }
    val sharingFunction = shFunction(10f)
    val simpleNeatExperiment = simpleNeatExperiment(Random(0), 0, 0, activationFunctions)
    val population = simpleNeatExperiment.generateInitialPopulation(100, 3, 1, sigmoidalTransferFunction)
    val speciationController = SpeciationController(0, standardCompatibilityTest(sharingFunction, distanceFunction))
    val generationRules = GenerationRules(
        speciationController = speciationController,
        adjustedFitness = adjustedFitnessCalculation(speciationController, distanceFunction, sharingFunction),
        reproductionStrategy = weightedReproduction(mutationDictionary(activationFunctions), .41f),
        populationEvaluator = { population ->
            population.map { FitnessModel(it, 32f - (32 - it.nodes.size).absoluteValue) }
        }
    )
    val speciesScoreKeeper = SpeciesScoreKeeper()
    val neat = Neat(generationRules) /*{
        val species = speciesScoreKeeper.bestSpecies()
        val modelScore = speciesScoreKeeper.getModelScore(species)
        println("$species - ${modelScore?.fitness}")
    }*/
    neat.process(100, population, speciesScoreKeeper, SpeciesLineage(listOf()), simpleNeatExperiment)
    return speciesScoreKeeper.run { getModelScore(bestSpecies()) }
}


fun runWeightSummationExample() {
    val activationFunctions = listOf(sigmoidalTransferFunction, Identity)
    val dataFunction: DistanceFunction = { a, b -> compatibilityDistance(a, b, 1f, 1f, .4f) }
    val sharingFunction = shFunction(1f)
    val simpleNeatExperiment = simpleNeatExperiment(Random(0), 0, 0, activationFunctions)
    val population = simpleNeatExperiment.generateInitialPopulation(100, 3, 1, sigmoidalTransferFunction)
    val speciationController = SpeciationController(0, standardCompatibilityTest(sharingFunction, dataFunction))
    val generationRules = GenerationRules(
        speciationController = speciationController,
        adjustedFitness = adjustedFitnessCalculation(speciationController, dataFunction, sharingFunction),
        reproductionStrategy = weightedReproduction(mutationDictionary(activationFunctions), .41f),
        populationEvaluator = { population ->
            population.map { FitnessModel(it, 100f - (100 - it.connections.map { c -> c.weight }.sum()).absoluteValue) }
        }
    )
    val speciesScoreKeeper = SpeciesScoreKeeper()
    val neat = Neat(generationRules) /*{
        val species = speciesScoreKeeper.bestSpecies()
        val modelScore = speciesScoreKeeper.getModelScore(species)
        println("$species - ${modelScore?.fitness}")
    }*/
    neat.process(100, population, speciesScoreKeeper, SpeciesLineage(listOf()), simpleNeatExperiment)
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
    { fitnessModel -> adjustedFitnessCalculation(speciationController.population(), fitnessModel, df, sharingFunction) }

fun NeatExperiment.generateInitialPopulation(
    populationSize: Int, numberOfInputNodes: Int, numberOfOutputNodes: Int, function: ActivationFunction
): List<NeatMutator> {
    val neatMutator = createNeatMutator(numberOfInputNodes, numberOfOutputNodes, random, function)
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