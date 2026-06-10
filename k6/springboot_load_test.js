import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

const mainPageDuration  = new Trend('main_page_duration');
const detailDuration    = new Trend('detail_duration');
const recommendDuration = new Trend('recommend_duration');
const errorRate         = new Rate('error_rate');

export const options = {
  scenarios: {
    warmup: {
      executor: 'constant-vus',
      vus: 1,
      duration: '10s',
      tags: { phase: 'warmup' },
    },
    load_test: {
      executor: 'constant-vus',
      vus: 50,
      duration: '30s',
      startTime: '10s',
      tags: { phase: 'load' },
    },
    peak_test: {
      executor: 'constant-vus',
      vus: 100,
      duration: '20s',
      startTime: '40s',
      tags: { phase: 'peak' },
    },
  },
  thresholds: {
    'main_page_duration':  ['p(95)<2000'],
    'detail_duration':     ['p(95)<2000'],
    'recommend_duration':  ['p(95)<3000'],
    'error_rate':          ['rate<0.05'],
  },
};

export function setup() {
  const loginRes = http.post(
    'http://localhost:8080/studio-recipe/auth/login',
    JSON.stringify({ id: __ENV.TEST_USER_ID, password: __ENV.TEST_USER_PW }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  const token = loginRes.json('accessToken');
  if (!token) throw new Error('로그인 실패');
  return { token };
}

export default function (data) {
  const headers = {
    'Authorization': `Bearer ${data.token}`,
    'Content-Type': 'application/json',
  };

  // 1. 메인 페이지
  const mainRes = http.get(
    'http://localhost:8080/studio-recipe/main-pages?page=0&size=10',
    { tags: { name: 'main_page' } }
  );
  mainPageDuration.add(mainRes.timings.duration);
  errorRate.add(mainRes.status !== 200);
  check(mainRes, { '메인 페이지 200': r => r.status === 200 });

  sleep(0.3);

  // 2. 레시피 상세 — 404는 에러로 카운트하지 않음
  const minId = 7016813;
  const maxId = 7041374;
  const recipeId = Math.floor(Math.random() * (maxId - minId + 1)) + minId;

  const detailRes = http.get(
    `http://localhost:8080/studio-recipe/recipes/${recipeId}`,
    {
      tags: { name: 'recipe_detail' },
      // 404는 예상된 응답 → http_req_failed 제외
      responseCallback: http.expectedStatuses(200, 404),
    }
  );
  detailDuration.add(detailRes.timings.duration);
  // 200 또는 404만 정상으로 카운트
  errorRate.add(detailRes.status !== 200 && detailRes.status !== 404);
  check(detailRes, { '레시피 상세 200/404': r => r.status === 200 || r.status === 404 });

  sleep(0.3);

  // 3. 추천 레시피
  const recRes = http.get(
    'http://localhost:8080/studio-recipe/recommend-recipes?k=10&lambda=0.8',
    { headers, tags: { name: 'recommend' } }
  );
  recommendDuration.add(recRes.timings.duration);
  errorRate.add(recRes.status !== 200);
  check(recRes, { '추천 200': r => r.status === 200 });

  sleep(0.4);
}

export function teardown() {
  console.log('부하 테스트 완료');
}
