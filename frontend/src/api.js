const request = async (path, options = {}) => {
  const response = await fetch(path, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  })

  if (!response.ok) {
    const problem = await response.json().catch(() => ({}))
    throw new Error(problem.message ?? `요청에 실패했습니다. (${response.status})`)
  }

  return response.json()
}

export const getNotes = () => request('/api/notes')

export const createNote = (note) =>
  request('/api/notes', { method: 'POST', body: JSON.stringify(note) })

export const createSummaryJob = (noteId) =>
  request(`/api/notes/${noteId}/ai-summary`, { method: 'POST' })

export const getAiJob = (jobId) => request(`/api/ai-jobs/${jobId}`)

