<template>
  <main class="shell">
    <header class="hero">
      <div class="hero-copy">
        <p class="eyebrow">AI-READY CLOTHING RENTAL</p>
        <h1>필요한 순간,<br><em>가장 나다운 옷.</em></h1>
        <p class="subtitle">개인 일정부터 행사·모임까지. 사지 않아도 괜찮아요.<br>당신의 중요한 하루에 꼭 맞는 옷을 찾아드릴게요.</p>
        <RouterLink class="hero-cta" to="/recommend">내 옷 추천받기 <span>→</span></RouterLink>
        <div class="hero-stats"><div><strong>4.9</strong><span>평균 만족도</span></div><div><strong>2,400+</strong><span>누적 대여</span></div><div><strong>100%</strong><span>검수·세탁</span></div></div>
      </div>
      <div class="hero-visual" aria-hidden="true">
        <div class="orbit"></div><div class="outfit back"><small>EVENT</small><strong>Group<br>Outfit</strong></div>
        <div class="outfit front"><small>PERSONAL</small><div class="jacket">♟</div><strong>Classic Suit Set</strong><span>₩29,000 / 3 days</span></div>
      </div>
    </header>
    <section id="service" class="benefits">
      <article><b>01</b><div><strong>상황에 꼭 맞게</strong><p>개인 일정·행사·모임별 맞춤 추천</p></div></article>
      <article><b>02</b><div><strong>부담 없는 가격</strong><p>구매가보다 합리적인 대여 비용</p></div></article>
      <article><b>03</b><div><strong>깨끗하고 안전하게</strong><p>전문 세탁과 꼼꼼한 검수 완료</p></div></article>
    </section>
    <section class="home-cta"><p class="eyebrow">PERSONAL CURATION</p><h2>고민은 줄이고,<br>중요한 순간에 집중하세요.</h2><RouterLink class="hero-cta" to="/recommend">AI 추천 시작하기 <span>→</span></RouterLink></section>
  </main>
</template>
