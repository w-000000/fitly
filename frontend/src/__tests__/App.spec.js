import { flushPromises, mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import { describe, expect, it } from 'vitest'
import App from '../App.vue'
import HomeView from '../views/HomeView.vue'
import RecommendView from '../views/RecommendView.vue'

const createTestRouter = () => createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/', component: HomeView },
    { path: '/recommend', component: RecommendView },
  ],
})

describe('App navigation', () => {
  it('추천 버튼을 누르면 추천 페이지로 이동한다', async () => {
    const router = createTestRouter()
    router.push('/')
    await router.isReady()

    const wrapper = mount(App, {
      global: { plugins: [router] },
    })

    await wrapper.get('.primary-button').trigger('click')
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/recommend')
    expect(wrapper.text()).toContain('AI 코디 추천')
  })
})
