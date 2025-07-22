<template>
  <div>
    <v-row class="mb-4">
      <v-col>
        <v-btn
          prepend-icon="mdi-arrow-left"
          variant="text"
          @click="$router.push('/students')"
          class="mb-4"
        >
          Back to Students
        </v-btn>
        <h1 class="text-h4">Student Details</h1>
      </v-col>
    </v-row>

    <v-card v-if="!loading && student" max-width="600">
      <v-card-title class="text-h5 primary white--text">
        <v-icon class="mr-2">mdi-account</v-icon>
        {{ student.name }}
      </v-card-title>
      
      <v-card-text class="pt-4">
        <v-row>
          <v-col cols="12" md="6">
            <v-list-item class="px-0">
              <template v-slot:prepend>
                <v-icon color="primary">mdi-identifier</v-icon>
              </template>
              <v-list-item-title>Student ID</v-list-item-title>
              <v-list-item-subtitle>{{ student.id }}</v-list-item-subtitle>
            </v-list-item>
          </v-col>
          
          <v-col cols="12" md="6">
            <v-list-item class="px-0">
              <template v-slot:prepend>
                <v-icon color="primary">mdi-email</v-icon>
              </template>
              <v-list-item-title>Email</v-list-item-title>
              <v-list-item-subtitle>{{ student.email }}</v-list-item-subtitle>
            </v-list-item>
          </v-col>
          
          <v-col cols="12" md="6">
            <v-list-item class="px-0">
              <template v-slot:prepend>
                <v-icon color="primary">mdi-school</v-icon>
              </template>
              <v-list-item-title>Department</v-list-item-title>
              <v-list-item-subtitle>{{ student.department }}</v-list-item-subtitle>
            </v-list-item>
          </v-col>
          
          <v-col cols="12" md="6">
            <v-list-item class="px-0">
              <template v-slot:prepend>
                <v-icon color="primary">mdi-calendar</v-icon>
              </template>
              <v-list-item-title>Created At</v-list-item-title>
              <v-list-item-subtitle>{{ formatDate(student.createdAt) }}</v-list-item-subtitle>
            </v-list-item>
          </v-col>
          
          <v-col cols="12" md="6" v-if="student.updatedAt && student.updatedAt !== student.createdAt">
            <v-list-item class="px-0">
              <template v-slot:prepend>
                <v-icon color="primary">mdi-update</v-icon>
              </template>
              <v-list-item-title>Last Updated</v-list-item-title>
              <v-list-item-subtitle>{{ formatDate(student.updatedAt) }}</v-list-item-subtitle>
            </v-list-item>
          </v-col>
        </v-row>
      </v-card-text>
      
      <v-card-actions>
        <v-btn
          color="primary"
          prepend-icon="mdi-pencil"
          @click="$router.push(`/students/${student.id}/edit`)"
        >
          Edit Student
        </v-btn>
        <v-spacer></v-spacer>
        <v-btn
          color="error"
          prepend-icon="mdi-delete"
          @click="confirmDelete"
        >
          Delete Student
        </v-btn>
      </v-card-actions>
    </v-card>

    <!-- Loading state -->
    <v-card v-else-if="loading" max-width="600">
      <v-card-text class="text-center py-8">
        <v-progress-circular
          indeterminate
          color="primary"
          size="64"
        ></v-progress-circular>
        <p class="mt-4">Loading student details...</p>
      </v-card-text>
    </v-card>

    <!-- Error state -->
    <v-alert
      v-else
      type="error"
      class="mb-4"
    >
      Student not found or failed to load.
    </v-alert>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="500px">
      <v-card>
        <v-card-title class="text-h5">Confirm Delete</v-card-title>
        <v-card-text>
          Are you sure you want to delete student "{{ student?.name }}"?
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
import { useRoute, useRouter } from 'vue-router'
import studentService from '@/services/studentService'

export default {
  name: 'StudentDetail',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const showNotification = inject('showNotification')
    
    const student = ref(null)
    const loading = ref(false)
    const deleteDialog = ref(false)
    const deleting = ref(false)

    const loadStudent = async () => {
      loading.value = true
      try {
        student.value = await studentService.getStudent(route.params.id)
      } catch (error) {
        showNotification(error.message, 'error')
      } finally {
        loading.value = false
      }
    }

    const confirmDelete = () => {
      deleteDialog.value = true
    }

    const deleteStudent = async () => {
      if (!student.value) return

      deleting.value = true
      try {
        await studentService.deleteStudent(student.value.id)
        showNotification('Student deleted successfully', 'success')
        router.push('/students')
      } catch (error) {
        showNotification(error.message, 'error')
      } finally {
        deleting.value = false
        deleteDialog.value = false
      }
    }

    const formatDate = (dateString) => {
      if (!dateString) return ''
      return new Date(dateString).toLocaleString()
    }

    onMounted(() => {
      loadStudent()
    })

    return {
      student,
      loading,
      deleteDialog,
      deleting,
      confirmDelete,
      deleteStudent,
      formatDate
    }
  }
}
</script>