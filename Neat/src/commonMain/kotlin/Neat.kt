import kotlin.random.Random

typealias Generations = Int

typealias EnvironmentQuery = () -> EnvironmentEntryElement
typealias EnvironmentEntryElement = Pair<List<Float>, List<Float>>

data class FitnessModel<T>(val model: T, val score: Float)

fun <T> identity(): (T) -> T = { it }

typealias ReproductionStrategy = NeatExperiment.(SpeciationController, List<ModelScore>) -> List<NeatMutator>
typealias PopulationEvaluator = (List<NeatMutator>) -> List<FitnessModel<NeatMutator>>

fun SpeciationController.population() =
    speciesSet.flatMap { getSpeciesPopulation(it) }

data class Population(val nodeInnovation: Int, val connectionInnovation: Int, val population: List<NeatMutator>)
data class GenerationRules(
//    val activationFunctions: List<ActivationFunction>,
    val speciationController: SpeciationController,
    val adjustedFitness: AdjustedFitnessCalculation,
    val reproductionStrategy: ReproductionStrategy,
    val populationEvaluator: PopulationEvaluator
)

class Neat(
    private val generationRules: GenerationRules,
    private val newGenerationHandler: (SpeciesMap) -> Unit
) {
    fun process(
        times: Int,
        population: List<NeatMutator>,
        speciesScoreKeeper: SpeciesScoreKeeper,
        speciesLineage: SpeciesLineage,
        simpleNeatExperiment: NeatExperiment
    ) {
        val (speciationController, adjustedFitness, reproductionStrategy, populationEvaluator) = generationRules
        var currentPopulation = population
        speciationController.speciate(currentPopulation, speciesLineage, 0)
        repeat(times) { generation ->
            println("Generation $generation Population: ${currentPopulation.size} NumberOfSpecies: ${speciationController.speciesSet.size}")
            val modelScoreList =
                populationEvaluator(currentPopulation).toModelScores(adjustedFitness)
            sortModelsByAdjustedFitness(speciationController, modelScoreList)
            speciesScoreKeeper.updateScores(modelScoreList.map { speciationController.species(it.neatMutator) to it })
            val newPopulation = reproductionStrategy(simpleNeatExperiment, speciationController, modelScoreList)
            val speciesMap = speciationController.speciate(newPopulation, speciesLineage, generation)
            newGenerationHandler(speciesMap)
            currentPopulation = newPopulation
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
fun validatePopulation(currentPopulation: List<NeatMutator>) {
    currentPopulation.forEach { neatMutator ->
        validateNeatModel(neatMutator)
    }
}
fun validateNeatModel(neatMutator: NeatMutator) {
    neatMutator.connections.forEach { connectionGene ->
        if (neatMutator.nodes.none { connectionGene.inNode == it.node }
            || neatMutator.nodes.none { connectionGene.outNode == it.node }) {
            error("Couldn't satisfy $connectionGene from node pool ${neatMutator.nodes}")
        }
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

