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
                fitnessModel.first.model to adjustedFitness
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
            val nTemp = expectedOffSpring.div(y1).toInt()
            offspring += nTemp
            newSkim += expectedOffSpring - (nTemp * y1)
            if (newSkim >= 1f) {
                offspring += 1
                newSkim -= 1f
            }
        }
        return Offspring(offspring, newSkim)
    }

    private fun mutatePopulation(adjustedPopulationScore: List<AdjustedFitnessModel>) {
        //average fitness per spcies?
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

typealias ExpectedOffSpring = Float

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