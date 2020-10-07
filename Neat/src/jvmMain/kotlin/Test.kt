import kotlin.math.roundToInt

fun main() {
//    runWeightSummationExample()
//    runNodeCountExample()
    runNeatExample().let { (scoreKeeper, speciesLineage) ->


        scoreKeeper.bestSpecies().let { species ->
            val toNetwork = scoreKeeper.getModelScore(species)!!.neatMutator.toNetwork()

            println("===$species===")
            XORTruthTable().map { it() }.forEach {
                toNetwork.evaluate(it.first, true)
                println("$species ${toNetwork.output()} == ${it.second}")
                println("$species ${toNetwork.output().map { it.roundToInt().toFloat() }} == ${it.second}")
                println("species ${toNetwork.output().map { it.roundToInt().toFloat() } == it.second}")
            }
        }
    }
}