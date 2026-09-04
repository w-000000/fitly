<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import { createRecommendation, createRuleRecommendation, uploadWardrobeItem } from '../api'

const tpoOptions = [['INTERVIEW', '면접'], ['WORK', '출근'], ['DATE', '데이트'], ['GUEST', '하객'], ['DAILY', '일상']]
const styleOptions = ['Minimal', 'Formal', 'Street', 'Casual']
const form = ref({
  purpose: 'PERSONAL', personalSituation: 'INTERVIEW', style: 'Formal', size: 'M', budget: 50000,
  rentalStartDate: '', rentalEndDate: '', groupName: '', activityType: '',
  groupSizes: { S: 0, M: 0, L: 0, XL: 0 }, prompt: '',
  wardrobeName: '내 옷', wardrobeCategory: 'BOTTOM', wardrobeColor: 'UNKNOWN',
})
const selectedImages = ref([])
const imageError = ref('')
const isDraggingImage = ref(false)
const loading = ref(false)
const error = ref('')
const recommendation = ref(null)
const isGroup = computed(() => form.value.purpose === 'EVENT')
const groupCount = computed(() => Object.values(form.value.groupSizes).reduce((sum, value) => sum + Number(value || 0), 0))
const rentalDays = computed(() => {
  if (!form.value.rentalStartDate || !form.value.rentalEndDate) return 0
  return Math.floor((new Date(form.value.rentalEndDate) - new Date(form.value.rentalStartDate)) / 86400000) + 1
})

const addImageFiles = (files) => {
  imageError.value = ''
  Array.from(files || []).forEach((file) => {
    if (!file.type.startsWith('image/')) {
      imageError.value = '이미지 파일만 추가할 수 있습니다.'
      return
    }
    if (file.size > 10 * 1024 * 1024) {
      imageError.value = '이미지는 한 장당 최대 10MB까지 추가할 수 있습니다.'
      return
    }
    selectedImages.value.push({
      id: `${file.name}-${file.lastModified}-${selectedImages.value.length}`,
      name: file.name,
      file,
      url: globalThis.URL.createObjectURL(file),
      wardrobeId: null,
    })
  })
}

const selectFile = (event) => {
  addImageFiles(event.target.files)
  event.target.value = ''
}
const dropFile = (event) => {
  isDraggingImage.value = false
  addImageFiles(event.dataTransfer?.files)
}
const removeFile = (id) => {
  const image = selectedImages.value.find((item) => item.id === id)
  if (image) globalThis.URL.revokeObjectURL(image.url)
  selectedImages.value = selectedImages.value.filter((item) => item.id !== id)
  imageError.value = ''
}

onBeforeUnmount(() => selectedImages.value.forEach((image) => globalThis.URL.revokeObjectURL(image.url)))
const changeSize = (size, amount) => {
  form.value.groupSizes[size] = Math.max(0, Number(form.value.groupSizes[size] || 0) + amount)
}
const formatPrice = (price = 0) => Number(price).toLocaleString('ko-KR')
const resultCount = computed(() => recommendation.value?.recommendations?.length
  ?? recommendation.value?.products?.length ?? 0)
const resultMessage = computed(() => {
  if (recommendation.value?.status === 'NO_MATCH') return '현재 조건에 맞는 대여 가능 상품이 없습니다.'
  if (recommendation.value?.status === 'NEEDS_INPUT') return '추천에 필요한 정보를 더 입력해주세요.'
  return recommendation.value?.message || '내 옷을 활용한 추천 코디입니다.'
})

const recommend = async () => {
  error.value = ''
  if (!form.value.rentalStartDate || !form.value.rentalEndDate) {
    error.value = '대여 시작일과 종료일을 선택해주세요.'
    return
  }
  if (rentalDays.value < 1) {
    error.value = '대여 종료일은 시작일과 같거나 이후여야 합니다.'
    return
  }
  if (isGroup.value && groupCount.value < 2) {
    error.value = '행사·모임 대여는 총 2명 이상 입력해주세요.'
    return
  }
  if (!isGroup.value && !selectedImages.value.length) {
    error.value = '추천에 사용할 내 옷 사진을 선택해주세요.'
    return
  }
  loading.value = true
  try {
    if (isGroup.value) {
      recommendation.value = await createRuleRecommendation({
        purpose: form.value.purpose,
        groupName: form.value.groupName,
        activityType: form.value.activityType,
        groupSizes: form.value.groupSizes,
        budget: form.value.budget,
        rentalStartDate: form.value.rentalStartDate,
        rentalEndDate: form.value.rentalEndDate,
        prompt: form.value.prompt,
      })
    } else {
      const wardrobeItemIds = await Promise.all(selectedImages.value.map(async (image) => {
        if (image.wardrobeId) return image.wardrobeId
        const wardrobe = await uploadWardrobeItem(image.file, {
          name: form.value.wardrobeName,
          category: form.value.wardrobeCategory,
          color: form.value.wardrobeColor,
          season: 'ALL',
          description: form.value.prompt,
        })
        image.wardrobeId = wardrobe.id
        return wardrobe.id
      }))
      recommendation.value = await createRecommendation({
        tpo: form.value.personalSituation,
        style: form.value.style,
        size: form.value.size,
        budget: form.value.budget,
        wardrobeItemIds,
      })
    }
    window.requestAnimationFrame(() => document.querySelector('#results')?.scrollIntoView({ behavior: 'smooth' }))
  } catch (requestError) {
    error.value = requestError.message || '추천 결과를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="recommend-page content-width">
    <header class="recommend-header">
      <p class="section-kicker">AI OUTFIT CURATION</p>
      <h1>AI 코디 추천</h1>
      <p>상황과 취향, 가지고 있는 옷을 알려주세요. 필요한 아이템만 골라 추천해드릴게요.</p>
    </header>

    <form class="curation-form" @submit.prevent="recommend">
      <section class="form-section">
        <div class="form-number">1</div>
        <div class="form-content">
          <div class="form-title"><h2>TPO</h2><span>어떤 상황에 입을 옷인가요?</span></div>
          <div class="chip-row"><button v-for="option in tpoOptions" :key="option[0]" type="button" :class="{ active: form.personalSituation === option[0] }" @click="form.personalSituation = option[0]">{{ option[1] }}</button></div>
        </div>
      </section>

      <section class="form-section">
        <div class="form-number">2</div>
        <div class="form-content">
          <div class="form-title"><h2>스타일</h2><span>원하는 무드를 골라주세요.</span></div>
          <div class="chip-row style-chips"><button v-for="style in styleOptions" :key="style" type="button" :class="{ active: form.style === style }" @click="form.style = style">{{ style }}</button></div>
        </div>
      </section>

      <section class="form-section">
        <div class="form-number">3</div>
        <div class="form-content">
          <div class="form-title"><h2>내 옷</h2><span>함께 입고 싶은 옷 사진을 추가할 수 있어요.</span></div>
          <label
            class="upload-zone"
            @dragenter.prevent="isDraggingImage = true"
            @dragover.prevent="isDraggingImage = true"
            @dragleave.prevent="isDraggingImage = false"
            @drop.prevent="dropFile"
          >
            <input type="file" accept="image/*" multiple @change="selectFile">
            <b>＋</b><strong>{{ isDraggingImage ? '여기에 이미지들을 놓아주세요' : '내 옷 사진 Drag & Drop' }}</strong>
            <small>여러 장 선택 가능 · PNG, JPG · 장당 최대 10MB</small><span>사진 선택</span>
          </label>
          <div class="wardrobe-fields">
            <label>아이템명<input v-model="form.wardrobeName" maxlength="100"></label>
            <label>카테고리<select v-model="form.wardrobeCategory"><option value="TOP">상의</option><option value="BOTTOM">하의</option><option value="OUTER">아우터</option><option value="SHOES">신발</option><option value="ACCESSORY">액세서리</option></select></label>
            <label>색상<input v-model="form.wardrobeColor" maxlength="50" placeholder="예: BLACK"></label>
          </div>
          <p v-if="imageError" class="upload-error" role="alert">{{ imageError }}</p>
          <div v-if="selectedImages.length" class="image-preview-list" aria-label="선택한 이미지 미리보기">
            <article v-for="image in selectedImages" :key="image.id" class="image-preview-card">
              <div class="image-preview-header">
                <small :title="image.name">{{ image.name }}</small>
                <button type="button" :aria-label="`${image.name} 삭제`" @click="removeFile(image.id)">×</button>
              </div>
              <img :src="image.url" :alt="`${image.name} 미리보기`">
            </article>
          </div>
        </div>
      </section>

      <section class="form-section final-section">
        <div class="form-number">4</div>
        <div class="form-content">
          <div class="form-title"><h2>세부 조건</h2><span>추천에 필요한 조건을 확인해주세요.</span></div>
          <div class="purpose-switch"><button type="button" :class="{ active: !isGroup }" @click="form.purpose = 'PERSONAL'">개인 코디</button><button type="button" :class="{ active: isGroup }" @click="form.purpose = 'EVENT'">행사·모임 대여</button></div>
          <div class="field-grid">
            <template v-if="!isGroup">
              <label>사이즈<select v-model="form.size"><option>S</option><option>M</option><option>L</option><option>XL</option></select></label>
              <label>최대 예산<div class="suffix-input"><input v-model.number="form.budget" type="number" min="10000" step="1000"><span>원</span></div></label>
            </template>
            <template v-else>
              <label>단체명<input v-model="form.groupName" placeholder="예: 졸업 작품 발표팀"></label>
              <label>행사 종류<input v-model="form.activityType" placeholder="예: 발표회, 워크숍"></label>
              <div class="group-size-row full-field">
                <div v-for="size in ['S', 'M', 'L', 'XL']" :key="size"><b>{{ size }}</b><button type="button" @click="changeSize(size, -1)">−</button><span>{{ form.groupSizes[size] }}</span><button type="button" @click="changeSize(size, 1)">＋</button></div>
                <strong>총 {{ groupCount }}명</strong>
              </div>
            </template>
            <label>대여 시작일<input v-model="form.rentalStartDate" type="date" required></label>
            <label>대여 종료일<input v-model="form.rentalEndDate" type="date" :min="form.rentalStartDate" required></label>
            <label class="full-field">추가 요청<textarea v-model="form.prompt" rows="3" placeholder="예: 너무 딱딱하지 않고 활동하기 편한 스타일"></textarea></label>
          </div>
          <p v-if="rentalDays > 0" class="date-summary">선택한 대여 기간 · <b>{{ rentalDays }}일</b></p>
          <p v-if="error" class="form-error" role="alert">{{ error }}</p>
          <button class="submit-button" :disabled="loading"><span>{{ loading ? '추천을 준비하고 있어요' : 'AI 코디 추천받기' }}</span><b>→</b></button>
        </div>
      </section>
    </form>

    <section v-if="recommendation" id="results" class="result-section">
      <div class="result-heading"><div><p class="section-kicker">CURATED FOR YOU</p><h2>추천 코디 {{ resultCount }}개</h2></div><span>AI CURATION · FITLY</span></div>
      <p class="result-message">{{ resultMessage }}</p>
      <div v-if="recommendation.recommendations" class="result-grid">
        <article v-for="look in recommendation.recommendations" :key="look.recommendationId" class="result-card">
          <div class="result-visual"><span>LOOK {{ String(look.rank).padStart(2, '0') }}</span><div class="look-shape"></div><small>{{ look.matchScore }}% MATCH</small></div>
          <div class="result-info">
            <small>AI MATCHED</small><h3>{{ look.outfitTitle }}</h3>
            <div class="look-items"><b>내 옷</b><p v-for="item in look.wardrobeItems" :key="item.wardrobeItemId">{{ item.itemName }} <span>0원</span></p></div>
            <div class="look-items"><b>필요한 대여 상품</b><p v-for="item in look.rentalItems" :key="item.productVariantId">{{ item.productName }} · {{ item.size }} <span>{{ formatPrice(item.rentalPrice) }}원</span></p></div>
            <div class="reason"><b>추천 이유</b><span>{{ look.stylingComment }}</span></div>
            <div class="result-price"><span>총 대여가</span><strong>{{ formatPrice(look.totalRentalPrice) }}원</strong></div>
          </div>
        </article>
      </div>
      <div v-else class="result-grid">
        <article v-for="(product, index) in recommendation.products" :key="product.id" class="result-card">
          <div class="result-visual"><span>LOOK 0{{ index + 1 }}</span><div class="look-shape"></div><small>{{ product.category }}</small></div>
          <div class="result-info"><small>{{ product.category }} · AI MATCHED</small><h3>{{ product.name }}</h3><p>{{ product.description }}</p><div class="reason"><b>추천 이유</b><span>{{ product.reason }}</span></div><div class="result-price"><span>대여가</span><strong>{{ formatPrice(product.rentalPrice) }}원</strong></div><div class="result-actions"><button type="button">코디 상세</button><button type="button" class="accent">대여하기</button></div></div>
        </article>
      </div>
    </section>
  </main>
</template>
