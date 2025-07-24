import { ref, readonly } from 'vue'

// Create a reactive loading state map to track loading states by request ID or type
const loadingStates = ref({
  global: false,
  students: false,
  studentDetail: {},  // Map of student IDs to loading states
  requests: {}        // Map of request IDs to loading states
})

// Counter for active requests
const activeRequests = ref(0)

/**
 * Loading state service to manage loading indicators across the application
 */
const loadingState = {
  /**
   * Get the readonly loading states object
   */
  states: readonly(loadingStates),
  
  /**
   * Check if there are any active requests
   */
  isLoading: () => activeRequests.value > 0,
  
  /**
   * Check if a specific resource is loading
   * @param {string} resource - The resource name (e.g., 'students', 'global')
   * @param {string|number} [id] - Optional ID for resource-specific loading state
   * @returns {boolean} - Whether the resource is loading
   */
  isResourceLoading: (resource, id) => {
    if (id !== undefined && resource === 'studentDetail') {
      return !!loadingStates.value.studentDetail[id]
    }
    return !!loadingStates.value[resource]
  },
  
  /**
   * Start loading state for a resource
   * @param {string} resource - The resource name
   * @param {string|number} [id] - Optional ID for resource-specific loading state
   * @param {string} [requestId] - Optional request ID for tracking specific requests
   */
  startLoading: (resource, id, requestId) => {
    activeRequests.value++
    
    // Set global loading state
    loadingStates.value.global = true
    
    // Set resource-specific loading state
    if (resource) {
      if (id !== undefined && resource === 'studentDetail') {
        loadingStates.value.studentDetail = { 
          ...loadingStates.value.studentDetail, 
          [id]: true 
        }
      } else {
        loadingStates.value[resource] = true
      }
    }
    
    // Track specific request if ID provided
    if (requestId) {
      loadingStates.value.requests[requestId] = true
    }
  },
  
  /**
   * End loading state for a resource
   * @param {string} resource - The resource name
   * @param {string|number} [id] - Optional ID for resource-specific loading state
   * @param {string} [requestId] - Optional request ID for tracking specific requests
   */
  endLoading: (resource, id, requestId) => {
    activeRequests.value = Math.max(0, activeRequests.value - 1)
    
    // Update global loading state
    loadingStates.value.global = activeRequests.value > 0
    
    // Update resource-specific loading state
    if (resource) {
      if (id !== undefined && resource === 'studentDetail') {
        loadingStates.value.studentDetail = { 
          ...loadingStates.value.studentDetail, 
          [id]: false 
        }
      } else {
        loadingStates.value[resource] = false
      }
    }
    
    // Clear specific request tracking
    if (requestId && loadingStates.value.requests[requestId]) {
      const { [requestId]: _, ...rest } = loadingStates.value.requests
      loadingStates.value.requests = rest
    }
  },
  
  /**
   * Reset all loading states
   */
  resetAll: () => {
    activeRequests.value = 0
    loadingStates.value = {
      global: false,
      students: false,
      studentDetail: {},
      requests: {}
    }
  }
}

export default loadingState