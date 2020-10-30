<script>
	import { wsListen } from "./store/Websocket";
	import { parameters } from "./store/EnvironmentStore";
	import type { XorParameters, XorParametersUI } from "./api/Environment";
	import type { InitialPopulationRoute } from "./store/Websocket";
	import SetupEnvironment from "@components/SetupEnvironment.svelte";

	import {
		activeExperiment,
		startExperiment,
		getExperiment,
	} from "./store/ExperimentStore";
	type RegisterSessionRoute = {
		register: boolean;
	};
	type StartExperimentRoute = {
		start: string;
	};
	let { write, read, readWithDefault } = wsListen<
		StartExperimentRoute & RegisterSessionRoute & InitialPopulationRoute
	>();
	let start = read("start");
	let register = read("register");
	let initialPopulation = readWithDefault("initialPopulation", []);

	function buildExperimentConfig(): XorParameters {
		return {
			generations: $parameters.generations,
			mateChance: $parameters.mateChance,
			populationSize: $parameters.populationSize,
			sharingThreshold: $parameters.sharingThreshold,
			survivalThreshold: $parameters.survivalThreshold,
			activationFunctions: $parameters.activationFunctions
				.filter((activation) => activation.enabled)
				.map((activation) => ({ name: activation.name })),
			mutations: $parameters.mutations
				.filter((mutation) => mutation.enabled)
				.map((mutation) => ({
					name: mutation.name,
					percentChance: mutation.percentChance,
				})),
		};
	}
	async function startSimulation() {
		let response = await fetch("http://localhost:8090/start", {
			headers: {
				Accept: "application/json",
				"Content-Type": "application/json",
			},
			method: "POST",
			body: JSON.stringify(buildExperimentConfig()),
		});
		let sessionId: { sessionId: number } = await response.json();
		write("register", sessionId);
		startExperiment(sessionId);
		// parameters = await response.json();
	}

	let environments = ["XOR"];
</script>

<div class="max-w-7xl mx-auto sm:px-6 lg:px-8">
	<div class="max-w-3xl mx-auto">
		<!-- { $parameters.populationSize } -->
		{#if $activeExperiment}
			<div>
				{$activeExperiment?.sessionId}
				<ul>
					{#each $initialPopulation as organism}
						<li>{organism.connections.length}</li>
					{/each}
				</ul>
			</div>
		{:else}
			<SetupEnvironment
				{environments}
				parameters={$parameters}
				on:startSimulation={startSimulation} />
		{/if}
	</div>
</div>
