<script setup>
const quickMenus = [
  { icon: '01', href: '#wardrobe', title: '내 옷장', description: 'AI 추천에 사용할 보유 의류' },
  { icon: '02', href: '#rental', title: '대여 현황', description: '배송 · 대여 · 반납 상태' },
  { icon: '03', href: '#group-rental', title: '단체 대여', description: '연구복 · 안전복 · 단체 행사' },
]
const wardrobe = [
  { type: 'Pants · Black', name: 'Black Slacks', image: 'https://images.unsplash.com/photo-1553704571-1f9d11da53b0?auto=format&fit=crop&w=900&q=80' },
  { type: 'Top · White', name: 'White Shirt', image: 'https://images.unsplash.com/photo-1718278868740-9351177c5768?auto=format&fit=crop&w=900&q=80' },
  { type: 'Shoes · White', name: 'White Sneakers', image: 'https://images.unsplash.com/photo-1623496928880-9ced3876d2ea?auto=format&fit=crop&w=900&q=80' },
]
</script>

<template>
  <main>
    <section class="home-hero content-width">
      <div class="hero-main">
        <p class="section-kicker ai-kicker">✦ AI PERSONAL STYLIST</p>
        <h1>오늘 뭐 입지?</h1>
        <p>상황과 가지고 있는 옷을 알려주세요.<br>FITLY가 필요한 옷만 골라 완성된 코디를 제안합니다.</p>
        <RouterLink class="primary-button" to="/recommend">AI 코디 추천받기 <span>→</span></RouterLink>
        <div class="hero-note">
          <strong>내 옷은 활용하고, 필요한 옷만 대여</strong>
          <span>TPO · 스타일 · 내 옷을 한 번에 입력하면 실제 대여 가능한 조합만 보여드려요.</span>
        </div>
      </div>
      <aside class="hero-fashion">
        <div class="hero-photo"></div>
        <div class="match-card"><b>96% MATCH</b><span>면접 · Formal 추천 완료</span></div>
      </aside>
    </section>

    <section class="quick-section content-width">
      <h2>바로가기</h2>
      <div class="quick-grid">
        <a v-for="menu in quickMenus" :key="menu.title" :href="menu.href" class="quick-card">
          <span>{{ menu.icon }}</span><div><b>{{ menu.title }}</b><small>{{ menu.description }}</small></div><i>↗</i>
        </a>
      </div>
    </section>

    <section id="wardrobe" class="wardrobe-section content-width">
      <div class="section-heading">
        <div><p class="section-kicker">MY CLOSET · 3 ITEMS</p><h2>내 옷장</h2><p>AI 추천에서 활용할 보유 의류를 관리하세요.</p></div>
        <button type="button" class="dark-button">＋ 새 옷 등록</button>
      </div>
      <div class="wardrobe-grid">
        <article v-for="item in wardrobe" :key="item.name" class="wardrobe-card">
          <img class="wardrobe-image" :src="item.image" :alt="item.name">
          <div><h3>{{ item.name }}</h3><small>{{ item.type }}</small><div class="card-tools"><button type="button">수정</button><button type="button">삭제</button></div></div>
        </article>
        <button class="add-card" type="button"><b>＋</b><strong>새 옷 등록</strong><span>사진을 올리면 AI가<br>분류를 도와드려요.</span></button>
      </div>
    </section>

    <section id="rental" class="rental-section content-width">
      <div class="section-heading compact-heading"><div><p class="section-kicker">RENTAL STATUS · ACTIVE</p><h2>현재 대여 중</h2></div><span class="due-badge">반납 예정 09.16 · D-2</span></div>
      <article class="rental-card">
        <div class="rental-product-image"></div>
        <div class="rental-info">
          <h3>Navy Single Blazer</h3><p>MUSINSA STANDARD · M · 선택 기간 대여</p>
          <div class="rental-progress"><span class="done">배송 완료</span><span class="active">대여 중</span><span>반납 예정</span></div>
          <small>대여 기간</small><strong>09.12 수령&nbsp;&nbsp;—&nbsp;&nbsp;09.16 반납</strong>
          <div class="rental-actions"><button type="button">반납 신청</button><button type="button" class="dark-button">소장하기</button></div>
        </div>
      </article>
    </section>

    <section id="group-rental" class="group-section content-width">
      <div class="group-copy"><p class="section-kicker">GROUP RENTAL</p><h2>단체 / 특수 의류 대여</h2><p>연구실·현장 실습·단체 행사에 필요한 품목을 한 번에 요청하세요.</p></div>
      <div class="group-request-card">
        <div><small>이용 목적</small><strong>연구실</strong></div><div><small>이용 기간</small><strong>기간 직접 선택</strong></div>
        <div><small>인원</small><strong>2명 이상</strong></div><div><small>필요 품목</small><strong>연구복 · 안전화</strong></div>
        <button type="button" class="dark-button">단체 대여 요청하기 →</button>
      </div>
      <aside class="group-guide"><h3>요청 전에 확인하세요</h3><p>• 이용 목적과 기간<br>• 참여 인원<br>• 필요한 품목과 수량</p><small>요청 접수 후 담당자가 재고와 일정을 확인해 연락드립니다.</small></aside>
    </section>
  </main>
</template>
