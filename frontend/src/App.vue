<template>
  <v-app>
    <v-app-bar
      color="primary"
      dark
      app
    >
      <v-app-bar-title>
        <v-icon class="mr-2">mdi-school</v-icon>
        IUB Student Management
      </v-app-bar-title>
      
      <v-spacer></v-spacer>
      
      <v-btn
        icon
        @click="$router.push('/students')"
      >
        <v-icon>mdi-home</v-icon>
      </v-btn>
    </v-app-bar>

    <v-navigation-drawer
      v-model="drawer"
      app
      temporary
    >
      <v-list>
        <v-list-item
          prepend-icon="mdi-account-group"
          title="Students"
          @click="$router.push('/students')"
        ></v-list-item>
        <v-list-item
          prepend-icon="mdi-account-plus"
          title="Add Student"
          @click="$router.push('/students/create')"
        ></v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-main>
      <v-container fluid>
        <BreadcrumbNav />
        <router-view />
      </v-container>
    </v-main>

    <!-- Global snackbar for notifications -->
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      :timeout="snackbar.timeout"
      top
    >
      {{ snackbar.message }}
      <template v-slot:actions>
        <v-btn
          color="white"
          variant="text"
          @click="snackbar.show = false"
        >
          Close
        </v-btn>
      </template>
    </v-snackbar>
  </v-app>
</template>

<script>
import { ref, provide } from 'vue'

export default {
  name: 'App',
  setup() {
    const drawer = ref(false)
    const snackbar = ref({
      show: false,
      message: '',
      color: 'success',
      timeout: 4000
    })

    const showNotification = (message, color = 'success') => {
      snackbar.value = {
        show: true,
        message,
        color,
        timeout: 4000
      }
    }

    // Provide notification function to all child components
    provide('showNotification', showNotification)

    return {
      drawer,
      snackbar,
      showNotification
    }
  }
}
</script>