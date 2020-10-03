import mutation.mutateAddConnection
import mutation.mutateAddNode
import mutation.mutateConnections
import mutation.mutateToggleConnection
import org.junit.Test
import kotlin.math.roundToInt
import kotlin.random.Random


//val mutation.mutateNodeActivationFunction : mutation.Mutation = { this. }
class SpeciationTest {

    fun test() {
        .2f chanceToMutate mutateConnections
//        addConnection
//        disableConnection
//        addNode
//        changeActivationFunction
//        .5f percentChanceToMutate mutation.uniformWeightPerturbation()
    }


    inner class Epoch {
//        fun
    }


    inner class AdjustFitness {
        @Test
        fun `neatmutator where age debt is present`() {
            TODO()
        }

        @Test
        fun `mark poor performing neatMutators`() {
            TODO()
        }

        @Test
        fun `number of parents`() {
            val survivalThreshold = 0.5f
            val numberOfOrganisms = 0
            val expectedNumberOfParents = TODO()
            //Decide how many get to reproduce based on survival_thresh*pop_size
            //Adding 1.0 ensures that at least one will survive
            // floor is the largest (closest to positive infinity) double value that is not greater
            // than the argument and is equal to a mathematical integer


            //Decide how many get to reproduce based on survival_thresh*pop_size
            //Adding 1.0 ensures that at least one will survive
            // floor is the largest (closest to positive infinity) double value that is not greater
            // than the argument and is equal to a mathematical integer
//num_parents = floor((Neat.p_survival_thresh * (size1 as Double)) + 1.0) as Int
        }

        @Test
        fun `should have incurred age debt`() {
            TODO()
        }

        @Test
        fun `should not incur age debt`() {
            TODO()
        }

        @Test
        fun `age debt penalizes fitness greatly`() {
            TODO()
        }

        @Test
        fun `age significant paramater - boot fitness of younger species`() {
            TODO()
        }

        @Test
        fun `negative fitness is reassigned to nearly 0+`() {
            TODO()
        }

        @Test
        fun `plain adjusted fitness - no age debt - no youth boost`() {
            TODO()
        }

    }

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


    @Test
    fun process() {
        val activationFunctions = listOf(sigmoidalTransferFunction, Identity)
        val simpleNeatExperiment = simpleNeatExperiment(Random(0), 0, 0, activationFunctions)
        val population: List<NeatMutator> = simpleNeatExperiment.generateInitialPopulation(6)
        val mutationEntries = mutationDictionary(activationFunctions)
        val df: DeltaFunction = { a, b -> compatibilityDistance(a, b, 1f, 1f, .4f) }
        val sharingFunction = shFunction(3f)
        val speciationController = SpeciationController(0, standardCompatibilityTest(sharingFunction, df))
        val adjustedFitness = adjustedFitnessCalculation(speciationController, df, sharingFunction)

        val speciesScoreKeeper = SpeciesScoreKeeper()
        val speciesLineage = SpeciesLineage(listOf())
        speciationController.speciate(population, speciesLineage, 0)
        val times = 20
        repeat(times) { generation ->
            println("generation $generation (pop: ${speciationController.population().size}")
            val inputOutput = setupEnvironment().map { it() }
            val modelScoreList = speciationController
                .evaluatePopulation(speciationController.population(), inputOutput)
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

private fun SpeciationTest.evaluateAndDisplayBestSpecies(speciesScoreKeeper: SpeciesScoreKeeper) {
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

private fun List<Pair<FitnessModel<NeatMutator>, ActivatableNetwork>>.toModelScores(adjustedFitness: AdjustedFitnessCalculation): List<ModelScore> {
    return map { it.first }.map { fitnessModel ->
        ModelScore(fitnessModel.model, fitnessModel.score, adjustedFitness(fitnessModel))
    }
}

private fun adjustedFitnessCalculation(
    speciationController: SpeciationController,
    df: DeltaFunction,
    sharingFunction: SharingFunction
): AdjustedFitnessCalculation =
    { it -> adjustedFitnessCalculation(speciationController.population(), it, df, sharingFunction) }

private fun populateNextGeneration(
    speciationController: SpeciationController,
    modelScoreList: List<ModelScore>,
    mutationEntries: List<MutationEntry>,
    simpleNeatExperiment: NeatExperiment
): List<NeatMutator> {
    return speciationController.reproduce(
        simpleNeatExperiment,
        speciationController.speciesReport(modelScoreList),
        offspringFunction(0f, mutationEntries)
    ).values.flatten()
}


fun SpeciationController.reproduce(
    neatExperiment: NeatExperiment,
    speciesReport: SpeciesReport,
    offspringFunction: OffspringFunction
): SpeciesMap {
    return speciesSet.map { species ->
        val speciesPopulation = speciesReport.speciesMap.getValue(species)
        val offspring = speciesReport.speciesOffspringMap.getValue(species)
        val newGenerationPopulation = (0 until offspring).map {
            offspringFunction(neatExperiment, speciesPopulation)
        }
        species to newGenerationPopulation
    }.toMap()
}

fun offspringFunction(chance: Float, mutationEntries: List<MutationEntry>): OffspringFunction {
    val probabilityToMate = rollFrom(chance)
    return {
        newOffspring(
            probabilityToMate,
            this,
            it
        ).mutateModel(mutationEntries, this)
    }
}

typealias OffspringFunction = NeatExperiment.(Collection<ModelScore>) -> NeatMutator

fun NeatMutator.mutateModel(mutationEntries: List<MutationEntry>, neatExperiment: NeatExperiment): NeatMutator {
    mutationEntries.forEach { mutationEntry ->
        mutationEntry.mutate(neatExperiment, this)
    }
    return this
}

fun SpeciationController.speciesReport(modelScoreList: List<ModelScore>): SpeciesReport {
    val overallAverageFitness = modelScoreList.map { modelScore -> modelScore.adjustedFitness }.average()
    return calculateSpeciesReport(modelScoreList, overallAverageFitness)

}

private fun SpeciationController.population() =
    speciesSet.flatMap { getSpeciesPopulation(it) }

private fun newOffspring(
    probabilityToMate: MutationRoll,
    neatExperiment: NeatExperiment,
    speciesPopulation: Collection<ModelScore>
): NeatMutator {
    return when {
        probabilityToMate(neatExperiment) && speciesPopulation.size > 1 -> {
            val randomParent1 = speciesPopulation.random(neatExperiment.random)
            val randomParent2 = (speciesPopulation - randomParent1).random(neatExperiment.random)
            println("mate")
            neatExperiment.crossover(
                FitnessModel(randomParent1.neatMutator, randomParent1.adjustedFitness),
                FitnessModel(randomParent2.neatMutator, randomParent2.adjustedFitness)
            )
        }
        else -> speciesPopulation.random(neatExperiment.random).neatMutator.clone()//.also { println("clone") }
    }
}

private fun NeatExperiment.generateInitialPopulation(populationSize: Int): List<NeatMutator> {
    val neatMutator = createNeatMutator(3, 1, random, sigmoidalTransferFunction)
    return (0 until populationSize).map {
        val clone = neatMutator.clone()
        mutateConnections(this, clone)
        clone
    }
}


fun NeatExperiment.createNeatMutator(
    inputNumber: Int,
    outputNumber: Int,
    random: Random = Random,
    function: ActivationFunction = Identity
): NeatMutator {
    val simpleNeatMutator = simpleNeatMutator(listOf(), listOf())
    createNodes(inputNumber, NodeType.Input, identity(), simpleNeatMutator)
    createNodes(outputNumber, NodeType.Output, function, simpleNeatMutator)
    connectNodes(simpleNeatMutator, random)
    return simpleNeatMutator
}

private fun NeatExperiment.createNodes(
    numberOfNodes: Int, nodeType: NodeType, activationFunction: ActivationFunction, neatMutator: SimpleNeatMutator
) = repeat(numberOfNodes) {
    neatMutator.addNode(NodeGene(nextNode(), nodeType, activationFunction))
}

fun NeatExperiment.newConnection(input: NodeGene, output: NodeGene, neatMutator: SimpleNeatMutator) {
    val weight = random.nextFloat()
    neatMutator.addConnection(ConnectionGene(input.node, output.node, weight, true, nextInnovation()))
}

private fun NeatExperiment.connectNodes(simpleNeatMutator: SimpleNeatMutator, random: Random) {
    for (input in simpleNeatMutator.inputNodes) {
        for (output in simpleNeatMutator.outputNodes) {
            newConnection(input, output, simpleNeatMutator)
        }
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