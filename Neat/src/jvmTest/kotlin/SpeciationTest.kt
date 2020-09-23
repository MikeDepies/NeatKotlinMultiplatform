import org.junit.Test
import setup.*
import kotlin.random.*

val mutateConnections: Mutation = { neatMutator ->
    neatMutator.connections.forEach { connectionGene ->
        mutateConnectionWeight(connectionGene)
    }
}

class SpeciationTest {

    fun test() {

        .2f chanceToMutate mutateConnections
//        addConnection
//        disableConnection
//        addNode
//        changeActivationFunction
//        .5f percentChanceToMutate uniformWeightPerturbation()
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

    @Test
    fun process() {
        var population: List<NeatMutator> = generateInitialPopulation(Random(0), 100)
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
            val evaluatePopulation = evaluatePopulation(population, inputOutput)
            val adjustedPopulationScore = evaluatePopulation.map { fitnessModel ->
                fitnessModel.first.model to adjustedFitness(fitnessModel.first)
            }.toMap()
            val fitnessForModel: (NeatMutator) -> Float = { adjustedPopulationScore.getValue(it) }
            speciationController.sortSpeciesByFitness(fitnessForModel)
//            mutatePopulation(adjustedPopulationScore)
        }
    }

    data class Offspring(val offspring: Int, val skim: Double)

    fun List<ExpectedOffSpring>.countOffspring(skim: Double, y1: Float = 1f): Offspring {
        var offspring = 0
        var newSkim = skim
        this.forEach { expectedOffSpring ->
            val expectedOffspringValue = expectedOffSpring.second
            val nTemp = expectedOffspringValue.div(y1).toInt()
            offspring += nTemp
            newSkim += expectedOffspringValue - (nTemp * y1)
            if (newSkim >= 1f) {
                offspring += 1
                newSkim -= 1f
            }
        }
        return Offspring(offspring, newSkim)
    }

    private fun mutatePopulation(
        adjustedPopulationScore: List<AdjustedFitnessModel>,
        speciationController: SpeciationController
    ) {
        val modelScoreMap = adjustedPopulationScore.map {
            it.second.model to ModelScore(fitness = it.second.score, adjustedFitness = it.first)
        }.toMap()
        speciationController.expectedChildrenInSpecies(modelScoreMap)
//        fun
        //fitness metric per species (top, average, etc)
        //compute expected number of offspring for each individual organism
        //  o -> o.adjustedFitness / populationAdjustedFitnessAverage
        //compute for species
        //check if speciesOffspringExpected is less than total organisms (for experiment[?])
        //      give the difference between expected and total organisms - in new "organisms" to that species to refill the population]
        //Handle where population gets killed of by stagnation + add age to species


        TODO("Not yet implemented")
    }

    private fun evaluatePopulation(
        population: List<NeatMutator>,
        inputOutput: List<EnvironmentEntryElement>
    ): List<Pair<FitnessModel<NeatMutator>, ActivatableNetwork>> {
        return population.map { neatMutator ->
            val network = neatMutator.toNetwork()
            val score = inputOutput.map {
                network.evaluate(it.first, true)
                if (network.output() == it.second) 1f else 0f
            }.sum()
            FitnessModel(neatMutator, score) to network
        }
    }

    private fun setupEnvironment(): List<EnvironmentQuery> {
        return XORTruthTable()
    }

    private fun generateInitialPopulation(random: Random, populationSize: Int): List<NeatMutator> {
        return (0 until populationSize).map { neatMutator(3, 1, random, sigmoidalTransferFunction) }
    }

}

private fun SpeciationController.expectedChildrenInSpecies(modelScoreMap: Map<NeatMutator, ModelScore>): Map<Species, List<ModelScore>> {
    return speciesSet.map { species ->
        species to getSpeciesPopulation(species).map { neatMutator ->
            modelScoreMap.getValue(
                neatMutator
            )
        }
    }.toMap()
}

data class ModelScore(val fitness: Float, val adjustedFitness: Float)

typealias ExpectedOffSpring = Pair<NeatMutator, Float>

private fun XORTruthTable(): List<() -> Pair<List<Float>, List<Float>>> {
    return listOf(
        { listOf(0f, 0f) to listOf(0f) },
        { listOf(0f, 1f) to listOf(0f) },
        { listOf(1f, 0f) to listOf(1f) },
        { listOf(1f, 1f) to listOf(1f) },
    )
}

private fun standardCompatibilityTest(
    sharingFunction: SharingFunction,
    df: DeltaFunction
): CompatibilityTest = { neat1, neat2 -> sharingFunction(df(neat1, neat2)) == 1 }

typealias Operation<T, K> = (T) -> K

enum class OperationMode {
    BatchSequential, AssemblySequential
}

fun t() {

    listOf(1, 5, 3).perform({ }, {}, operationMode = OperationMode.BatchSequential)
}

fun <T, K> List<T>.perform(
    vararg operations: Operation<T, K>,
    operationMode: OperationMode = OperationMode.AssemblySequential
) {
    when (operationMode) {
        OperationMode.BatchSequential -> operations.forEach { op -> forEach { item -> op(item) } }
        OperationMode.AssemblySequential -> forEach { item -> operations.forEach { op -> op(item) } }
    }
}