<template>
  <div id="app">
    <Header />
    <Tabs />
  </div>
</template>

<script>
  import Header from './components/Header.vue'
  import Tabs from './components/Tabs.vue'

  export default {
    name: 'app',
    components: {
      Header,
      Tabs
    }
  }
</script>

<style>
  #app {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
    margin-top: 60px;
  }
</style>
