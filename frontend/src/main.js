import { createApp } from 'vue'
import { createVuetify } from 'vuetify'
import 'vuetify/styles'
import '@mdi/font/css/materialdesignicons.css'

import App from './App.vue'
import router from './router'

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