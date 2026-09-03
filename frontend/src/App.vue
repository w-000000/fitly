<template>
  <div class="page">
    <nav class="navbar">
      <RouterLink class="brand" to="/"><span>RE:</span>WEAR</RouterLink>
      <div class="nav-links">
        <RouterLink to="/">홈</RouterLink>
        <RouterLink to="/recommend">AI 맞춤 추천</RouterLink>
        <a href="#service">이용 방법</a>
      </div>
      <button class="nav-button" type="button">MY</button>
    </nav>
    <RouterView />
    <footer><RouterLink class="brand" to="/"><span>RE:</span>WEAR</RouterLink><p>필요한 순간에만, 더 나은 방식으로 입어요.</p><small>AI-READY MINI PROJECT · 2026</small></footer>
  </div>
</template>
