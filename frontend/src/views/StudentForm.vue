<template>
  <div>
    <v-row class="mb-4">
      <v-col>
        <h1 class="text-h4 mb-4">{{ isEdit ? 'Edit Student' : 'Add Student' }}</h1>
      </v-col>
    </v-row>

    <v-card max-width="600">
      <v-card-text>
        <v-form ref="form" v-model="valid" @submit.prevent="submitForm">
          <v-text-field
            v-model="student.name"
            label="Full Name"
            :rules="nameRules"
            required
            prepend-icon="mdi-account"
            variant="outlined"
            class="mb-3"
          ></v-text-field>

          <v-text-field
            v-model="student.email"
            label="Email"
            :rules="emailRules"
            required
            prepend-icon="mdi-email"
            variant="outlined"
            class="mb-3"
          ></v-text-field>

          <v-text-field
            v-model="student.department"
            label="Department"
            :rules="departmentRules"
            required
            prepend-icon="mdi-school"
            variant="outlined"
            class="mb-3"
          ></v-text-field>

          <v-card-actions class="px-0">
            <v-btn
              color="grey"
              variant="outlined"
              @click="$router.push('/students')"
            >
              Cancel
            </v-btn>
            <v-spacer></v-spacer>
            <v-btn
              color="primary"
              type="submit"
              :loading="loading"
              :disabled="!valid"
            >
              {{ isEdit ? 'Update' : 'Create' }} Student
            </v-btn>
          </v-card-actions>
        </v-form>
      </v-card-text>
    </v-card>
  </div>
</template>

<script>
import { ref, computed, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import studentService from '@/services/studentService'

export default {
  name: 'StudentForm',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const showNotification = inject('showNotification')
    
    const form = ref(null)
    const valid = ref(false)
    const loading = ref(false)
    
    const student = ref({
      name: '',
      email: '',
      department: ''
    })

    const isEdit = computed(() => !!route.params.id)

    // Validation rules
    const nameRules = [
      v => !!v || 'Name is required',
      v => (v && v.length >= 2) || 'Name must be at least 2 characters',
      v => (v && v.length <= 100) || 'Name must be less than 100 characters'
    ]

    const emailRules = [
      v => !!v || 'Email is required',
      v => /.+@.+\..+/.test(v) || 'Email must be valid'
    ]

    const departmentRules = [
      v => !!v || 'Department is required',
      v => (v && v.length >= 2) || 'Department must be at least 2 characters',
      v => (v && v.length <= 50) || 'Department must be less than 50 characters'
    ]

    const loadStudent = async () => {
      if (!isEdit.value) return

      loading.value = true
      try {
        const data = await studentService.getStudent(route.params.id)
        student.value = {
          name: data.name,
          email: data.email,
          department: data.department
        }
      } catch (error) {
        showNotification(error.message, 'error')
        router.push('/students')
      } finally {
        loading.value = false
      }
    }

    const submitForm = async () => {
      if (!valid.value) return

      loading.value = true
      try {
        if (isEdit.value) {
          await studentService.updateStudent(route.params.id, student.value)
          showNotification('Student updated successfully', 'success')
        } else {
          await studentService.createStudent(student.value)
          showNotification('Student created successfully', 'success')
        }
        router.push('/students')
      } catch (error) {
        showNotification(error.message, 'error')
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      loadStudent()
    })

    return {
      form,
      valid,
      loading,
      student,
      isEdit,
      nameRules,
      emailRules,
      departmentRules,
      submitForm
    }
  }
}
</script>