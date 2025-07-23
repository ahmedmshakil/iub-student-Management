import { createRouter, createWebHistory } from 'vue-router'
import routes from './routes'

/**
 * Vue Router configuration for IUB Student Management System
 * 
 * Features:
 * - HTML5 history mode for clean URLs
 * - Navigation guards for route protection and validation
 * - Automatic page title updates
 * - Scroll behavior management
 * - Breadcrumb navigation support
 */
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // If the user used browser back/forward buttons, restore position
    if (savedPosition) {
      return savedPosition
    }
    
    // If the route has a hash, scroll to that element
    if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth'
      }
    }
    
    // Otherwise scroll to top
    return { top: 0 }
  }
})

// Global navigation guards
router.beforeEach((to, from, next) => {
  // Update document title based on route meta
  document.title = to.meta.title ? `IUB Student Management - ${to.meta.title}` : 'IUB Student Management'
  
  // Check for valid ID parameter in routes that require it
  if (to.params.id && to.name !== 'NotFound') {
    const id = parseInt(to.params.id)
    if (isNaN(id) || id <= 0) {
      // If ID is invalid, redirect to not found page
      next({ 
        name: 'NotFound',
        // Preserve the current path and query parameters for potential redirection
        params: { pathMatch: to.path.substring(1).split('/') },
        query: to.query,
        hash: to.hash
      })
      return
    }
  }
  
  // You can add authentication checks here if needed
  // For example:
  // if (to.meta.requiresAuth && !isAuthenticated()) {
  //   next({
  //     name: 'Login',
  //     query: { redirect: to.fullPath }
  //   })
  //   return
  // }
  
  next()
})

// After navigation hooks
router.afterEach((to, from) => {
  // Log navigation for analytics purposes (can be expanded)
  console.log(`Navigated from ${from.name || 'unknown'} to ${to.name || 'unknown'}`)
  
  // Additional post-navigation logic can be added here
})

export default router