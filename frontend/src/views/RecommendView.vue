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
const recommendationStage = ref('idle')
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
      status: '추천에 사용할 사진',
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
const submitLabel = computed(() => ({
  uploading: '내 옷 사진을 등록하고 있어요',
  analyzing: 'AI가 조건과 옷을 분석하고 있어요',
}[recommendationStage.value] || 'AI 코디 추천받기'))
const previewForWardrobe = (wardrobeItemId) => selectedImages.value
  .find((image) => image.wardrobeId === wardrobeItemId)?.url
const resetRecommendation = () => {
  recommendation.value = null
  error.value = ''
  document.querySelector('.recommend-header')?.scrollIntoView({ behavior: 'smooth' })
}

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
  recommendationStage.value = isGroup.value ? 'analyzing' : 'uploading'
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
          name: image.name.replace(/\.[^.]+$/, '') || form.value.wardrobeName,
          category: form.value.wardrobeCategory,
          color: form.value.wardrobeColor,
          season: 'ALL',
          description: form.value.prompt,
        })
        image.wardrobeId = wardrobe.id
        image.status = '업로드 완료'
        return wardrobe.id
      }))
      recommendationStage.value = 'analyzing'
      recommendation.value = await createRecommendation({
        tpo: form.value.personalSituation,
        style: form.value.style,
        size: form.value.size,
        budget: form.value.budget,
        wardrobeItemIds,
      })
      selectedImages.value.forEach((image) => { image.status = 'AI 분석 완료' })
    }
    window.requestAnimationFrame(() => document.querySelector('#results')?.scrollIntoView({ behavior: 'smooth' }))
  } catch (requestError) {
    selectedImages.value.forEach((image) => {
      if (!image.wardrobeId) image.status = '업로드 실패'
    })
    error.value = requestError.message || '추천 결과를 불러오지 못했습니다.'
  } finally {
    loading.value = false
    recommendationStage.value = 'idle'
  }
}
</script>

<template>
  <main class="recommend-page content-width">
    <header class="recommend-header">
      <p class="section-kicker ai-kicker">STEP 1 OF 2 · RECOMMENDATION INPUT</p>
      <h1>어떤 옷이 필요하세요?</h1>
      <p>상황과 취향, 가지고 있는 옷을 알려주시면 가장 잘 맞는 조합을 찾아드릴게요.</p>
    </header>

    <form class="curation-form" @submit.prevent="recommend">
      <div class="recommend-form-column">
        <section class="form-block">
          <h2><span>1.</span> TPO</h2>
          <div class="chip-row"><button v-for="option in tpoOptions" :key="option[0]" type="button" :class="{ active: form.personalSituation === option[0] }" @click="form.personalSituation = option[0]">{{ option[1] }}</button></div>
        </section>

        <section class="form-block">
          <h2><span>2.</span> 스타일</h2>
          <div class="chip-row style-chips"><button v-for="style in styleOptions" :key="style" type="button" :class="{ active: form.style === style }" @click="form.style = style">{{ style }}</button></div>
        </section>

        <section class="form-block size-block">
          <h2><span>3.</span> 평소 착용 사이즈</h2>
          <div class="size-select-row"><button v-for="size in ['S', 'M', 'L', 'XL']" :key="size" type="button" :class="{ active: form.size === size }" @click="form.size = size">{{ size }}</button></div>
        </section>

        <section class="selection-summary">
          <small>선택 요약</small><strong>{{ tpoOptions.find((option) => option[0] === form.personalSituation)?.[1] }} &nbsp;·&nbsp; {{ form.style }} &nbsp;·&nbsp; {{ form.size }}</strong>
          <p>Chip을 눌러 선택을 변경할 수 있어요. 조건이 구체적일수록 추천 정확도가 높아집니다.</p>
        </section>

        <section class="form-block detail-options">
          <div class="purpose-switch"><button type="button" :class="{ active: !isGroup }" @click="form.purpose = 'PERSONAL'">개인 코디</button><button type="button" :class="{ active: isGroup }" @click="form.purpose = 'EVENT'">단체 대여</button></div>
          <div class="field-grid">
            <template v-if="!isGroup"><label>최대 예산<div class="suffix-input"><input v-model.number="form.budget" type="number" min="10000" step="1000"><span>원</span></div></label></template>
            <template v-else>
              <label>단체명<input v-model="form.groupName" placeholder="예: 졸업 작품 발표팀"></label><label>행사 종류<input v-model="form.activityType" placeholder="예: 발표회, 워크숍"></label>
              <div class="group-size-row full-field"><div v-for="size in ['S', 'M', 'L', 'XL']" :key="size"><b>{{ size }}</b><button type="button" @click="changeSize(size, -1)">−</button><span>{{ form.groupSizes[size] }}</span><button type="button" @click="changeSize(size, 1)">＋</button></div><strong>총 {{ groupCount }}명</strong></div>
            </template>
            <label>대여 시작일<input v-model="form.rentalStartDate" type="date" required></label><label>대여 종료일<input v-model="form.rentalEndDate" type="date" :min="form.rentalStartDate" required></label>
            <label class="full-field">추가 요청<textarea v-model="form.prompt" rows="3" placeholder="예: 너무 딱딱하지 않고 활동하기 편한 스타일"></textarea></label>
          </div>
        </section>
      </div>

      <section class="wardrobe-input-panel">
        <div class="panel-title"><span>4.</span><div><h2>내 옷</h2><p>코디에 활용할 옷을 업로드하거나 내 옷장에서 선택하세요.</p></div></div>
        <label class="upload-zone" @dragenter.prevent="isDraggingImage = true" @dragover.prevent="isDraggingImage = true" @dragleave.prevent="isDraggingImage = false" @drop.prevent="dropFile">
          <input type="file" accept="image/*" multiple @change="selectFile">
          <b>＋</b><strong>{{ isDraggingImage ? '여기에 이미지들을 놓아주세요' : '사진을 끌어다 놓으세요' }}</strong><small>JPG, PNG · 최대 10MB</small><span>사진 업로드</span>
        </label>
        <button class="closet-load-button" type="button">내 옷장 불러오기</button>
        <p v-if="imageError" class="upload-error" role="alert">{{ imageError }}</p>
        <div v-if="selectedImages.length" class="image-preview-list" aria-label="선택한 이미지 미리보기">
          <article v-for="image in selectedImages" :key="image.id" class="image-preview-card"><div class="image-preview-header"><small :title="image.name">{{ image.name }}</small><button type="button" :aria-label="`${image.name} 삭제`" @click="removeFile(image.id)">×</button></div><img :src="image.url" :alt="`${image.name} 미리보기`"><small class="image-analysis-status">{{ image.status || '추천에 사용할 사진' }}</small></article>
        </div>
        <p v-if="rentalDays > 0" class="date-summary">선택한 대여 기간 · <b>{{ rentalDays }}일</b></p>
        <p v-if="error" class="form-error" role="alert">{{ error }}</p>
        <button class="submit-button" :disabled="loading"><span>{{ submitLabel }}</span><b>→</b></button>
      </section>
    </form>

    <section v-if="recommendation" id="results" class="result-section">
      <div class="result-heading"><div><p class="section-kicker ai-kicker">✦ CUSTOMER AI · {{ resultCount }} RESULTS</p><h2>나를 위한 추천 코디</h2><p>내 옷을 최대한 활용하고, 꼭 필요한 상품만 골랐어요.</p></div><button type="button" class="outline-button" @click="resetRecommendation">조건 다시 설정</button></div>
      <p class="result-message">{{ resultMessage }}</p>
      <div v-if="recommendation.recommendations" class="result-grid">
        <article v-for="look in recommendation.recommendations" :key="look.recommendationId" class="result-card">
          <div class="result-visual" :class="`look-${((look.rank - 1) % 3) + 1}`"><span>LOOK {{ String(look.rank).padStart(2, '0') }} · {{ look.matchScore }}% MATCH</span></div>
          <div class="result-info">
            <small>AI MATCHED</small><h3>{{ look.outfitTitle }}</h3>
            <div class="look-items"><b>내 옷</b><p v-for="item in look.wardrobeItems" :key="item.wardrobeItemId"><img v-if="previewForWardrobe(item.wardrobeItemId)" :src="previewForWardrobe(item.wardrobeItemId)" alt="">{{ item.itemName }} <span>0원</span></p></div>
            <div class="look-items"><b>필요한 대여 상품</b><p v-for="item in look.rentalItems" :key="item.productVariantId">{{ item.productName }} · {{ item.size }} <span>{{ formatPrice(item.rentalPrice) }}원</span></p></div>
            <div class="reason"><b>추천 이유</b><span>{{ look.stylingComment }}</span></div>
            <div class="result-price"><span>총 대여가</span><strong>{{ formatPrice(look.totalRentalPrice) }}원</strong></div>
          </div>
        </article>
      </div>
      <div v-else class="result-grid">
        <article v-for="(product, index) in recommendation.products" :key="product.id" class="result-card">
          <div class="result-visual" :class="`look-${(index % 3) + 1}`"><span>LOOK 0{{ index + 1 }}</span></div>
          <div class="result-info"><small>{{ product.category }} · AI MATCHED</small><h3>{{ product.name }}</h3><p>{{ product.description }}</p><div class="reason"><b>추천 이유</b><span>{{ product.reason }}</span></div><div class="result-price"><span>대여가</span><strong>{{ formatPrice(product.rentalPrice) }}원</strong></div><div class="result-actions"><button type="button">코디 상세</button><button type="button" class="accent">대여하기</button></div></div>
        </article>
      </div>
    </section>
  </main>
</template>
