import org.junit.Test
import setup.*
import kotlin.random.*

class SpeciationTest {

    fun test() {
        .2f percentChanceToMutate uniformWeightPerturbation()
        .5f percentChanceToMutate uniformWeightPerturbation()
    }

    @Test
    fun process() {
        var population: List<NeatMutator> = generateInitialPopulation(Random(0), 100)
        val df: DeltaFunction = { a, b -> compatibilityDistance(a, b, 1f, 1f, .4f) }
        val sharingFunction = shFunction(3f)
        val speciationController = SpeciationController(0, standardCompatibilityTest(sharingFunction, df))
        val times = 100
        speciationController.speciate(population)
        repeat(times) {
            val setupEnvironment = setupEnvironment()
            val inputOutput = setupEnvironment.map { it() }
            val evaluatePopulation = evaluatePopulation(population, inputOutput)
            val adjustedPopulationScore = evaluatePopulation.map { fitnessModel ->
                val adjustedFitness = adjustedFitnessCalculation(population, fitnessModel.first, df, sharingFunction)
                adjustedFitness to fitnessModel
            }
//            mutatePopulation(adjustedPopulationScore)
        }
    }

    private fun mutatePopulation(adjustedPopulationScore: List<AdjustedFitnessModel>) {


        TODO("Not yet implemented")
    }

    private fun evaluatePopulation(
        population: List<NeatMutator>,
        inputOutput: List<EnvironmentEntryElement>
    ): List<Pair<FitnessModel<NeatMutator>, ActivatableNetwork>> {
        return population.map { neatMutator ->
            val network = neatMutator.toNetwork()
            val score = inputOutput.map {
                network.evaluate(it.first)
                if (network.output() == it.second) 1f else 0f
            }.sum()
            FitnessModel(neatMutator, score) to network
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
        return (0 until populationSize).map { neatMutator(3, 1, random, sigmoidalTransferFunction) }
    }

}

private fun standardCompatibilityTest(
    sharingFunction: SharingFunction,
    df: DeltaFunction
) = { neat1, neat2 -> sharingFunction(df(neat1, neat2)) == 1 }