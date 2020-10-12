package neat

import neat.model.NeatMutator
import neat.mutation.*
import kotlin.math.absoluteValue
import kotlin.random.Random

fun mutationDictionary(): List<MutationEntry> {
    return listOf(
        .8f chanceToMutate mutateConnections,
        .4f chanceToMutate mutateAddNode,
        .4f chanceToMutate mutateAddConnection,
        .1f chanceToMutate mutatePerturbBiasConnections(),
        .11f chanceToMutate mutateToggleConnection,
        .1f chanceToMutate mutateNodeActivationFunction(),
        )
}


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
    var reproductionStrategy: ReproductionStrategy = weightedReproduction(mutationEntries, .4f, .6f)
    var evaluationFunction: PopulationEvaluator = { error("need to provide a evaluator function") }
}

fun compatibilityDistanceFunction(c1: Float, c2: Float, c3: Float): DistanceFunction = { a, b ->
    compatibilityDistance(a, b, c1, c2, c3)
}

fun runNodeCountExample() {
    val activationFunctions = baseActivationFunctions()
    val distanceFunction: DistanceFunction = { a, b -> compatibilityDistance(a, b, 1f, 1f, .4f) }
    val sharingFunction = shFunction(10f)
    val simpleNeatExperiment = simpleNeatExperiment(Random(0), 0, 0, activationFunctions)
    val population = simpleNeatExperiment.generateInitialPopulation(100, 3, 1, Activation.sigmoidal)
    val speciationController = SpeciationController(0, standardCompatibilityTest(sharingFunction, distanceFunction))
    val generationRules = GenerationRules(
        speciationController = speciationController,
        adjustedFitness = adjustedFitnessCalculation(speciationController, distanceFunction, sharingFunction),
        reproductionStrategy = weightedReproduction(mutationDictionary(), .41f, .7f),
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
    val activationFunctions = listOf(SigmoidalTransferFunction, Identity)
    val dataFunction: DistanceFunction = { a, b -> compatibilityDistance(a, b, 1f, 1f, .4f) }
    val sharingFunction = shFunction(1f)
    val simpleNeatExperiment = simpleNeatExperiment(Random(0), 0, 0, baseActivationFunctions())
    val population = simpleNeatExperiment.generateInitialPopulation(100, 3, 1, Activation.sigmoidal)
    val speciationController = SpeciationController(0, standardCompatibilityTest(sharingFunction, dataFunction))
    val generationRules = GenerationRules(
        speciationController = speciationController,
        adjustedFitness = adjustedFitnessCalculation(speciationController, dataFunction, sharingFunction),
        reproductionStrategy = weightedReproduction(mutationDictionary(), .41f, .7f),
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
    mateChance: Float,
    survivalThreshold: Float
): NeatExperiment.(SpeciationController, List<ModelScore>) -> List<NeatMutator> {
    return { speciationController, modelScoreList ->
        populateNextGeneration(speciationController, modelScoreList, mutationEntries, this, mateChance,
            survivalThreshold
        )
    }
}

fun adjustedFitnessCalculation(
    speciationController: SpeciationController,
    df: DistanceFunction,
    sharingFunction: SharingFunction
): AdjustedFitnessCalculation =
    { fitnessModel ->
        val adjustedFitnessCalculation =
            adjustedFitnessCalculation(speciationController.population(), fitnessModel, df, sharingFunction)
        adjustedFitnessCalculation
    }

fun NeatExperiment.generateInitialPopulation(
    populationSize: Int, numberOfInputNodes: Int, numberOfOutputNodes: Int, function: ActivationGene
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