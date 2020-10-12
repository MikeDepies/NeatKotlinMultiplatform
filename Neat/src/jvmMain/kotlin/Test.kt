import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import neat.*
import neat.mutation.*
import kotlin.math.roundToInt
import kotlin.random.Random

fun main() {
    server()

}

fun Application.main() {
    val experimentSessionManager = ExperimentSessionManager()
    install(CORS)
    {
        method(HttpMethod.Options)
        header(HttpHeaders.XForwardedProto)
        anyHost()
        host("my-host")
        // host("my-host:80")
        // host("my-host", subDomains = listOf("www"))
        // host("my-host", schemes = listOf("http", "https"))
        allowCredentials = true
        allowNonSimpleContentTypes = true
//        maxAge = Duration.ofDays(1)
    }
//    serialization()

    val mutationMap = mapOf(
        "mutateConnections" to mutateConnections,
        "mutateAddNode" to mutateAddNode,
        "mutateAddConnection" to mutateAddConnection,
        "mutatePerturbBiasConnections" to mutatePerturbBiasConnections(),
        "mutateToggleConnection" to mutateToggleConnection,
        "mutateNodeActivationFunction" to mutateNodeActivationFunction(),
    )
    val mutations = listOf(
        MutationParameter("mutateConnections", "Mutate Connections", "Mutate the connections nodes.", .8f, true),
        MutationParameter(
            "mutateAddNode",
            "Mutate Add Node",
            "A mutation that takes a random connection and splices a node inbetween the connection.",
            .4f, true
        ),
        MutationParameter(
            "mutateAddConnection",
            "Mutate Add Connection",
            "A mutation that connects two unconnected nodes.",
            .4f, true
        ),
        MutationParameter(
            "mutatePerturbBiasConnections",
            "Mutate Perturb Bias Connections",
            "A mutation that randomly nudges each bias connection a small amount.",
            .1f, true
        ),
        MutationParameter(
            "mutateToggleConnection",
            "Mutate Toggle Connection",
            "A mutation that selects a random connection and switches and toggles its enabled state.",
            .11f, true
        ),
        MutationParameter(
            "mutateNodeActivationFunction",
            "Mutate Activation Function",
            "A mutation that causes a random node to be selected and a (new) random mutation to be chosen.",
            .1f, true
        )
    )
    val activationParameterMap = listOf(
        ActivationFunctionParameter(
            "identity",
            "Identity",
            "A simple Identity function - acts as a stand in for no transformation",
            true
        ), ActivationFunctionParameter(
            "sigmoidal",
            "Sigmoidal",
            "A Sigmoid function of the form: 1 / (1 + e^(-4.9 * x)",
            true
        )
    ).toMap { it.name }
    
    launch {

    }
    routing {
        get("/") {
            call.respondText("Hello World!", ContentType.Text.Html)
        }
        post("/start") {
            val experimentSession = experimentSessionManager.newSession()
            val experimentFactory = ExperimentFactory()
            experimentFactory.newExperimentTask(experimentSession)
            call.respond(Json.encodeToString(experimentSession))
        }
        post("/configuration") {
            val xorExperimentParameters = XorExperimentParameters(
                generations = 10,
                populationSize = 100,
                sharingThreshold = 3f,
                mateChance = .4f,
                survivalThreshold = .6f,
                activationFunctions = baseActivationFunctions().map { activationParameterMap.getValue(it.name) },
                mutations = mutations
            )
            call.respond(Json.encodeToString(xorExperimentParameters))
        }
    }
}

class ExperimentFactory {
    suspend fun newExperimentTask(experimentSession: ExperimentSession) {
        TODO("Not yet implemented")
    }

}

@Serializable
data class XorExperimentParameters(
    val generations: Int,
    val populationSize: Int,
    val sharingThreshold: Float,
    val mateChance: Float,
    val survivalThreshold: Float,
    val activationFunctions: List<ActivationFunctionParameter>,
    val mutations: List<MutationParameter>
)

@Serializable
data class ActivationFunctionParameter(
    val name: String,
    val displayName: String,
    val description: String,
    val enabled: Boolean
)

@Serializable
data class MutationParameter(
    val name: String,
    val displayName: String,
    val description: String,
    val percentChance: Float,
    val enabled: Boolean
)

@Serializable
data class ExperimentSession(val id: Int)
class ExperimentSessionManager {
    var session = 0
    fun newSession() = ExperimentSession(nextSession())

    private fun nextSession(): Int {
        return session++
    }
}

fun server() {
    embeddedServer(Netty, port = 8090, module = Application::main).start(wait = true)
}


fun sampleProcess(
    generations: Int,
    activationFunctions: List<ActivationGene>,
    mutationEntries: List<MutationEntry>,
    sharingThreshold: Float,
    populationSize: Int
) {
//    val nodeGeneSerializer = NodeGeneSerializer(activationFunctions.toMap { it.name })
    val speciesScoreKeeper = SpeciesScoreKeeper()
    val speciesLineage = SpeciesLineage()
    val neat = neat(mutationEntries) {
        evaluationFunction = { population ->
            val inputOutput = XORTruthTable().map { generateQA -> generateQA() }
            evaluatePopulation(population, inputOutput)
        }
        sharingFunction = shFunction(sharingThreshold)
    }
    neat.generationFinishedHandlers += {
        val species = speciesScoreKeeper.bestSpecies()
        val modelScore = speciesScoreKeeper.getModelScore(species)
        println("$species - ${modelScore?.fitness}")
    }
    val simpleNeatExperiment = simpleNeatExperiment(Random(0), 0, 0, activationFunctions)
    val population = simpleNeatExperiment.generateInitialPopulation(populationSize, 3, 1, Activation.sigmoidal)
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