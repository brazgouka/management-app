<template>
    <div>
        <b-tabs content-class="mt-3" fill>
            <b-tab title="Notes" active><Notes /></b-tab>
            <b-tab title="Finances"><Finances /></b-tab>
            <b-tab title="Timer"><Timer /></b-tab>
        </b-tabs>
    </div>
</template>

<script>
  import Finances from './tabs/Finances.vue'
  import Timer from './tabs/Timer.vue'
  import Notes from './tabs/Notes.vue'

  export default {
    components: {
      Finances,
      Timer,
      Notes
    }
  }
</script>