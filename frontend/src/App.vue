<template>
  <v-app>
    <!-- App Bar with navigation menu -->
    <v-app-bar
      color="primary"
      dark
      app
      elevation="2"
    >
      <!-- Menu button for mobile -->
      <v-app-bar-nav-icon 
        @click="drawer = !drawer"
        class="d-flex d-sm-none"
      ></v-app-bar-nav-icon>
      
      <v-app-bar-title class="d-flex align-center">
        <v-icon class="mr-2">mdi-school</v-icon>
        IUB Student Management
      </v-app-bar-title>
      
      <v-spacer></v-spacer>
      
      <!-- Desktop navigation menu -->
      <div class="d-none d-sm-flex">
        <v-btn
          v-for="item in navigationItems"
          :key="item.title"
          :to="item.path"
          :prepend-icon="item.icon"
          variant="text"
          class="mx-1"
        >
          {{ item.title }}
        </v-btn>
      </div>
      
      <!-- Action buttons -->
      <v-btn
        icon
        @click="$router.push('/students/create')"
        title="Add New Student"
      >
        <v-icon>mdi-account-plus</v-icon>
      </v-btn>
      
      <v-btn
        icon
        @click="toggleTheme"
        title="Toggle Theme"
      >
        <v-icon>{{ isDarkTheme ? 'mdi-weather-sunny' : 'mdi-weather-night' }}</v-icon>
      </v-btn>
    </v-app-bar>

    <!-- Responsive navigation drawer for mobile devices -->
    <v-navigation-drawer
      v-model="drawer"
      app
      temporary
      :width="280"
    >
      <v-list>
        <v-list-item
          prepend-avatar="https://cdn.vuetifyjs.com/images/logos/vuetify-logo-dark.png"
          title="IUB Student Management"
          subtitle="Admin Panel"
        ></v-list-item>
      </v-list>
      
      <v-divider></v-divider>
      
      <v-list density="compact" nav>
        <v-list-item
          v-for="item in navigationItems"
          :key="item.title"
          :prepend-icon="item.icon"
          :title="item.title"
          :subtitle="item.description"
          :to="item.path"
          :value="item.title"
          @click="drawer = false"
        ></v-list-item>
      </v-list>
      
      <template v-slot:append>
        <div class="pa-2">
          <v-btn
            block
            color="primary"
            prepend-icon="mdi-account-plus"
            to="/students/create"
            @click="drawer = false"
          >
            Add New Student
          </v-btn>
        </div>
      </template>
    </v-navigation-drawer>

    <!-- Main content area with router view -->
    <v-main>
      <v-container fluid>
        <BreadcrumbNav />
        <v-fade-transition mode="out-in">
          <router-view />
        </v-fade-transition>
      </v-container>
    </v-main>

    <!-- Footer -->
    <v-footer app class="d-flex flex-column">
      <div class="text-center">
        <span>&copy; {{ new Date().getFullYear() }} IUB Student Management System</span>
      </div>
    </v-footer>

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
import { ref, provide, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import BreadcrumbNav from '@/components/BreadcrumbNav.vue'
import { setNotificationFunction } from '@/services/studentService'
import loadingState from '@/services/loadingState'

export default {
  name: 'App',
  components: {
    BreadcrumbNav
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    // Navigation drawer state
    const drawer = ref(false)
    
    // Theme state
    const isDarkTheme = ref(false)
    
    // Navigation items based on routes
    const navigationItems = [
      {
        title: 'Students',
        path: '/students',
        icon: 'mdi-account-group',
        description: 'View and manage all student records'
      },
      {
        title: 'Add Student',
        path: '/students/create',
        icon: 'mdi-account-plus',
        description: 'Create a new student record'
      }
    ]
    
    // Notification system
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
    
    // Toggle between light and dark theme
    const toggleTheme = () => {
      isDarkTheme.value = !isDarkTheme.value
      // In a real app, you would apply the theme change to Vuetify
    }

    // Provide notification function to all child components
    provide('showNotification', showNotification)

    return {
      drawer,
      snackbar,
      showNotification,
      navigationItems,
      isDarkTheme,
      toggleTheme
    }
  }
}
</script>