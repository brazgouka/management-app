import Vue from 'vue'
import BootstrapVue from 'bootstrap-vue'
import App from './App.vue'

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(BootstrapVue)
Vue.config.productionTip = false


new Vue({
  render: h => h(App)
}).$mount('#app')

Vue.filter('two_digits', function (value) {
  if(value.toString().length <= 1)
  {
      return "0" + value.toString();
  }
  return value.toString();
});