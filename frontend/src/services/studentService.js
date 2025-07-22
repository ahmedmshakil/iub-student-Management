import axios from 'axios'

// Create axios instance with base configuration
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor
api.interceptors.request.use(
  (config) => {
    // Add any auth headers here if needed
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    // Handle global errors here
    console.error('API Error:', error.response?.data || error.message)
    return Promise.reject(error)
  }
)

class StudentService {
  async getAllStudents() {
    try {
      const response = await api.get('/students')
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to fetch students')
    }
  }

  async getStudent(id) {
    try {
      const response = await api.get(`/students/${id}`)
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to fetch student')
    }
  }

  async createStudent(student) {
    try {
      const response = await api.post('/students', student)
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to create student')
    }
  }

  async updateStudent(id, student) {
    try {
      const response = await api.put(`/students/${id}`, student)
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to update student')
    }
  }

  async deleteStudent(id) {
    try {
      await api.delete(`/students/${id}`)
      return true
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to delete student')
    }
  }
}

export default new StudentService()