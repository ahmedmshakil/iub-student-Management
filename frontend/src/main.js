import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { createVuetify } from 'vuetify'
import 'vuetify/styles'
import '@mdi/font/css/materialdesignicons.css'

import App from './App.vue'
import routes from './router/routes.js'

// Create router
const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Create Vuetify
const vuetify = createVuetify({
  theme: {
    defaultTheme: 'light'
  }
})

const app = createApp(App)

app.use(router)
app.use(vuetify)

app.mount('#app')