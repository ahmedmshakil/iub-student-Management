import StudentList from '@/views/StudentList.vue'
import StudentForm from '@/views/StudentForm.vue'
import StudentDetail from '@/views/StudentDetail.vue'

const routes = [
  {
    path: '/',
    redirect: '/students'
  },
  {
    path: '/students',
    name: 'StudentList',
    component: StudentList,
    meta: {
      title: 'Students'
    }
  },
  {
    path: '/students/create',
    name: 'StudentCreate',
    component: StudentForm,
    meta: {
      title: 'Add Student'
    }
  },
  {
    path: '/students/:id/edit',
    name: 'StudentEdit',
    component: StudentForm,
    meta: {
      title: 'Edit Student'
    }
  },
  {
    path: '/students/:id',
    name: 'StudentDetail',
    component: StudentDetail,
    meta: {
      title: 'Student Details'
    }
  }
]

export default routes