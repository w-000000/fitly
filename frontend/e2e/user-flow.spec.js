import { Buffer } from 'node:buffer'
import { expect, test } from '@playwright/test'

const recommendation = {
  message: '테스트 추천 결과입니다.',
  model: 'fitly-rules-v1',
  rentalDays: 3,
  products: [{
    id: 1,
    category: 'SUIT',
    name: '브라우저 테스트 수트',
    description: '브라우저에서 응답 표시를 확인합니다.',
    reason: '면접 상황에 적합합니다.',
    rentalPrice: 24000,
    purchasePrice: 180000,
  }],
}

test('홈에서 추천 화면으로 이동하고 API 결과를 표시한다', async ({ page }) => {
  await page.route('**/api/recommendations', async (route) => {
    await route.fulfill({ json: recommendation })
  })
  await page.goto('/')

  await page.getByRole('link', { name: /AI 코디 추천받기/ }).click()
  await expect(page).toHaveURL(/\/recommend$/)

  await page.locator('input[type="date"]').nth(0).fill('2030-01-10')
  await page.locator('input[type="date"]').nth(1).fill('2030-01-12')
  await page.getByRole('button', { name: /AI 코디 추천받기/ }).click()

  await expect(page.getByText('테스트 추천 결과입니다.')).toBeVisible()
  await expect(page.getByText('브라우저 테스트 수트')).toBeVisible()
})

test('주요 화면이 가로로 넘치지 않고 핵심 영역이 보인다', async ({ page }) => {
  await page.goto('/')
  await expect(page.locator('.site-header')).toBeVisible()
  await expect(page.locator('.home-hero')).toBeVisible()
  await expect(page.locator('.wardrobe-section')).toBeVisible()

  const hasHorizontalOverflow = await page.evaluate(
    () => document.documentElement.scrollWidth > document.documentElement.clientWidth,
  )
  expect(hasHorizontalOverflow).toBe(false)
})

test('선택한 옷 이미지를 업로드 영역에서 미리 보여준다', async ({ page }) => {
  await page.goto('/recommend')

  await page.locator('input[type="file"]').setInputFiles({
    name: 'event-look.png',
    mimeType: 'image/png',
    buffer: Buffer.from('preview image'),
  })

  await expect(page.locator('.image-preview-card img')).toBeVisible()
  await expect(page.getByText('event-look.png', { exact: true })).toBeVisible()
})
