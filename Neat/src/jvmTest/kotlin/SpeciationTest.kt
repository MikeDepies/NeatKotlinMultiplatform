import mutation.*
import org.junit.Test
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
            .5f chanceToMutate mutateConnections,
            .2f chanceToMutate mutateAddNode,
            .2f chanceToMutate mutateAddConnection,
            .1f chanceToMutate mutatePerturbBiasConnections(),
            .1f chanceToMutate mutateDisableConnection,
            .1f chanceToMutate mutateNodeActivationFunction(activationFunctions),

            )
    }

    @Test
    fun process() {

        var population: List<NeatMutator> = generateInitialPopulation(Random(0), 100)
        val node = population.first().lastNode.node
        val innovation = population.first().connections.last().innovation
        val activationFunctions = listOf(sigmoidalTransferFunction, Identity)
        val simpleNeatExperiment =
            simpleNeatExperiment(Random(0), innovation, node, activationFunctions)
        val mutationEntries = mutationDictionary(activationFunctions)
        val size = population.size
        val df: DeltaFunction = { a, b -> compatibilityDistance(a, b, 1f, 1f, .4f) }
        val sharingFunction = shFunction(3f)
        val speciationController = SpeciationController(0, standardCompatibilityTest(sharingFunction, df))
        val times = 100
        speciationController.speciate(population)
        val adjustedFitness: AdjustedFitnessCalculation =
            { it -> adjustedFitnessCalculation(population, it, df, sharingFunction) }



        repeat(times) {
            val setupEnvironment = setupEnvironment()
            val inputOutput = setupEnvironment.map { it() }
            val modelScoreList =
                speciationController.evaluatePopulation(population, inputOutput).map { it.first }.map { fitnessModel ->
                    ModelScore(fitnessModel.model, fitnessModel.score, adjustedFitness(fitnessModel))
                }
            val adjustedPopulationScore = modelScoreList.map { it.neatMutator to it }.toMap()
            val fitnessForModel: (NeatMutator) -> Float = { adjustedPopulationScore.getValue(it).adjustedFitness }
            speciationController.sortSpeciesByFitness(fitnessForModel)
//            speciationController.mutatePopulation(modelScoreList)

            val overallAverageFitness = modelScoreList.map { it.adjustedFitness }.average()

            val speciesReport = speciationController.speciesReport(modelScoreList, overallAverageFitness)
            population = speciationController.reproduce(
                mutationEntries,
                simpleNeatExperiment,
                speciesReport
            )

//            population = listOf()
        }
    }

    //species reproduce
    //iterate for the number of expected children of given species
    //handle "super champions" inside the species population
    //  these get special handling for how they are brought forward
    //Then with remaining slots (expectedChildren -numberOfSuperChamps)
    //check if remaining is > 5 - ifso bring clone the champion w/o mutation and bring forward
    //if (mutateOnly roll || poolsize == 1)
    //else mate two models & mutate
    fun SpeciationController.reproduce(
        mutationEntries: List<MutationEntry>,
        neatExperiment: NeatExperiment,
        speciesReport: SpeciesReport
    ): List<NeatMutator> {
        val probabilityToMate = rollFrom(.3f)
        fun mutateModel(neatMutator: NeatMutator) {
            mutationEntries.forEach { mutationEntry ->
                mutationEntry.mutate(neatExperiment, neatMutator)
            }
        }

        speciesSet.forEach { species ->
            val speciesPopulation = speciesReport.speciesMap.getValue(species)
            val offspring = speciesReport.speciesOffspringMap.getValue(species)

            repeat(offspring) {
                val newOffspring = newOffspring(probabilityToMate, neatExperiment, speciesPopulation)
                mutateModel(newOffspring)
            }
        }


        TODO()
    }


    private fun setupEnvironment(): List<EnvironmentQuery> {
        return XORTruthTable()
    }

}

private fun newOffspring(
    probabilityToMate: MutationRoll,
    neatExperiment: NeatExperiment,
    speciesPopulation: Collection<ModelScore>
): NeatMutator {
    return when {
        probabilityToMate(neatExperiment) && speciesPopulation.size > 1 -> {
            val randomParent1 = speciesPopulation.random(neatExperiment.random)
            val randomParent2 = (speciesPopulation - randomParent1).random(neatExperiment.random)
            neatExperiment.crossover(
                FitnessModel(randomParent1.neatMutator, randomParent1.adjustedFitness),
                FitnessModel(randomParent2.neatMutator, randomParent2.adjustedFitness)
            )
        }
        else -> speciesPopulation.random(neatExperiment.random).neatMutator.clone()
    }
}

private fun generateInitialPopulation(random: Random, populationSize: Int): List<NeatMutator> {
    return (0 until populationSize).map { neatMutator(3, 1, random, sigmoidalTransferFunction) }
}


private fun standardCompatibilityTest(
    sharingFunction: SharingFunction,
    df: DeltaFunction
): CompatibilityTest = { neat1, neat2 -> sharingFunction(df(neat1, neat2)) == 1 }

typealias Operation<T, K> = (T) -> K

enum class OperationMode {
    BatchSequential, AssemblySequential
}

//
//fun <T, K> List<T>.perform(
//    vararg operations: Operation<T, K>,
//    operationMode: OperationMode = OperationMode.AssemblySequential
//) {
//    when (operationMode) {
//        OperationMode.BatchSequential -> operations.forEach { op -> forEach { item -> op(item) } }
//        OperationMode.AssemblySequential -> forEach { item -> operations.forEach { op -> op(item) } }
//    }
//}

private fun XORTruthTable(): List<() -> Pair<List<Float>, List<Float>>> {
    return listOf(
        { listOf(0f, 0f) to listOf(0f) },
        { listOf(0f, 1f) to listOf(0f) },
        { listOf(1f, 0f) to listOf(1f) },
        { listOf(1f, 1f) to listOf(1f) },
    )
}