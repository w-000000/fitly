import { Buffer } from 'node:buffer'
import { expect, test } from '@playwright/test'

const recommendation = {
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
}

test('홈에서 추천 화면으로 이동하고 API 결과를 표시한다', async ({ page }) => {
  await page.route('**/api/wardrobe/items', async (route) => {
    await route.fulfill({ json: { id: 101, name: 'Black Slacks' } })
  })
  await page.route('**/api/recommendations', async (route) => {
    await route.fulfill({ json: recommendation })
  })
  await page.goto('/')

  await page.getByRole('link', { name: /AI 코디 추천받기/ }).click()
  await expect(page).toHaveURL(/\/recommend$/)

  await page.locator('input[type="file"]').setInputFiles({
    name: 'slacks.jpg', mimeType: 'image/jpeg', buffer: globalThis.Buffer.from([1, 2, 3]),
  })
  await page.locator('input[type="date"]').nth(0).fill('2030-01-10')
  await page.locator('input[type="date"]').nth(1).fill('2030-01-12')
  await page.getByRole('button', { name: /AI 코디 추천받기/ }).click()

  await expect(page.locator('.result-card').first().getByText('85% MATCH').first()).toBeVisible()
  await page.getByRole('button', { name: '코디 상세보기' }).click()
  await expect(page.locator('.look-items p').filter({ hasText: 'Black Slacks' })).toBeVisible()
  await expect(page.locator('.look-items p').filter({ hasText: 'Navy Single Blazer' })).toBeVisible()
})

test('주요 화면이 가로로 넘치지 않고 핵심 영역이 보인다', async ({ page }) => {
  await page.goto('/')
  await expect(page.locator('.site-header')).toBeVisible()
  await expect(page.locator('.home-hero')).toBeVisible()
  await expect(page.locator('.quick-section')).toBeVisible()
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
