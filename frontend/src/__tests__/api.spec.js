import { afterEach, describe, expect, it, vi } from 'vitest'
import { createRecommendation, uploadWardrobeItem } from '../api'

const successfulResponse = () => ({
  ok: true,
  json: vi.fn().mockResolvedValue({ id: 1 }),
})

describe('API content type', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('추천 JSON 요청에 application/json과 인증 헤더를 함께 보낸다', async () => {
    const fetchMock = vi.fn().mockResolvedValue(successfulResponse())
    vi.stubGlobal('fetch', fetchMock)

    await createRecommendation({ tpo: 'INTERVIEW' })

    expect(fetchMock).toHaveBeenCalledWith('/api/recommendations', expect.objectContaining({
      method: 'POST',
      headers: expect.objectContaining({
        'Content-Type': 'application/json',
        'X-Actor-Role': 'ROLE_CUSTOMER',
        'X-User-Id': '71',
      }),
    }))
  })

  it('multipart 업로드의 Content-Type은 브라우저가 boundary와 함께 설정하게 둔다', async () => {
    const fetchMock = vi.fn().mockResolvedValue(successfulResponse())
    vi.stubGlobal('fetch', fetchMock)
    const image = new globalThis.File(
      [new Uint8Array([1, 2, 3])], 'slacks.jpg', { type: 'image/jpeg' },
    )

    await uploadWardrobeItem(image, { name: 'Black Slacks', category: 'BOTTOM' })

    const options = fetchMock.mock.calls[0][1]
    expect(options.body).toBeInstanceOf(globalThis.FormData)
    expect(options.headers).not.toHaveProperty('Content-Type')
    expect(options.headers).toMatchObject({
      'X-Actor-Role': 'ROLE_CUSTOMER',
      'X-User-Id': '71',
    })
  })
})
