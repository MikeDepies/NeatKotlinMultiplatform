import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import neat.*
import neat.model.ConnectionGene
import neat.model.NeatMutator
import neat.model.NodeGene
import neat.model.NodeType
import kotlin.math.absoluteValue
import kotlin.random.Random

fun main() {
    server()
}

fun Application.main() {
    install(CORS)
    {
        method(HttpMethod.Options)
        header(HttpHeaders.XForwardedProto)
        anyHost()
//        host("my-host")
        // host("my-host:80")
        // host("my-host", subDomains = listOf("www"))
        // host("my-host", schemes = listOf("http", "https"))
        allowCredentials = true
        allowNonSimpleContentTypes = true
    }
    install(WebSockets)
    val channel = Channel<ExperimentSession>(Channel.UNLIMITED)
    val experimentFactory = ExperimentFactory(channel, ExperimentSessionManager())
    val mutations = sampleMutationParameters()
    val activationParameterMap = sampleActivationParameters()
    val activationFunctionMap = sampleActivationFunctions()
    val sessionWebsocketsManager = SessionWebSocketsManager()
    taskExecutor(channel, experimentFactory, activationFunctionMap, sessionWebsocketsManager)

    routing {
        post("/start") {

            val experimentRunDefinition = Json.decodeFromString<ExperimentRunDefinition>(call.receiveText())
            val newExperimentTask = experimentFactory.newExperimentTask(experimentRunDefinition)
            call.respond(Json.encodeToString(newExperimentTask))
            delay(1000)
            channel.send(newExperimentTask)
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

        fun onClose(defaultWebSocketServerSession: DefaultWebSocketServerSession) {
            sessionWebsocketsManager.unregisterSession(defaultWebSocketServerSession)
        }

        fun onError(defaultWebSocketServerSession: DefaultWebSocketServerSession, e: Exception) {
            log.error("failure on socket", e)
        }
        handleFrame(::onError, ::onClose) {
            when (it.topic) {
                "register" -> {
                    val (sessionId) = Json.decodeFromJsonElement<RegisterSession>(it.data)
                    sessionWebsocketsManager.registerSession(this, sessionId)
                    launch {
                        log.info("Session id: $sessionId")
                        sessionWebsocketsManager.sendMessage(ExperimentSession(sessionId), "register", true)
                    }
                }
            }
        }
    }
}


class SessionWebSocketsManager() {
    val sessionMap = mutableMapOf<ExperimentSession, MutableList<DefaultWebSocketServerSession>>()
    private val sessionLookupMap = mutableMapOf<DefaultWebSocketServerSession, MutableList<ExperimentSession>>()
    fun registerSession(defaultWebSocketServerSession: DefaultWebSocketServerSession, sessionId: Int) {
        println("start register session $sessionId")
        val experimentSession = ExperimentSession(sessionId)
        if (sessionMap.containsKey(experimentSession)) {
            println("remove to session set")
            sessionMap[experimentSession]!! += defaultWebSocketServerSession
            sessionLookupMap[defaultWebSocketServerSession]!! += experimentSession
        } else {
            println("added to session set")
            sessionMap[experimentSession] = mutableListOf(defaultWebSocketServerSession)
            sessionLookupMap[defaultWebSocketServerSession] = mutableListOf(experimentSession)
        }
        println("end register session")
    }

    fun unregisterSession(defaultWebSocketServerSession: DefaultWebSocketServerSession) {
        if (sessionLookupMap.containsKey(defaultWebSocketServerSession)) {
            sessionLookupMap.getValue(defaultWebSocketServerSession).forEach {
                sessionMap.getValue(it).remove(defaultWebSocketServerSession)
            }
            sessionLookupMap.remove(defaultWebSocketServerSession)
        }
    }

    suspend inline fun <reified T> sendMessage(session: ExperimentSession, topic: String, data: T) {
        val listeners = sessionMap[session]
        println("searched for $session for $topic found ${listeners?.isNotEmpty() ?: false}")
        if (listeners == null)
            println(sessionMap)
        listeners?.let { clients ->
            println("sending topic: $topic")
            val message = SimpleMessage(topic, Json.encodeToJsonElement(data))
            clients.forEach {
                try {
                    println("sent $it")
                    it.send(Json.encodeToString(SimpleMessage.serializer(), message))
                }
                catch(e : Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

fun Routing.handleFrame(
    onError: DefaultWebSocketServerSession.(e: Exception) -> Unit,
    onClose: DefaultWebSocketServerSession.() -> Unit,
    block: DefaultWebSocketServerSession.(SimpleMessage) -> Unit
) = webSocket("/ws") {
    println("New connection! $this")
    try {
        for (frame in incoming) {
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    block(Json.decodeFromString(SimpleMessage.serializer(), text))
                }
            }
        }
    } catch (e: Exception) {
        onError(this, e)
    } finally {
        onClose(this)
        println("close connection! $this")
    }
}

@Serializable
data class SimpleMessage(val topic: String, val data: JsonElement)

@Serializable
data class RegisterSession(val sessionId: Int)
typealias InputOutputPair = Pair<List<Float>, List<Float>>

fun scoreNetwork(
    inputOutput: List<InputOutputPair>,
    network: ActivatableNetwork,
    useBias: Boolean
): Float {
    return inputOutput.map { (input, output) ->
        network.evaluate(input, useBias)
        network.output().zip(output).map { 1f - (it.first - it.second).absoluteValue }.sum()
    }.sum()
}

fun Application.taskExecutor(
    channel: Channel<ExperimentSession>,
    experimentFactory: ExperimentFactory,
    activationFunctionMap: Map<String, ActivationGene>,
    sessionWebsocketsManager: SessionWebSocketsManager
) {
    val mutationMap = mutationMap()
    launch(Dispatchers.IO) {
        fun input(inputSize: Int, useBoolean: Boolean) = inputSize + if (useBoolean) 1 else 0
        for (experiment in channel) {
            println("starting new experiment")
            sessionWebsocketsManager.sendMessage(experiment, "start", "starting")
            val (generations, populationSize, sharingThreshold, mateChance,
                survivalThreshold, activationFunctionDefinitions, mutationDefinitions,
                randomSeed, useBias) = experimentFactory.getDefinitionForSession(experiment)
            val inputSize = 2
            val outputSize = 1
            val mutationEntries = createMutationDictionary(mutationMap, mutationDefinitions)
            val activationFunctions = createActivationFunctions(activationFunctionMap, activationFunctionDefinitions)
            val speciesScoreKeeper = SpeciesScoreKeeper()
            val speciesLineage = SpeciesLineage()
            val neat = neat(mutationEntries) {
                evaluationFunction = { population ->
                    population.map { neatMutator ->
                        val score = scoreNetwork(XORTruthTable2(), neatMutator.toNetwork(), useBias)
                        FitnessModel(neatMutator, score)
                    }
                }
                sharingFunction = shFunction(sharingThreshold)
                reproductionStrategy = weightedReproduction(mutationEntries, mateChance, survivalThreshold)
            }
            val simpleNeatExperiment = simpleNeatExperiment(Random(randomSeed), 0, 0, activationFunctions)
            val population = simpleNeatExperiment.generateInitialPopulation(
                populationSize,
                input(inputSize, useBias),
                outputSize,
                Activation.sigmoidal
            )
            sessionWebsocketsManager.sendMessage(experiment, "initialPopulation", population.map { it.toOrganismDNA() })
            println("finished")
//            neat.process(generations, population, speciesScoreKeeper, speciesLineage, simpleNeatExperiment)
        }
    }
}

//data class OrganismFitnessScore
@Serializable
data class OrganismDNA(val nodes: List<OrganismNodeGene>, val connections: List<OrganismConnectionGene>)

@Serializable
data class OrganismNodeGene(val node: Int, val nodeType: NodeType, val activationFunction: String)

fun NodeGene.toOrganismNodeGene() = OrganismNodeGene(node, nodeType, activationFunction.name)
fun ConnectionGene.toOrganismConnectionGene() = OrganismConnectionGene(inNode, outNode, weight, enabled, innovation)
fun NeatMutator.toOrganismDNA() =
    OrganismDNA(nodes.map { it.toOrganismNodeGene() }, connections.map { it.toOrganismConnectionGene() })

@Serializable
data class OrganismConnectionGene(
    val inNode: Int,
    val outNode: Int,
    val weight: Float,
    val enabled: Boolean,
    val innovation: Int
)

data class GenerationFinished(val generation: Int)

interface ExperimentWebsocketManager {
//    send()
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

    val simpleNeatExperiment = simpleNeatExperiment(Random(0), 0, 0, activationFunctions)
    val population = simpleNeatExperiment.generateInitialPopulation(populationSize, 3, 1, Activation.sigmoidal)
//    neat.process(generations, population, speciesScoreKeeper, speciesLineage, simpleNeatExperiment)

    /*speciesScoreKeeper.bestSpecies().let { species ->
        val toNetwork = speciesScoreKeeper.getModelScore(species)!!.neatMutator.toNetwork()

        println("===$species===")
        XORTruthTable().map { it() }.forEach {
            toNetwork.evaluate(it.first, true)
            println("$species ${toNetwork.output()} == ${it.second}")
            println("$species ${toNetwork.output().map { it.roundToInt().toFloat() }} == ${it.second}")
            println("species ${toNetwork.output().map { it.roundToInt().toFloat() } == it.second}")
        }
    }*/
}

class NNActivity(val speciesLineage: SpeciesLineage, val speciesScoreKeeper: SpeciesScoreKeeper, val neat: Neat) {

}