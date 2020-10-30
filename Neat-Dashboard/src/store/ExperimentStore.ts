import { writable } from "svelte/store"
import { wsListen } from "./Websocket"

export type ExperimentSession = { sessionId: number }
export type ExperimentData = {}
let experimentMap = new Map<ExperimentSession, ExperimentData>()
export let activeExperiment = writable<ExperimentSession | undefined>(undefined)
export function startExperiment(experimentSession: ExperimentSession) {
  if (!experimentMap.has(experimentSession)) {
    wsListen<any>().write("start", ({}))
    activeExperiment.set(experimentSession)
  }
}

export function getExperiment(experimentSession: ExperimentSession) {
  return experimentMap.get(experimentSession)
}
