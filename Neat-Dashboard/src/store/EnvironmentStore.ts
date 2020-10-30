import type { XorParametersUI } from "@app/api/Environment"
import { derived, Readable, writable } from "svelte/store"
let environment = writable("Xor")
let defaults: XorParametersUI = {
  activationFunctions: [],
  generations: 10,
  mateChance: 0.4,
  mutations: [],
  populationSize: 10,
  sharingThreshold: 3,
  survivalThreshold: 0.7,
}
export let parameters: Readable<XorParametersUI> = derived(environment, ($environment, set) => {
  getParameters($environment, set)
}, defaults)

export async function getParameters(env: string, set: (value: XorParametersUI) => void) {
  let response = await fetch("http://localhost:8090/configuration?env=" + env, {
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    method: "POST",
  })
  const newParameter: XorParametersUI = await response.json()
  set(newParameter)
}