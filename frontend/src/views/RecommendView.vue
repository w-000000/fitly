<script setup>
import { computed, ref } from 'vue'
import { createRecommendation } from '../api'

const form = ref({
  purpose: 'PERSONAL',
  personalSituation: 'INTERVIEW',
  size: 'M',
  groupName: '',
  activityType: '',
  groupSizes: { S: 0, M: 0, L: 0, XL: 0 },
  budget: 50000,
  rentalStartDate: '',
  rentalEndDate: '',
  prompt: '',
})
const loading = ref(false)
const error = ref('')
const recommendation = ref(null)
const groupCount = computed(() => Object.values(form.value.groupSizes).reduce((sum, count) => sum + Number(count || 0), 0))
const isGroupPurpose = computed(() => form.value.purpose === 'EVENT')
const rentalDays = computed(() => {
  if (!form.value.rentalStartDate || !form.value.rentalEndDate) return 0
  const start = new Date(`${form.value.rentalStartDate}T00:00:00`)
  const end = new Date(`${form.value.rentalEndDate}T00:00:00`)
  return Math.floor((end - start) / 86400000) + 1
})

const recommend = async () => {
  error.value = ''
  recommendation.value = null
  if (!form.value.rentalStartDate || !form.value.rentalEndDate) {
    error.value = '대여 시작일과 종료일을 모두 선택해주세요.'
    return
  }
  if (rentalDays.value < 1) {
    error.value = '대여 종료일은 시작일과 같거나 이후여야 합니다.'
    return
  }
  if (isGroupPurpose.value && groupCount.value < 2) {
    error.value = '단체 대여는 사이즈별 수량을 합해 2명 이상 입력해주세요.'
    return
  }
  loading.value = true
  try {
    recommendation.value = await createRecommendation({
      purpose: form.value.purpose,
      personalSituation: isGroupPurpose.value ? null : form.value.personalSituation,
      size: isGroupPurpose.value ? null : form.value.size,
      groupName: isGroupPurpose.value ? form.value.groupName : null,
      activityType: isGroupPurpose.value ? form.value.activityType : null,
      groupSizes: isGroupPurpose.value ? form.value.groupSizes : null,
      budget: form.value.budget,
      rentalStartDate: form.value.rentalStartDate,
      rentalEndDate: form.value.rentalEndDate,
      prompt: form.value.prompt || null,
    })
  } catch (requestError) {
    error.value = requestError.message || '추천 상품을 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

const formatPrice = (price) => price.toLocaleString('ko-KR')

const changeSizeCount = (size, amount) => {
  const current = Number(form.value.groupSizes[size] || 0)
  form.value.groupSizes[size] = Math.min(100, Math.max(0, current + amount))
}
</script>

<template>
  <main class="shell recommend-page">
    <div class="page-heading"><p class="eyebrow">AI OUTFIT CURATION</p><h1>나에게 꼭 맞는 옷을<br><em>AI로 찾아보세요.</em></h1><p>상황과 예산을 알려주시면 가장 잘 어울리는 대여 상품을 추천해드려요.</p></div>
    <section class="recommend-layout">
      <div class="recommend-intro"><p class="eyebrow">SMART RECOMMEND</p><h2>어떤 옷이<br>필요한가요?</h2><p>간단한 정보만 알려주세요.<br>AI가 상황과 예산에 맞는 옷을 골라드려요.</p><div class="steps"><span>1</span><i></i><span>2</span><i></i><span>3</span></div><small>조건 선택　·　AI 추천　·　대여 신청</small></div>
      <div class="panel">
        <div class="panel-heading"><div><span>STEP 01</span><h3>대여 조건을 선택해주세요</h3></div><b>✦</b></div>
        <form @submit.prevent="recommend">
          <label class="wide">이용 목적<div class="purpose-options">
            <button type="button" :class="{ selected: form.purpose === 'PERSONAL' }" @click="form.purpose = 'PERSONAL'">▣<span>개인</span></button>
            <button type="button" :class="{ selected: form.purpose === 'EVENT' }" @click="form.purpose = 'EVENT'">✦<span>행사·모임</span></button>
          </div></label>
          <template v-if="isGroupPurpose">
            <label>모임명<input v-model.trim="form.groupName" type="text" placeholder="예: 신입사원 환영회"></label>
            <label>행사 종류<input v-model.trim="form.activityType" type="text" placeholder="예: 워크숍, 체육대회, 결혼식"></label>
            <div class="size-quantity wide">
              <div class="size-heading">
                <strong>사이즈별 인원</strong>
                <span>총 <b>{{ groupCount }}명</b></span>
              </div>
              <div class="size-grid">
                <label v-for="size in ['S', 'M', 'L', 'XL']" :key="size">
                  <span>{{ size }}</span>
                  <div class="quantity-control">
                    <button type="button" aria-label="인원 줄이기" @click="changeSizeCount(size, -1)">−</button>
                    <div class="quantity-value">
                      <input v-model.number="form.groupSizes[size]" type="number" min="0" max="100" :aria-label="`${size} 사이즈 인원`">
                      <span>명</span>
                    </div>
                    <button type="button" aria-label="인원 늘리기" @click="changeSizeCount(size, 1)">+</button>
                  </div>
                </label>
              </div>
            </div>
          </template>
          <template v-else>
            <label>개인 상황<select v-model="form.personalSituation"><option value="INTERVIEW">면접</option><option value="WORK">출근·비즈니스</option><option value="DATE">소개팅·데이트</option><option value="GUEST">결혼식 하객</option><option value="DAILY">일상·기타</option></select></label>
            <label>사이즈<select v-model="form.size"><option>S</option><option>M</option><option>L</option><option>XL</option></select></label>
          </template>
          <label class="wide">최대 예산<div class="input-suffix"><input v-model.number="form.budget" type="number" min="10000" step="1000" required><span>원</span></div></label>
          <fieldset class="rental-period wide">
            <legend>대여 일정</legend>
            <div class="date-range">
              <label><span>시작일</span><input v-model="form.rentalStartDate" type="date" required></label>
              <b aria-hidden="true">→</b>
              <label><span>종료일</span><input v-model="form.rentalEndDate" type="date" :min="form.rentalStartDate" required></label>
            </div>
            <p v-if="rentalDays > 0" class="rental-summary">총 <strong>{{ rentalDays }}일</strong> 동안 대여합니다.</p>
          </fieldset>
          <label class="prompt-field wide">
            <span class="prompt-heading"><b>AI에게 추가로 요청하기</b><small>선택사항</small></span>
            <textarea
              v-model.trim="form.prompt"
              rows="4"
              maxlength="500"
              placeholder="예: 너무 딱딱하지 않은 스타일로 추천해줘. 밝은 색상은 피하고 활동하기 편했으면 좋겠어."
            ></textarea>
            <small class="character-count">{{ form.prompt.length }} / 500</small>
          </label>
          <button class="recommend-button wide" :disabled="loading"><span>{{ loading ? '나에게 맞는 옷을 찾는 중…' : 'AI 맞춤 의류 추천받기' }}</span><b>→</b></button>
        </form>
        <p v-if="error" class="error" role="alert">{{ error }}</p>
      </div>
    </section>
    <section v-if="recommendation" class="results">
      <div class="section-title"><div><p class="eyebrow">YOUR CURATION</p><h2>당신을 위한 추천</h2></div><span>{{ recommendation.products.length }}개</span></div>
      <p class="recommendation-message">✦ {{ recommendation.message }}</p>
      <div class="product-grid">
        <article v-for="product in recommendation.products" :key="product.id" class="card">
          <div class="product-image"><span>{{ product.category }}</span><b>♟</b><small>CURATED LOOK</small></div>
          <div class="product-info"><span class="badge">{{ product.category }}</span><h3>{{ product.name }}</h3><p>{{ product.description }}</p><aside class="summary"><strong>AI 추천 이유</strong><p>{{ product.reason }}</p></aside><div class="price-info"><p>대여 가격<strong>{{ formatPrice(product.rentalPrice) }}원 / {{ rentalDays }}일</strong></p><p>구매 가격<strong>{{ formatPrice(product.purchasePrice) }}원</strong></p></div><div class="actions"><button type="button">대여 신청</button><button type="button" class="secondary">구매하기</button></div></div>
        </article>
      </div>
    </section>
  </main>
</template>
