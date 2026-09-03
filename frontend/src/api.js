const request = async (path, options = {}) => {
  const isMultipart = options.body instanceof globalThis.FormData
  const response = await fetch(path, {
    ...options,
    headers: { ...(isMultipart ? {} : { 'Content-Type': 'application/json' }), ...options.headers },
  })

  if (!response.ok) {
    const problem = await response.json().catch(() => ({}))
    throw new Error(problem.message ?? `요청에 실패했습니다. (${response.status})`)
  }

  return response.json()
}

const customerHeaders = {
  'X-Actor-Role': 'ROLE_CUSTOMER',
  'X-User-Id': '71',
}

export const createRecommendation = (conditions) =>
  request('/api/recommendations', {
    method: 'POST',
    headers: customerHeaders,
    body: JSON.stringify(conditions),
  })

export const createRuleRecommendation = (conditions) =>
  request('/api/recommendations/rules', {
    method: 'POST',
    body: JSON.stringify(conditions),
  })

export const uploadWardrobeItem = (image, metadata) => {
  const body = new globalThis.FormData()
  body.append('metadata', new globalThis.Blob([JSON.stringify(metadata)], { type: 'application/json' }))
  body.append('image', image)
  return request('/api/wardrobe/items', {
    method: 'POST',
    headers: customerHeaders,
    body,
  })
}
