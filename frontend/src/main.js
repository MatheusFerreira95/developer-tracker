import 'roboto-fontface/css/roboto/roboto-fontface.css';
import Vue from 'vue';
import App from './App.vue';
import './plugins/vuetify';
import router from "./router/";
import VueHtmlToPaper from 'vue-html-to-paper';

Vue.config.productionTip = false;
Vue.use(VueHtmlToPaper);

new Vue({
  router,
  render: h => h(App)
}).$mount("#app");
