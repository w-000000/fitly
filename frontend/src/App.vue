<template>
  <div class="app-shell">
    <header class="site-header">
      <div class="header-inner">
        <RouterLink class="logo" to="/">FITLY</RouterLink>
        <nav class="main-nav" aria-label="주요 메뉴">
          <RouterLink to="/recommend">코디 추천</RouterLink>
          <a href="/#wardrobe">내 옷장</a>
          <a href="/#rental">대여 현황</a>
          <a href="/#group-rental">단체 대여</a>
        </nav>
        <button class="profile-button" type="button">MY / PROFILE <span>⌄</span></button>
      </div>
    </header>
    <RouterView />
    <footer class="site-footer">
      <strong>FITLY</strong>
      <p>내 옷은 활용하고, 필요한 옷만 대여하는 AI 스타일 큐레이션</p>
      <small>FITLY MINI PROJECT</small>
    </footer>
  </div>
</template>
