<template>
  <div>
    <v-row class="mb-4">
      <v-col>
        <h1 class="text-h4 mb-4">Students</h1>
      </v-col>
      <v-col cols="auto">
        <v-btn
          color="primary"
          prepend-icon="mdi-plus"
          @click="$router.push('/students/create')"
        >
          Add Student
        </v-btn>
      </v-col>
    </v-row>

    <v-card>
      <v-card-title>
        <v-text-field
          v-model="search"
          prepend-inner-icon="mdi-magnify"
          label="Search students..."
          single-line
          hide-details
          clearable
        ></v-text-field>
      </v-card-title>

      <v-data-table
        :headers="headers"
        :items="students"
        :search="search"
        :loading="loading"
        class="elevation-1"
      >
        <template v-slot:item.actions="{ item }">
          <v-btn
            icon="mdi-eye"
            size="small"
            color="info"
            @click="viewStudent(item.id)"
            class="mr-2"
          ></v-btn>
          <v-btn
            icon="mdi-pencil"
            size="small"
            color="primary"
            @click="editStudent(item.id)"
            class="mr-2"
          ></v-btn>
          <v-btn
            icon="mdi-delete"
            size="small"
            color="error"
            @click="confirmDelete(item)"
          ></v-btn>
        </template>

        <template v-slot:item.createdAt="{ item }">
          {{ formatDate(item.createdAt) }}
        </template>

        <template v-slot:no-data>
          <v-alert
            type="info"
            class="ma-4"
          >
            No students found. <router-link to="/students/create">Add the first student</router-link>
          </v-alert>
        </template>
      </v-data-table>
    </v-card>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="500px">
      <v-card>
        <v-card-title class="text-h5">Confirm Delete</v-card-title>
        <v-card-text>
          Are you sure you want to delete student "{{ selectedStudent?.name }}"?
          This action cannot be undone.
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="grey"
            variant="text"
            @click="deleteDialog = false"
          >
            Cancel
          </v-btn>
          <v-btn
            color="error"
            variant="text"
            @click="deleteStudent"
            :loading="deleting"
          >
            Delete
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import studentService from '@/services/studentService'

export default {
  name: 'StudentList',
  setup() {
    const students = ref([])
    const loading = ref(false)
    const search = ref('')
    const deleteDialog = ref(false)
    const selectedStudent = ref(null)
    const deleting = ref(false)
    const showNotification = inject('showNotification')

    const headers = [
      { title: 'ID', key: 'id', sortable: true },
      { title: 'Name', key: 'name', sortable: true },
      { title: 'Email', key: 'email', sortable: true },
      { title: 'Department', key: 'department', sortable: true },
      { title: 'Created At', key: 'createdAt', sortable: true },
      { title: 'Actions', key: 'actions', sortable: false, width: '150px' }
    ]

    const fetchStudents = async () => {
      loading.value = true
      try {
        students.value = await studentService.getAllStudents()
      } catch (error) {
        showNotification(error.message, 'error')
      } finally {
        loading.value = false
      }
    }

    const viewStudent = (id) => {
      router.push(`/students/${id}`)
    }

    const editStudent = (id) => {
      router.push(`/students/${id}/edit`)
    }

    const confirmDelete = (student) => {
      selectedStudent.value = student
      deleteDialog.value = true
    }

    const deleteStudent = async () => {
      if (!selectedStudent.value) return

      deleting.value = true
      try {
        await studentService.deleteStudent(selectedStudent.value.id)
        showNotification('Student deleted successfully', 'success')
        await fetchStudents() // Refresh the list
        deleteDialog.value = false
        selectedStudent.value = null
      } catch (error) {
        showNotification(error.message, 'error')
      } finally {
        deleting.value = false
      }
    }

    const formatDate = (dateString) => {
      if (!dateString) return ''
      return new Date(dateString).toLocaleDateString()
    }

    onMounted(() => {
      fetchStudents()
    })

    return {
      students,
      loading,
      search,
      headers,
      deleteDialog,
      selectedStudent,
      deleting,
      viewStudent,
      editStudent,
      confirmDelete,
      deleteStudent,
      formatDate
    }
  }
}
</script>