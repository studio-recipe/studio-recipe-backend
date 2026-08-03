import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// SQL 튜닝 대상: GET /recipes/{recipeId} 단건 조회만 격리해서 부하를 준다.
// 메인페이지/추천 쿼리가 섞이지 않아야 slow.log에서 이 쿼리만 깨끗하게 추릴 수 있다.
const detailDuration = new Trend('detail_duration');
const errorRate = new Rate('error_rate');

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

const minId = 7016813;
const maxId = 7041374;

export default function () {
  const recipeId = Math.floor(Math.random() * (maxId - minId + 1)) + minId;

  const res = http.get(
    `http://localhost:8080/studio-recipe/recipes/${recipeId}`,
    {
      tags: { name: 'recipe_detail' },
      // 더미 데이터 범위 밖 id가 뽑히면 404가 정상이므로 실패로 카운트하지 않음
      responseCallback: http.expectedStatuses(200, 404),
    }
  );

  detailDuration.add(res.timings.duration);
  errorRate.add(res.status !== 200 && res.status !== 404);

  check(res, { '레시피 상세 200/404': r => r.status === 200 || r.status === 404 });

  sleep(0.3);
}
