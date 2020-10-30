<script>
  import { fade } from "svelte/transition";
  import type { XorParametersUI } from "@app/api/Environment";
  import { createEventDispatcher } from "svelte";
  type EventMap = {
    startSimulation: undefined;
  };
  const dispatch = createEventDispatcher<EventMap>();
  export let parameters: XorParametersUI;
  export let environments: string[];
</script>

<div class="bg-white overflow-hidden shadow rounded-lg">
  <div class="border-b border-gray-200 px-4 py-5 sm:px-6">
    Train a network
    {parameters.populationSize}
  </div>
  <div class="px-4 py-5 sm:p-6">
    <div>
      <label
        for="location"
        class="block text-sm leading-5 font-medium text-gray-700">
        Select Environment
      </label>
      {#each environments as env}
        <select
          id="location"
          class="mt-1 form-select block w-full pl-3 pr-10 py-2 text-base
          leading-6 border-gray-300 focus:outline-none
          focus:shadow-outline-blue focus:border-blue-300 sm:text-sm
          sm:leading-5">
          <option selected>{env}</option>
        </select>
      {/each}
    </div>
    <!--  -->
    <div class="hidden sm:block">
      <div class="py-5">
        <div class="border-t border-gray-200" />
      </div>
    </div>
    <div class="mt-10 sm:mt-0">
      <div class="md:grid md:grid-cols-3 md:gap-6">
        <div class="md:col-span-1">
          <div class="px-4 sm:px-0">
            <h3 class="text-lg font-medium leading-6 text-gray-900">
              Basic Parameters
            </h3>
            <p class="mt-1 text-sm leading-5 text-gray-600">
              Set up basic parameters for the problem and solver.
            </p>
          </div>
        </div>
        <div class="mt-5 md:mt-0 md:col-span-2">
          <div class="shadow overflow-hidden sm:rounded-md">
            <div class="px-4 py-5 bg-white sm:p-6">
              <div class="grid grid-cols-6 gap-6">
                <div class="col-span-3 sm:col-span-3">
                  <label
                    for="generations"
                    class="block text-sm font-medium leading-5 text-gray-700">
                    Number Of Generations
                  </label>
                  <input
                    id="generations"
                    bind:value={parameters.generations}
                    class="mt-1 form-input block w-full py-2 px-3 border
                    border-gray-300 rounded-md shadow-sm focus:outline-none
                    focus:shadow-outline-blue focus:border-blue-300
                    transition duration-150 ease-in-out sm:text-sm
                    sm:leading-5" />
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label
                    for="population_size"
                    class="block text-sm font-medium leading-5 text-gray-700">
                    Population Size
                  </label>
                  <input
                    bind:value={parameters.populationSize}
                    id="population_size"
                    class="mt-1 form-input block w-full py-2 px-3 border
                    border-gray-300 rounded-md shadow-sm focus:outline-none
                    focus:shadow-outline-blue focus:border-blue-300
                    transition duration-150 ease-in-out sm:text-sm
                    sm:leading-5" />
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label
                    for="surival_threshold"
                    class="block text-sm font-medium leading-5 text-gray-700">
                    Survival Threshold
                  </label>
                  <input
                    bind:value={parameters.survivalThreshold}
                    id="surival_threshold"
                    class="mt-1 form-input block w-full py-2 px-3 border
                    border-gray-300 rounded-md shadow-sm focus:outline-none
                    focus:shadow-outline-blue focus:border-blue-300
                    transition duration-150 ease-in-out sm:text-sm
                    sm:leading-5" />
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label
                    for="mate_percent"
                    class="block text-sm font-medium leading-5 text-gray-700">
                    Mate Chance
                  </label>
                  <input
                    bind:value={parameters.mateChance}
                    id="mate_percent"
                    class="mt-1 form-input block w-full py-2 px-3 border
                    border-gray-300 rounded-md shadow-sm focus:outline-none
                    focus:shadow-outline-blue focus:border-blue-300
                    transition duration-150 ease-in-out sm:text-sm
                    sm:leading-5" />
                </div>

                <div class="col-span-6 sm:col-span-3">
                  <label
                    for="sharing_threshold"
                    class="block text-sm font-medium leading-5 text-gray-700">
                    Species Sharing Threshold
                  </label>
                  <input
                    bind:value={parameters.sharingThreshold}
                    id="sharing_threshold"
                    class="mt-1 form-input block w-full py-2 px-3 border
                    border-gray-300 rounded-md shadow-sm focus:outline-none
                    focus:shadow-outline-blue focus:border-blue-300
                    transition duration-150 ease-in-out sm:text-sm
                    sm:leading-5" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="hidden sm:block">
      <div class="py-5">
        <div class="border-t border-gray-200" />
      </div>
    </div>

    <div class="mt-10 sm:mt-0">
      <div class="md:grid md:grid-cols-3 md:gap-6">
        <div class="md:col-span-1">
          <div class="px-4 sm:px-0">
            <h3 class="text-lg font-medium leading-6 text-gray-900">
              Advanced
            </h3>
            <p class="mt-1 text-sm leading-5 text-gray-600">
              Decide on the activation functions to utilize and the mutation
              dictionary.
            </p>
          </div>
        </div>
        <div class="mt-5 md:mt-0 md:col-span-2">
          <div class="shadow overflow-hidden sm:rounded-md">
            <div class="px-4 py-5 bg-white sm:p-6">
              <fieldset>
                <legend class="text-base leading-6 font-medium text-gray-900">
                  Activation Functions
                </legend>
                <div class="mt-4">
                  {#if parameters != null}
                    {#each parameters.activationFunctions as activationFunction}
                      <div class="flex items-start">
                        <div class="flex items-center h-5">
                          <input
                            bind:checked={activationFunction.enabled}
                            id={activationFunction.name}
                            type="checkbox"
                            class="form-checkbox h-4 w-4 text-indigo-600
                            transition duration-150 ease-in-out" />
                        </div>
                        <div class="ml-3 text-sm leading-5">
                          <label
                            for={activationFunction.name}
                            class="font-medium text-gray-700">
                            {activationFunction.displayName}
                          </label>
                          <p class="text-gray-500">
                            {activationFunction.description}
                          </p>
                        </div>
                      </div>
                    {/each}
                  {/if}
                </div>
              </fieldset>
              <fieldset class="mt-6">
                <legend class="text-base leading-6 font-medium text-gray-900">
                  Mutation Dictionary
                </legend>
                <p class="text-sm leading-5 text-gray-500">
                  Here are the configured and available Mutations.
                </p>
                <div class="mt-4">
                  {#if parameters != null}
                    {#each parameters.mutations as mutation}
                      <label
                        for={mutation.name}
                        class="font-medium text-gray-700 hover:bg-gray-100 group">
                        <div
                          class="flex items-start  hover:bg-gray-100 group mb-2 p-2">
                          {#if mutation.enabled}
                            <div transition:fade class="text-sm leading-5">
                              <input
                                type="text"
                                class="form-input block w-16 sm:text-sm
                              sm:leading-5"
                                value={mutation.percentChance} />
                            </div>
                          {/if}
                          <div class="ml-3 flex items-center h-5">
                            <input
                              bind:checked={mutation.enabled}
                              id={mutation.name}
                              type="checkbox"
                              class="form-checkbox h-4 w-4 text-indigo-600
                            transition duration-150 ease-in-out" />
                          </div>

                          <div class="ml-3 text-sm leading-5 flex-grow ">
                            {mutation.displayName}

                            <p class="text-gray-500 group-hover:bg-gray-100">
                              {mutation.description}
                            </p>
                          </div>
                        </div>
                      </label>
                    {/each}
                  {/if}
                </div>
              </fieldset>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div class="px-4 py-3 bg-gray-50 text-right sm:px-6">
    <button
      on:click={() => dispatch('startSimulation')}
      class="py-2 px-4 border border-transparent text-sm font-medium
      rounded-md text-white bg-indigo-600 shadow-sm hover:bg-indigo-500
      focus:outline-none focus:shadow-outline-blue focus:bg-indigo-500
      active:bg-indigo-600 transition duration-150 ease-in-out">
      Run
    </button>
  </div>
</div>
