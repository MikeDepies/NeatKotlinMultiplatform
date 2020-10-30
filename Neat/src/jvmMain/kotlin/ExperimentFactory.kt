import kotlinx.coroutines.channels.Channel

class ExperimentFactory(
    val channel: Channel<ExperimentSession>,
    val experimentSessionManager: ExperimentSessionManager
) {
    private val mutableMap = mutableMapOf<ExperimentSession, ExperimentRunDefinition>()
    fun newExperimentTask(experimentSession: ExperimentRunDefinition): ExperimentSession {
        return experimentSessionManager.newSession().also {
            mutableMap[it] = experimentSession
        }
    }

    fun getDefinitionForSession(experimentSession: ExperimentSession): ExperimentRunDefinition {
        return mutableMap.getValue(experimentSession)
    }

}