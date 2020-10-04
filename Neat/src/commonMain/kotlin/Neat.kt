import kotlin.random.Random

typealias Generations = Int

typealias EnvironmentQuery = () -> EnvironmentEntryElement
typealias EnvironmentEntryElement = Pair<List<Float>, List<Float>>

data class FitnessModel<T>(val model: T, val score: Float)

fun <T> identity(): (T) -> T = { it }

typealias ReproductionStrategy = NeatExperiment.(SpeciationController, List<ModelScore>) -> List<NeatMutator>

fun SpeciationController.population() =
    speciesSet.flatMap { getSpeciesPopulation(it) }
data class Population(val nodeInnovation: Int, val connectionInnovation: Int, val population: List<NeatMutator>)
data class GenerationRules(
    val activationFunctions: List<ActivationFunction>,
    val speciationController: SpeciationController,
    val adjustedFitness: AdjustedFitnessCalculation,
    val reproductionStrategy: ReproductionStrategy,
    val populationEvaluator: (List<NeatMutator>) -> List<FitnessModel<NeatMutator>>
)

class Neat(
    private val random: Random,
    private val generationRules: GenerationRules
) {
    fun process(
        times: Int,
        population: Population,
        speciesScoreKeeper: SpeciesScoreKeeper,
        speciesLineage: SpeciesLineage
    ) {
        val (activationFunctions, speciationController, adjustedFitness, reproductionStrategy, populationEvaluator) = generationRules
        val simpleNeatExperiment = simpleNeatExperiment(
            random,
            population.connectionInnovation,
            population.nodeInnovation,
            activationFunctions
        )
        speciationController.speciate(population.population, speciesLineage, 0)
        repeat(times) { generation ->
            val modelScoreList =
                populationEvaluator(speciationController.population()).toModelScores(adjustedFitness)
            sortModelsByAdjustedFitness(speciationController, modelScoreList)
            val newPopulation = reproductionStrategy(simpleNeatExperiment, speciationController, modelScoreList)
            speciationController.speciate(newPopulation, speciesLineage, generation)
            speciesScoreKeeper.updateScores(modelScoreList.map { speciationController.species(it.neatMutator) to it })
        }
    }

    private fun sortModelsByAdjustedFitness(
        speciationController: SpeciationController,
        modelScoreList: List<ModelScore>
    ): List<ModelScore> {
        val adjustedPopulationScore = modelScoreList.toMap { modelScore -> modelScore.neatMutator }
        val fitnessForModel: (NeatMutator) -> Float = { neatMutator ->
            adjustedPopulationScore.getValue(neatMutator).adjustedFitness
        }
        speciationController.sortSpeciesByFitness(fitnessForModel)
        return modelScoreList
    }



}

fun List<FitnessModel<NeatMutator>>.toModelScores(adjustedFitness: AdjustedFitnessCalculation): List<ModelScore> {
    return map { fitnessModel ->
        ModelScore(fitnessModel.model, fitnessModel.score, adjustedFitness(fitnessModel))
    }
}
fun neatMutator(
    inputNumber: Int,
    outputNumber: Int,
    random: Random = Random,
    function: ActivationFunction = Identity
): NeatMutator {
    val simpleNeatMutator = simpleNeatMutator(listOf(), listOf())
    var nodeNumber = 0
    var innovation = 0
    repeat(inputNumber) {
        simpleNeatMutator.addNode(NodeGene(nodeNumber++, NodeType.Input, identity()))
    }
//    val hiddenNode = NodeGene(nodeNumber++, NodeType.Hidden)
//    simpleNeatMutator.addNode(hiddenNode)
    repeat(outputNumber) {
        simpleNeatMutator.addNode(NodeGene(nodeNumber++, NodeType.Output, function))
    }
    for (input in simpleNeatMutator.inputNodes) {
//        val weight = random.nextFloat();
        for (output in simpleNeatMutator.outputNodes) {
            val weight = random.nextFloat()
            simpleNeatMutator.addConnection(
                ConnectionGene(
                    input.node,
                    output.node,
                    weight,
                    true,
                    innovation++
                )
            )
        }
    }

    return simpleNeatMutator
}

