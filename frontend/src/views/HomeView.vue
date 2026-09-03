<script setup>
const quickMenus = [
  { icon: '↗', title: '내 옷장', description: '등록한 옷과 사진을 확인해요' },
  { icon: '→', title: '대여 현황', description: '현재 대여 중인 상품을 확인해요' },
  { icon: '+', title: '행사·모임 대여', description: '인원과 기간을 입력해 신청해요' },
]
const wardrobe = [
  { type: 'PANTS', name: 'Black Slacks', color: 'charcoal' },
  { type: 'TOP', name: 'White Shirt', color: 'ivory' },
  { type: 'SHOES', name: 'Daily Sneakers', color: 'silver' },
]
</script>

<template>
  <main>
    <section class="home-hero content-width">
      <div class="hero-main">
        <p class="section-kicker">CUSTOMER HOME</p>
        <h1>오늘 뭐 입지?</h1>
        <p>상황과 가지고 있는 옷을 알려주면<br>FITLY가 필요한 순간의 코디를 골라드려요.</p>
        <RouterLink class="primary-button" to="/recommend">AI 코디 추천받기 <span>→</span></RouterLink>
      </div>
      <aside class="quick-panel">
        <p class="panel-label">빠른가기</p>
        <button v-for="menu in quickMenus" :key="menu.title" type="button" class="quick-row">
          <span class="quick-icon">{{ menu.icon }}</span>
          <span><b>{{ menu.title }}</b><small>{{ menu.description }}</small></span><i>+</i>
        </button>
      </aside>
    </section>

    <section class="start-card content-width">
      <div><span class="card-index">01</span><p class="section-kicker">AI STYLING</p><h2>내 옷은 활용하고,<br>필요한 옷만 대여해요.</h2></div>
      <div class="start-description">
        <p>TPO와 스타일을 선택하고 옷장 사진을 추가하면, 보유한 옷과 대여 상품을 함께 조합해 추천합니다.</p>
        <RouterLink to="/recommend">추천 시작하기 <span>→</span></RouterLink>
      </div>
    </section>

    <section id="wardrobe" class="wardrobe-section content-width">
      <div class="section-heading">
        <div><p class="section-kicker">MY WARDROBE</p><h2>내 옷장</h2><p>사진으로 등록한 옷을 추천에 활용할 수 있어요.</p></div>
        <button type="button" class="outline-button">+ 새 옷 등록</button>
      </div>
      <div class="wardrobe-grid">
        <article v-for="item in wardrobe" :key="item.name" class="wardrobe-card">
          <div class="wardrobe-image" :class="item.color"><span>{{ item.type }}</span><div class="garment-shape"></div></div>
          <div><small>{{ item.type }}</small><h3>{{ item.name }}</h3><button type="button">수정</button><button type="button">삭제</button></div>
        </article>
        <RouterLink class="add-card" to="/recommend"><b>+</b><span>새 옷 등록</span></RouterLink>
      </div>
    </section>

    <section id="rental" class="rental-strip content-width">
      <div><p class="section-kicker">CURRENT RENTAL</p><h2>현재 대여 중인 상품이 없어요.</h2></div>
      <RouterLink class="outline-button" to="/recommend">코디 추천 보기</RouterLink>
    </section>
  </main>
</template>
