import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import RecommendView from '../views/RecommendView.vue'
import { createRecommendation, createRuleRecommendation, uploadWardrobeItem } from '../api'

vi.mock('../api', () => ({
  createRecommendation: vi.fn(),
  createRuleRecommendation: vi.fn(),
  uploadWardrobeItem: vi.fn(),
}))

const setValidDates = async (wrapper) => {
  const dates = wrapper.findAll('input[type="date"]')
  await dates[0].setValue('2030-01-10')
  await dates[1].setValue('2030-01-12')
}

describe('RecommendView', () => {
  beforeEach(() => {
    vi.mocked(createRecommendation).mockReset()
    vi.mocked(createRuleRecommendation).mockReset()
    vi.mocked(uploadWardrobeItem).mockReset()
    vi.stubGlobal('URL', {
      createObjectURL: vi.fn(() => 'blob:preview-image'),
      revokeObjectURL: vi.fn(),
    })
  })

  it('선택한 이미지를 업로드 영역에서 미리 보여준다', async () => {
    const wrapper = mount(RecommendView)
    const image = new globalThis.File(['image'], 'event-look.png', { type: 'image/png' })
    const input = wrapper.get('input[type="file"]')
    Object.defineProperty(input.element, 'files', { value: [image] })

    await input.trigger('change')

    expect(wrapper.get('.image-preview-card img').attributes('src')).toBe('blob:preview-image')
    expect(wrapper.get('.image-preview-card img').attributes('alt')).toContain('event-look.png')
    expect(wrapper.text()).toContain('event-look.png')
  })

  it('드래그해 놓은 이미지를 미리 보여준다', async () => {
    const wrapper = mount(RecommendView)
    const image = new globalThis.File(['image'], 'group-event.jpg', { type: 'image/jpeg' })

    await wrapper.get('.upload-zone').trigger('drop', {
      dataTransfer: { files: [image] },
    })

    expect(wrapper.get('.image-preview-card img').attributes('src')).toBe('blob:preview-image')
    expect(wrapper.text()).toContain('group-event.jpg')
  })

  it('여러 이미지를 선택하면 미리보기 상자를 필요한 만큼 만든다', async () => {
    const wrapper = mount(RecommendView)
    const input = wrapper.get('input[type="file"]')
    const images = [
      new globalThis.File(['one'], 'first.png', { type: 'image/png' }),
      new globalThis.File(['two'], 'second.jpg', { type: 'image/jpeg' }),
    ]
    Object.defineProperty(input.element, 'files', { value: images })

    await input.trigger('change')

    expect(wrapper.findAll('.image-preview-card')).toHaveLength(2)
    expect(wrapper.text()).toContain('first.png')
    expect(wrapper.text()).toContain('second.jpg')
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
    await wrapper.findAll('.purpose-switch button')[1].trigger('click')
    await setValidDates(wrapper)

    await wrapper.get('form').trigger('submit')

    expect(createRecommendation).not.toHaveBeenCalled()
    expect(wrapper.get('[role="alert"]').text()).toContain('2명 이상')
  })

  it('API 추천 응답을 상품 카드에 표시한다', async () => {
    vi.mocked(uploadWardrobeItem).mockResolvedValue({ id: 101, name: 'Black Slacks' })
    vi.mocked(createRecommendation).mockResolvedValue({
      requestId: 1001,
      status: 'COMPLETED',
      recommendations: [{
        recommendationId: 2001,
        rank: 1,
        outfitTitle: '면접용 네이비 블레이저 코디',
        matchScore: 85,
        stylingComment: '블랙 슬랙스와 조화롭습니다.',
        wardrobeItems: [{ wardrobeItemId: 101, itemName: 'Black Slacks', cost: 0 }],
        rentalItems: [{
          productId: 501,
          productVariantId: 1001,
          productName: 'Navy Single Blazer',
          brandName: 'FITLY',
          size: 'M',
          rentalPrice: 25000,
        }],
        totalRentalPrice: 25000,
      }],
    })
    const wrapper = mount(RecommendView)
    await setValidDates(wrapper)
    const image = new globalThis.File(
      [new Uint8Array([1, 2, 3])], 'slacks.jpg', { type: 'image/jpeg' },
    )
    const fileInput = wrapper.get('input[type="file"]')
    Object.defineProperty(fileInput.element, 'files', { value: [image] })
    await fileInput.trigger('change')

    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(uploadWardrobeItem).toHaveBeenCalledOnce()
    expect(createRecommendation).toHaveBeenCalledOnce()
    expect(createRecommendation).toHaveBeenCalledWith(expect.objectContaining({
      tpo: 'INTERVIEW',
      style: 'Formal',
      size: 'M',
      wardrobeItemIds: [101],
    }))
    expect(wrapper.text()).toContain('85% MATCH')
    expect(wrapper.text()).toContain('Black Slacks')
    expect(wrapper.text()).toContain('Navy Single Blazer')
    expect(wrapper.text()).toContain('25,000원')
    expect(wrapper.text()).toContain('AI 분석 완료')
    expect(wrapper.get('.look-items img').attributes('src')).toBe('blob:preview-image')
  })
})
