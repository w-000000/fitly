<script setup>
import { onMounted, ref } from 'vue'
import { createNote, createSummaryJob, getAiJob, getNotes } from './api'

const notes = ref([])
const title = ref('')
const content = ref('')
const loading = ref(false)
const error = ref('')
const summarizingId = ref(null)

const loadNotes = async () => {
  try {
    notes.value = await getNotes()
  } catch (e) {
    error.value = e.message
  }
}

const submit = async () => {
  error.value = ''
  loading.value = true
  try {
    const note = await createNote({ title: title.value, content: content.value })
    notes.value.unshift(note)
    title.value = ''
    content.value = ''
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

const summarize = async (note) => {
  error.value = ''
  summarizingId.value = note.id
  try {
    const job = await createSummaryJob(note.id)
    const completed = await pollJob(job.jobId)
    note.aiSummary = completed.result.summary
  } catch (e) {
    error.value = e.message
  } finally {
    summarizingId.value = null
  }
}

const pollJob = async (jobId) => {
  for (let attempt = 0; attempt < 15; attempt += 1) {
    const job = await getAiJob(jobId)
    if (job.status === 'COMPLETED') return job
    if (job.status === 'FAILED') throw new Error(job.error ?? 'AI 작업에 실패했습니다.')
    await new Promise((resolve) => setTimeout(resolve, 300))
  }
  throw new Error('AI 응답 대기 시간이 초과되었습니다.')
}

onMounted(loadNotes)
</script>

<template>
  <main class="shell">
    <header>
      <p class="eyebrow">AI-READY MINI PROJECT</p>
      <h1>Idea Note</h1>
      <p class="subtitle">아이디어를 기록하고 AI 요약 흐름을 미리 검증해보세요.</p>
    </header>

    <section class="panel">
      <h2>새 아이디어</h2>
      <form @submit.prevent="submit">
        <label>제목<input v-model.trim="title" required maxlength="100" placeholder="예: 점심 메뉴 추천 서비스" /></label>
        <label>내용<textarea v-model.trim="content" required maxlength="2000" rows="5" placeholder="해결하려는 문제와 핵심 기능을 적어주세요." /></label>
        <button :disabled="loading">{{ loading ? '저장 중…' : '아이디어 저장' }}</button>
      </form>
      <p v-if="error" class="error" role="alert">{{ error }}</p>
    </section>

    <section class="notes">
      <div class="section-title"><h2>아이디어 목록</h2><span>{{ notes.length }}개</span></div>
      <p v-if="!notes.length" class="empty">첫 번째 아이디어를 등록해보세요.</p>
      <article v-for="note in notes" :key="note.id" class="card">
        <div><h3>{{ note.title }}</h3><p>{{ note.content }}</p></div>
        <aside v-if="note.aiSummary" class="summary"><strong>AI 요약 (Mock)</strong><p>{{ note.aiSummary }}</p></aside>
        <button class="secondary" :disabled="summarizingId === note.id" @click="summarize(note)">
          {{ summarizingId === note.id ? '요약 중…' : 'AI 요약하기' }}
        </button>
      </article>
    </section>
  </main>
</template>

