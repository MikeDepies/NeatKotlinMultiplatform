import mutation.mutateAddConnection
import mutation.mutateAddNode
import mutation.mutateConnections
import mutation.mutateToggleConnection
import org.junit.Test
import kotlin.math.roundToInt
import kotlin.random.Random

class SpeciationTest {

    private fun mutationDictionary(activationFunctions: List<ActivationFunction>): List<MutationEntry> {
        return listOf(
            .3f chanceToMutate mutateConnections,
            .04f chanceToMutate mutateAddNode,
            .1f chanceToMutate mutateAddConnection,
//            .1f chanceToMutate mutatePerturbBiasConnections(),
            .11f chanceToMutate mutateToggleConnection,
//            .1f chanceToMutate mutateNodeActivationFunction(activationFunctions),

        )
    }

    @Test
    fun process() {
        val activationFunctions = listOf(sigmoidalTransferFunction, Identity)
        val mutationEntries = mutationDictionary(activationFunctions)
        val df: DeltaFunction = { a, b -> compatibilityDistance(a, b, 1f, 1f, .4f) }
        val sharingFunction = shFunction(3f)
        val simpleNeatExperiment = simpleNeatExperiment(Random(0), 0, 0, activationFunctions)
        val population: List<NeatMutator> = simpleNeatExperiment.generateInitialPopulation(6)

        val speciationController = SpeciationController(0, standardCompatibilityTest(sharingFunction, df))
        val adjustedFitness = adjustedFitnessCalculation(speciationController, df, sharingFunction)

        val speciesScoreKeeper = SpeciesScoreKeeper()
        val speciesLineage = SpeciesLineage(listOf())
        speciationController.speciate(population, speciesLineage, 0)
        val times = 20
        repeat(times) { generation ->
            println("generation $generation (pop: ${speciationController.population().size}")
            val inputOutput = setupEnvironment().map { it() }
            val modelScoreList = evaluatePopulation(speciationController.population(), inputOutput)
                .toModelScores(adjustedFitness)

            sortModelsByAdjustedFitness(speciationController, modelScoreList)
            val newPopulation =
                populateNextGeneration(speciationController, modelScoreList, mutationEntries, simpleNeatExperiment)
            speciationController.speciate(newPopulation, speciesLineage, generation)
            speciesScoreKeeper.updateScores(modelScoreList.map { speciationController.species(it.neatMutator) to it })
        }
        evaluateAndDisplayBestSpecies(speciesScoreKeeper)
    }

    private fun printReport(speciesReport: SpeciesReport) {
        val keys = speciesReport.speciesMap.keys
        for (species in keys) {
            val first = speciesReport.speciesMap.getValue(species).first()
            println("Species ${species.id} (pop: ${speciesReport.speciesOffspringMap[species]} offspring: ${speciesReport.speciesOffspringMap[species]} topScore= {${first.fitness}, ${first.adjustedFitness}})")
            speciesReport.speciesMap.getValue(species).forEach {
                println("\t${it.neatMutator.connections.condensedString()}\t${it.neatMutator.nodes.condensedString()}")
            }
        }
    }
}

private fun setupEnvironment(): List<EnvironmentQuery> {
    return XORTruthTable()
}

private fun sortModelsByAdjustedFitness(
    speciationController: SpeciationController,
    modelScoreList: List<ModelScore>
): List<ModelScore> {
    val adjustedPopulationScore = modelScoreList.toMap { modelScore -> modelScore.neatMutator }
    val fitnessForModel: (NeatMutator) -> Float =
        { neatMutator -> adjustedPopulationScore.getValue(neatMutator).adjustedFitness }
    speciationController.sortSpeciesByFitness(fitnessForModel)
    return modelScoreList
}

private fun evaluateAndDisplayBestSpecies(speciesScoreKeeper: SpeciesScoreKeeper) {
    val neatMutator: NeatMutator = speciesScoreKeeper.getModelScore(speciesScoreKeeper.bestSpecies())!!.neatMutator
    val data = setupEnvironment().map { it() }
    val network = neatMutator.toNetwork()
    val score = data.map {
        network.evaluate(it.first, true)
        println("Expected  : ${it.second}")
        println("Actual RAW: ${network.output()}")
        println("Actual RND: ${network.output().map { it.roundToInt().toFloat() }}")
        val roundedActual = network.output().map { it.roundToInt().toFloat() }
        if (roundedActual == it.second) 1f else 0f
    }.sum()
    println(score)
}

private fun adjustedFitnessCalculation(
    speciationController: SpeciationController,
    df: DeltaFunction,
    sharingFunction: SharingFunction
): AdjustedFitnessCalculation =
    { it -> adjustedFitnessCalculation(speciationController.population(), it, df, sharingFunction) }

private fun NeatExperiment.generateInitialPopulation(populationSize: Int): List<NeatMutator> {
    val neatMutator = createNeatMutator(3, 1, random, sigmoidalTransferFunction)
    return (0 until populationSize).map {
        val clone = neatMutator.clone()
        mutateConnections(this, clone)
        clone
    }
}

private fun standardCompatibilityTest(
    sharingFunction: SharingFunction,
    df: DeltaFunction
): CompatibilityTest = { neat1, neat2 -> sharingFunction(df(neat1, neat2)) == 1 }

private fun XORTruthTable(): List<() -> Pair<List<Float>, List<Float>>> {
    return listOf(
        { listOf(0f, 0f) to listOf(0f) },
        { listOf(0f, 1f) to listOf(0f) },
        { listOf(1f, 0f) to listOf(1f) },
        { listOf(1f, 1f) to listOf(1f) },
    )
}