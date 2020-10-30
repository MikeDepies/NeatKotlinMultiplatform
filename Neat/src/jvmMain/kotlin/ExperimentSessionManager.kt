class ExperimentSessionManager {
    var session = 0
    fun newSession() = ExperimentSession(nextSession())

    private fun nextSession(): Int {
        return session++
    }
}