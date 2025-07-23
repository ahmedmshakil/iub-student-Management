<template>
  <v-breadcrumbs
    v-if="breadcrumbs.length > 0"
    :items="breadcrumbs"
    class="pa-0 mb-4"
  >
    <template v-slot:divider>
      <v-icon icon="mdi-chevron-right"></v-icon>
    </template>
    <template v-slot:title="{ item }">
      <v-breadcrumbs-item
        :disabled="item.disabled"
        :to="item.href"
      >
        <v-icon v-if="item.icon" size="small" class="mr-1">{{ item.icon }}</v-icon>
        {{ item.text }}
      </v-breadcrumbs-item>
    </template>
  </v-breadcrumbs>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

/**
 * Computed property that generates the breadcrumb navigation items
 * based on the current route's meta information.
 * 
 * Features:
 * - Processes function-based hrefs with current route context
 * - Adds icons to breadcrumb items when available
 * - Handles disabled state for current/last item
 */
const breadcrumbs = computed(() => {
  // Skip breadcrumbs if explicitly disabled in route meta
  if (route.meta.skipBreadcrumb) {
    return [];
  }
  
  const breadcrumbData = route.meta.breadcrumb || [];
  
  return breadcrumbData.map(item => {
    // If href is a function, call it with the current route
    const href = typeof item.href === 'function' ? item.href(route) : item.href;
    
    // Add icon from route meta if available and not explicitly set on breadcrumb item
    const icon = item.icon || (
      router.hasRoute(item.text) ? 
        router.getRoutes().find(r => r.name === item.text)?.meta?.icon : 
        undefined
    );
    
    return {
      ...item,
      href,
      icon
    };
  });
});
</script>

<style scoped>
.v-breadcrumbs {
  font-size: 0.875rem;
}
</style>