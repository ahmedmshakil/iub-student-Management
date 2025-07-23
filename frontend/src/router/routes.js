import StudentList from '@/views/StudentList.vue'
import StudentForm from '@/views/StudentForm.vue'
import StudentDetail from '@/views/StudentDetail.vue'
import NotFound from '@/views/NotFound.vue'

/**
 * Route configuration for IUB Student Management System
 * 
 * Each route includes:
 * - path: URL path for the route
 * - name: Unique identifier for the route
 * - component: Vue component to render
 * - props: Props to pass to the component (if needed)
 * - meta: Additional information including:
 *   - title: Page title (used for document.title)
 *   - breadcrumb: Array of breadcrumb items for navigation
 *   - transitionName: Optional animation for page transitions
 */
const routes = [
  {
    path: '/',
    redirect: '/students',
    meta: {
      skipBreadcrumb: true
    }
  },
  {
    path: '/students',
    name: 'StudentList',
    component: StudentList,
    meta: {
      title: 'Students',
      description: 'View and manage all student records',
      icon: 'mdi-account-group',
      breadcrumb: [
        { text: 'Home', disabled: false, href: '/' },
        { text: 'Students', disabled: true }
      ],
      transitionName: 'slide-fade'
    }
  },
  {
    path: '/students/create',
    name: 'StudentCreate',
    component: StudentForm,
    props: { isEditMode: false },
    meta: {
      title: 'Add Student',
      description: 'Create a new student record',
      icon: 'mdi-account-plus',
      breadcrumb: [
        { text: 'Home', disabled: false, href: '/' },
        { text: 'Students', disabled: false, href: '/students' },
        { text: 'Add Student', disabled: true }
      ],
      transitionName: 'slide-fade'
    }
  },
  {
    path: '/students/:id/edit',
    name: 'StudentEdit',
    component: StudentForm,
    props: route => ({ 
      id: parseInt(route.params.id),
      isEditMode: true 
    }),
    meta: {
      title: 'Edit Student',
      description: 'Update existing student information',
      icon: 'mdi-account-edit',
      breadcrumb: [
        { text: 'Home', disabled: false, href: '/' },
        { text: 'Students', disabled: false, href: '/students' },
        { text: 'Student Details', disabled: false, href: to => `/students/${to.params.id}` },
        { text: 'Edit Student', disabled: true }
      ],
      transitionName: 'slide-fade'
    }
  },
  {
    path: '/students/:id',
    name: 'StudentDetail',
    component: StudentDetail,
    props: route => ({ id: parseInt(route.params.id) }),
    meta: {
      title: 'Student Details',
      description: 'View detailed student information',
      icon: 'mdi-account-details',
      breadcrumb: [
        { text: 'Home', disabled: false, href: '/' },
        { text: 'Students', disabled: false, href: '/students' },
        { text: 'Student Details', disabled: true }
      ],
      transitionName: 'slide-fade'
    }
  },
  {
    // Catch-all route for 404 errors
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: {
      title: 'Page Not Found',
      description: 'The requested page does not exist',
      icon: 'mdi-alert-circle',
      breadcrumb: [
        { text: 'Home', disabled: false, href: '/' },
        { text: 'Not Found', disabled: true }
      ],
      transitionName: 'fade'
    }
  }
]

export default routes