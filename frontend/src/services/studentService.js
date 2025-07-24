import axios from 'axios'

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

/**
 * Request interceptor for API calls
 * Can be used to add authentication headers or other request modifications
 */
api.interceptors.request.use(
  (config) => {
    // Add any auth headers here if needed
    // For example: config.headers.Authorization = `Bearer ${localStorage.getItem('token')}`;
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * Response interceptor for API calls
 * Handles global response processing and error handling
 */
api.interceptors.response.use(
  (response) => {
    // You can transform the data here if needed
    return response
  },
  (error) => {
    // Handle global errors here
    const errorMessage = error.response?.data?.message || error.message
    console.error('API Error:', errorMessage)

    // You can handle specific error codes here
    if (error.response) {
      switch (error.response.status) {
        case 401:
          console.error('Unauthorized access')
          // Handle unauthorized (e.g., redirect to login)
          break
        case 403:
          console.error('Forbidden access')
          // Handle forbidden
          break
        case 404:
          console.error('Resource not found')
          // Handle not found
          break
        case 409:
          console.error('Conflict error')
          // Handle conflict (e.g., duplicate email)
          break
        case 500:
          console.error('Server error')
          // Handle server error
          break
      }
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