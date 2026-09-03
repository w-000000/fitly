import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import RecommendView from '../views/RecommendView.vue'
import { createRecommendation } from '../api'

vi.mock('../api', () => ({
  createRecommendation: vi.fn(),
}))

const setValidDates = async (wrapper) => {
  const dates = wrapper.findAll('input[type="date"]')
  await dates[0].setValue('2030-01-10')
  await dates[1].setValue('2030-01-12')
}

describe('RecommendView', () => {
  beforeEach(() => {
    vi.mocked(createRecommendation).mockReset()
  })

  it('대여 날짜가 없으면 API를 호출하지 않고 오류를 표시한다', async () => {
    const wrapper = mount(RecommendView)

    await wrapper.get('form').trigger('submit')

    expect(createRecommendation).not.toHaveBeenCalled()
    expect(wrapper.get('[role="alert"]').text()).toContain('시작일과 종료일')
  })

  it('종료일이 시작일보다 빠르면 요청을 거절한다', async () => {
    const wrapper = mount(RecommendView)
    const dates = wrapper.findAll('input[type="date"]')
    await dates[0].setValue('2030-01-12')
    await dates[1].setValue('2030-01-10')

    await wrapper.get('form').trigger('submit')

    expect(createRecommendation).not.toHaveBeenCalled()
    expect(wrapper.get('[role="alert"]').text()).toContain('종료일은 시작일과 같거나 이후')
  })

  it('단체 대여 인원이 2명 미만이면 요청을 거절한다', async () => {
    const wrapper = mount(RecommendView)
    await wrapper.findAll('.purpose-options button')[1].trigger('click')
    await setValidDates(wrapper)

    await wrapper.get('form').trigger('submit')

    expect(createRecommendation).not.toHaveBeenCalled()
    expect(wrapper.get('[role="alert"]').text()).toContain('2명 이상')
  })

  it('API 추천 응답을 상품 카드에 표시한다', async () => {
    vi.mocked(createRecommendation).mockResolvedValue({
      message: '면접에 어울리는 상품입니다.',
      products: [{
        id: 1,
        category: 'SUIT',
        name: '클래식 수트',
        description: '단정한 면접용 수트',
        reason: '포멀한 상황에 적합합니다.',
        rentalPrice: 24000,
        purchasePrice: 180000,
      }],
    })
    const wrapper = mount(RecommendView)
    await setValidDates(wrapper)

    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(createRecommendation).toHaveBeenCalledOnce()
    expect(createRecommendation).toHaveBeenCalledWith(expect.objectContaining({
      purpose: 'PERSONAL',
      rentalStartDate: '2030-01-10',
      rentalEndDate: '2030-01-12',
    }))
    expect(wrapper.text()).toContain('면접에 어울리는 상품입니다.')
    expect(wrapper.text()).toContain('클래식 수트')
    expect(wrapper.text()).toContain('24,000원')
  })
})
