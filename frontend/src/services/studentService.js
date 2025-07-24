import axios from 'axios'
import loadingState from './loadingState'

// Generate a unique request ID
const generateRequestId = () => `req_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`

/**
 * Create axios instance with base configuration
 * The baseURL is set to '/api' which should be proxied to the backend server
 * in the Vite configuration (typically http://localhost:8080/api)
 */
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  timeout: 10000 // 10 second timeout
})

// Store for notification function that will be set from a component
let notifyFunction = null

/**
 * Set the notification function to be used by interceptors
 * @param {Function} notifyFn - Function to show notifications
 */
export const setNotificationFunction = (notifyFn) => {
  notifyFunction = notifyFn
}

/**
 * Show a notification if the notification function is available
 * @param {string} message - Message to display
 * @param {string} type - Notification type (success, error, info, warning)
 */
const notify = (message, type = 'error') => {
  if (notifyFunction) {
    notifyFunction(message, type)
  } else {
    console[type === 'error' ? 'error' : 'log'](message)
  }
}

/**
 * Extract resource information from request config
 * @param {Object} config - Axios request config
 * @returns {Object} Resource information
 */
const getResourceInfo = (config) => {
  const url = config.url || ''
  
  // Extract resource type and ID from URL
  let resource = 'global'
  let id = undefined
  
  if (url.includes('/students')) {
    resource = 'students'
    
    // Extract ID from URL if it's a specific student request
    const matches = url.match(/\/students\/(\d+)/)
    if (matches && matches[1]) {
      id = matches[1]
      resource = 'studentDetail'
    }
  }
  
  return { resource, id }
}

/**
 * Request interceptor for API calls
 * Manages loading states and adds common headers
 */
api.interceptors.request.use(
  (config) => {
    // Generate a unique request ID
    const requestId = generateRequestId()
    config.requestId = requestId
    
    // Extract resource information
    const { resource, id } = getResourceInfo(config)
    
    // Start loading state
    loadingState.startLoading(resource, id, requestId)
    
    // Add timestamp for cache busting on GET requests
    if (config.method?.toLowerCase() === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }
    
    // Add common headers for all requests
    config.headers = {
      ...config.headers,
      'X-Request-ID': requestId,
      'X-Client-Timestamp': new Date().toISOString()
    }
    
    // Add auth headers if available
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    return config
  },
  (error) => {
    // End loading state on request error
    loadingState.endLoading('global')
    console.error('Request interceptor error:', error)
    notify('Failed to send request. Please check your connection.', 'error')
    return Promise.reject(error)
  }
)

/**
 * Response interceptor for API calls
 * Handles global response processing and error handling
 */
api.interceptors.response.use(
  (response) => {
    // Extract resource information and request ID
    const { resource, id } = getResourceInfo(response.config)
    const requestId = response.config.requestId
    
    // End loading state
    loadingState.endLoading(resource, id, requestId)
    
    // Log successful responses in development environment
    if (process.env.NODE_ENV === 'development') {
      console.log(`API Success [${response.config.method?.toUpperCase()}] ${response.config.url}:`, response.data)
    }
    
    // Show success notifications for create, update, delete operations
    const method = response.config.method?.toLowerCase()
    if (['post', 'put', 'delete'].includes(method)) {
      let message = ''
      if (method === 'post') {
        message = 'Record created successfully'
      } else if (method === 'put') {
        message = 'Record updated successfully'
      } else if (method === 'delete') {
        message = 'Record deleted successfully'
      }
      
      if (message) {
        notify(message, 'success')
      }
    }
    
    return response
  },
  (error) => {
    // Extract resource information and request ID
    const { resource, id } = error.config ? getResourceInfo(error.config) : { resource: 'global', id: undefined }
    const requestId = error.config?.requestId
    
    // End loading state
    loadingState.endLoading(resource, id, requestId)
    
    // Handle global errors here
    const errorMessage = error.response?.data?.message || error.message
    const errorDetails = error.response?.data?.details || {}
    
    // Log detailed error information
    console.error('API Error:', {
      status: error.response?.status,
      message: errorMessage,
      details: errorDetails,
      url: error.config?.url,
      method: error.config?.method,
      requestId
    })

    // Handle specific error codes
    if (error.response) {
      switch (error.response.status) {
        case 401:
          notify('Unauthorized access. Please log in again.', 'error')
          // Redirect to login page if authentication is required
          // window.location.href = '/login'
          break
        case 403:
          notify('You do not have permission to perform this action.', 'error')
          break
        case 404:
          notify('The requested resource was not found.', 'error')
          break
        case 409:
          notify('A conflict occurred. This record may already exist.', 'error')
          break
        case 422:
          // Handle validation errors with field-specific messages
          if (errorDetails && typeof errorDetails === 'object') {
            const fieldErrors = Object.entries(errorDetails)
              .map(([field, message]) => `${field}: ${message}`)
              .join(', ')
            notify(`Validation failed: ${fieldErrors}`, 'error')
          } else {
            notify('Validation failed. Please check your input.', 'error')
          }
          break
        case 500:
          notify('An unexpected server error occurred. Please try again later.', 'error')
          break
        default:
          notify(`Error: ${errorMessage}`, 'error')
      }
    } else if (error.request) {
      // The request was made but no response was received
      notify('Network error. Please check your connection and try again.', 'error')
      
      // Implement automatic retry for network errors (optional)
      // if (error.config && !error.config.__isRetryRequest) {
      //   error.config.__isRetryRequest = true
      //   return api(error.config)
      // }
    } else {
      // Something happened in setting up the request
      notify('An error occurred while setting up the request.', 'error')
    }

    return Promise.reject(error)
  }
)

/**
 * StudentService class provides methods to interact with the student API endpoints
 * Implements all CRUD operations and additional functionality
 */
class StudentService {
  /**
   * Get all students from the API
   * @returns {Promise<Array>} Promise resolving to an array of student objects
   */
  async getAllStudents() {
    try {
      const response = await api.get('/students')
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to fetch students')
    }
  }

  /**
   * Get a specific student by ID
   * @param {number} id - The student ID
   * @returns {Promise<Object>} Promise resolving to a student object
   */
  async getStudent(id) {
    try {
      const response = await api.get(`/students/${id}`)
      return response.data
    } catch (error) {
      if (error.response?.status === 404) {
        throw new Error(`Student with ID ${id} not found`)
      }
      throw new Error(error.response?.data?.message || 'Failed to fetch student')
    }
  }

  /**
   * Get students by department
   * @param {string} department - The department name
   * @returns {Promise<Array>} Promise resolving to an array of student objects
   */
  async getStudentsByDepartment(department) {
    try {
      const response = await api.get(`/students/department/${encodeURIComponent(department)}`)
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || `Failed to fetch students from ${department} department`)
    }
  }

  /**
   * Create a new student
   * @param {Object} student - The student object with name, email, and department
   * @returns {Promise<Object>} Promise resolving to the created student object
   */
  async createStudent(student) {
    try {
      const response = await api.post('/students', student)
      return response.data
    } catch (error) {
      if (error.response?.status === 409) {
        throw new Error(`Student with email ${student.email} already exists`)
      } else if (error.response?.status === 400) {
        throw new Error(error.response?.data?.message || 'Invalid student data')
      }
      throw new Error(error.response?.data?.message || 'Failed to create student')
    }
  }

  /**
   * Update an existing student
   * @param {number} id - The student ID
   * @param {Object} student - The updated student object
   * @returns {Promise<Object>} Promise resolving to the updated student object
   */
  async updateStudent(id, student) {
    try {
      const response = await api.put(`/students/${id}`, student)
      return response.data
    } catch (error) {
      if (error.response?.status === 404) {
        throw new Error(`Student with ID ${id} not found`)
      } else if (error.response?.status === 409) {
        throw new Error(`Student with email ${student.email} already exists`)
      } else if (error.response?.status === 400) {
        throw new Error(error.response?.data?.message || 'Invalid student data')
      }
      throw new Error(error.response?.data?.message || 'Failed to update student')
    }
  }

  /**
   * Delete a student
   * @param {number} id - The student ID
   * @returns {Promise<boolean>} Promise resolving to true if deletion was successful
   */
  async deleteStudent(id) {
    try {
      await api.delete(`/students/${id}`)
      return true
    } catch (error) {
      if (error.response?.status === 404) {
        throw new Error(`Student with ID ${id} not found`)
      }
      throw new Error(error.response?.data?.message || 'Failed to delete student')
    }
  }
}

export default new StudentService()