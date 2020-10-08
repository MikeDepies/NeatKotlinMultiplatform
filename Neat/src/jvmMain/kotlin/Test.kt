import kotlin.math.roundToInt
import kotlin.random.Random

fun main() {
    val generations = 100
    val activationFunctions = listOf(sigmoidalTransferFunction, Identity)
    val mutationEntries = mutationDictionary(activationFunctions)
    val speciesScoreKeeper = SpeciesScoreKeeper()
    val speciesLineage = SpeciesLineage()
    val neat = neat(mutationEntries) {
        evaluationFunction = { population ->
            val inputOutput = setupEnvironment().map { generateQA -> generateQA() }
            evaluatePopulation(population, inputOutput)
        }
    }
    neat.generationFinishedHandlers += {
        val species = speciesScoreKeeper.bestSpecies()
        val modelScore = speciesScoreKeeper.getModelScore(species)
        println("$species - ${modelScore?.fitness}")
    }
    val simpleNeatExperiment = simpleNeatExperiment(Random(0), 0, 0, activationFunctions)
    val population = simpleNeatExperiment.generateInitialPopulation(100, 3, 1, sigmoidalTransferFunction)
    neat.process(generations, population, speciesScoreKeeper, speciesLineage, simpleNeatExperiment)

    speciesScoreKeeper.bestSpecies().let { species ->
        val toNetwork = speciesScoreKeeper.getModelScore(species)!!.neatMutator.toNetwork()

        println("===$species===")
        XORTruthTable().map { it() }.forEach {
            toNetwork.evaluate(it.first, true)
            println("$species ${toNetwork.output()} == ${it.second}")
            println("$species ${toNetwork.output().map { it.roundToInt().toFloat() }} == ${it.second}")
            println("species ${toNetwork.output().map { it.roundToInt().toFloat() } == it.second}")
        }
    }
}