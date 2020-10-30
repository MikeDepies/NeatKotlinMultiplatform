import { derived, Readable, writable } from "svelte/store"

let ws: WebSocket | undefined
let message = writable<string | undefined>(undefined, set => {
  console.log("starting up websocket connection")
  const newWs = new WebSocket("ws://localhost:8090/ws")
  newWs.onopen = (e) => {
    if (newWs)
      newWs.onmessage = (ev) => {
        // console.log(ev.data)
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
  // let derivedMap = new Map<string, Readable<any>>()

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
type test<EventMap extends {} = any> = <EventKey extends Extract<keyof EventMap, string>>(type: EventKey, detail?: EventMap[EventKey]) => void
export declare function createEventDispatcher<EventMap extends {} = any>(): test<EventMap>
type WebsocketRouteMap<RouteMap extends {} = any> = {
  [P in Extract<keyof RouteMap, string>]: Readable<RouteMap[P]>
}
type WebsocketRouteMapFunc<RouteMap extends {} = any> = {
  read: <EventKey extends Extract<keyof RouteMap, string>> (topic: EventKey) => Readable<RouteMap[EventKey]>
}

export declare function wsTest<EventMap extends {} = any>(): WebsocketRouteMap<EventMap>


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

type ExperimentSession = {
  id: number
}

/*
Create interface/types that map topic : data structure.
Then the user can call wsListen<RouteDataMap>() and have access to reactive states at the end.

i.e.
  const {sessionId} = wsListen<RegisterSessionRoute>()
  console.log(`seseion ${$sessionId} registered`)

  so far I've conflated reading and sending and we still don't have a write api
*/
type RegisterSessionRoute = {
  register: {
    sessionId: number
  }
}

export type InitialPopulationRoute = {
  initialPopulation: OrganismDNA[]
}

export type OrganismDNA = {
  genes: GeneDNA[]
  connections: ConnectionDNA[]
}

export type GeneDNA = {
  node: number,
  nodeType: string,
  activationFunction: string
}

export type ConnectionDNA = {
  inNode: number,
  outNode: number,
  weight: number,
  enabled: boolean,
  innovation: number,
}
