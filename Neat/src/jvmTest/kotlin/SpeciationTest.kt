import setup.*
import kotlin.random.*

class SpeciationTest {

    fun test() {
        .2f percentChanceToMutate uniformWeightPerturbation()
        .5f percentChanceToMutate uniformWeightPerturbation()
    }

    fun process() {
        var population: List<NeatMutator> = generateInitialPopulation(Random(0), 100)
        val df: DeltaFunction = { a, b -> compatibilityDistance(a, b, 1f, 1f, .4f) }

        val times = 100
        repeat(times) {
            val setupEnvironment = setupEnvironment()
            val inputOutput = setupEnvironment.map { it() }
            val evaluatePopulation = evaluatePopulation(population, inputOutput)
            val adjustedPopulationScore = evaluatePopulation.map { fitnessModel ->
                val adjustedFitness = adjustedFitnessCalculation(population, fitnessModel, df, shFunction(3f))
                adjustedFitness to fitnessModel
            }
            mutatePopulation(adjustedPopulationScore)
        }
    }

    private fun mutatePopulation(adjustedPopulationScore: List<AdjustedFitnessModel>) {


        TODO("Not yet implemented")
    }

    private fun evaluatePopulation(
        population: List<NeatMutator>,
        inputOutput: List<EnvironmentEntryElement>
    ): List<FitnessModel<NeatMutator>> {
        return population.map { neatMutator ->
            val network = neatMutator.toNetwork()
            val score = inputOutput.map {
                network.evaluate(it.first)
                if (network.output() == it.second) 1f else 0f
            }.sum()
            FitnessModel(neatMutator, score)
        }
    }

    private fun setupEnvironment(): List<EnvironmentQuery> {
        return listOf(
            { listOf(0f, 0f) to listOf(0f) },
            { listOf(0f, 1f) to listOf(0f) },
            { listOf(1f, 0f) to listOf(1f) },
            { listOf(1f, 1f) to listOf(1f) },
        )
    }

    private fun generateInitialPopulation(random: Random, populationSize: Int): List<NeatMutator> {
        return (0 until populationSize).map { neatMutator(1, 1, random, sigmoidalTransferFunction) }
    }
}

typealias EnvironmentQuery = () -> EnvironmentEntryElement
typealias EnvironmentEntryElement = Pair<List<Float>, List<Float>>
typealias AdjustedFitnessModel = Pair<Float, FitnessModel<NeatMutator>>