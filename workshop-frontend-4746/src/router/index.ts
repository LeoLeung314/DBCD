import { createRouter, createWebHistory } from 'vue-router'
// 注意这里：加上了 type 关键字
import type { RouteRecordRaw } from 'vue-router' 

import Layout from '../components/Layout.vue'
import TaskList from '../views/TaskList.vue'
import ProductList from '../views/ProductList.vue'

const routes: Array<RouteRecordRaw> = [
    {
        path: '/',
        component: Layout,
        redirect: '/tasks',
        children: [
            {
                path: 'tasks',
                name: 'Tasks',
                component: TaskList
            },
            {
                path: 'products',
                name: 'Products',
                component: ProductList
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router