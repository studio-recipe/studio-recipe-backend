import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// 랜덤 id 버전(recipe_detail_test.js)과 달리, 모든 VU가 같은 레시피 하나에 몰린다.
// 인기 레시피 하나에 트래픽이 집중될 때(row lock 경합, 조회수 증가 동시성)를 재현하는 용도.
const detailDuration = new Trend('detail_duration');
const errorRate = new Rate('error_rate');

// 부하를 집중시킬 고정 레시피 id. 존재하는 id인지 먼저 확인하고 넣을 것.
const HOT_RECIPE_ID = __ENV.HOT_RECIPE_ID || 7016813;

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
    'detail_duration': ['p(95)<2000'],
    'error_rate': ['rate<0.05'],
  },
};

export default function () {
  const res = http.get(
    `http://localhost:8080/studio-recipe/recipes/${HOT_RECIPE_ID}`,
    { tags: { name: 'recipe_detail_hot' } }
  );

  detailDuration.add(res.timings.duration);
  errorRate.add(res.status !== 200);

  check(res, { '레시피 상세 200': r => r.status === 200 });

  sleep(0.3);
}
