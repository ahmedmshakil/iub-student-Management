<template>
  <v-container class="text-center">
    <v-row class="mt-12">
      <v-col cols="12">
        <h1 class="text-h2 mb-6">404</h1>
        <h2 class="text-h4 mb-6">Page Not Found</h2>
        <p class="text-body-1 mb-6">The page you are looking for does not exist or has been moved.</p>
        <v-btn
          color="primary"
          to="/students"
          prepend-icon="mdi-arrow-left"
        >
          Return to Student List
        </v-btn>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
// No additional script needed for this simple component
</script>