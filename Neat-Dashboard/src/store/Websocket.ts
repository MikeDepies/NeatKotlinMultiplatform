import type { ExperimentSession } from "@app/api/Experiment"
import { derived, Readable, writable } from "svelte/store"

let ws: WebSocket | undefined
let message = writable<string | undefined>(undefined, set => {
  console.log("starting up websocket connection")
  const newWs = new WebSocket("ws://localhost:8090/ws")
  newWs.onopen = (e) => {
    if (newWs)
      newWs.onmessage = (ev) => {
        set(ev.data)
      }
  }
  ws = newWs
  return () => {
    newWs?.close()
    ws = undefined
  }
})
export function wsListen<T extends {} = any>(): Messager<T> {
  return {
    read<RouteKey extends Extract<keyof T, string>>(topic: RouteKey) {
      const derivied = derived(message, ($message: string | undefined, set: (x: T[RouteKey]) => void) => {
        if ($message) {
          const data: SimpleMessage<T[RouteKey]> = JSON.parse($message)
          if (data.topic === topic) {
            set(data.data)
          }
        }
      })
      return derivied
    },
    readWithDefault<RouteKey extends Extract<keyof T, string>>(topic: RouteKey, value: T[RouteKey]) {
      const derivied = derived(message, ($message: string | undefined, set: (x: T[RouteKey]) => void) => {
        if ($message) {
          const data: SimpleMessage<T[RouteKey]> = JSON.parse($message)
          if (data.topic === topic) {
            set(data.data)
          }
        }
      }, value)
      return derivied
    },
    write<M>(topic: string, data: M) {
      send({
        topic,
        data
      })
    }
  }
}

type Messager<RouteMap> = {
  read: <RouteKey extends Extract<keyof RouteMap, string>> (topic: RouteKey) => Readable<RouteMap[RouteKey] | undefined>
  readWithDefault: <RouteKey extends Extract<keyof RouteMap, string>> (topic: RouteKey, value: any) => Readable<RouteMap[RouteKey]>
  write: <M> (topic: string, data: M) => void
}

function send<T>(message: SimpleMessage<T>) {
  console.log("is ws undefined? " + !ws)
  ws?.send(JSON.stringify(message))
}

type SimpleMessage<T> = {
  topic: string,
  data: T
}

function registerSession(session: ExperimentSession) {
  send({
    topic: "register",
    data: {
      sessionId: session.id
    }
  })
}

