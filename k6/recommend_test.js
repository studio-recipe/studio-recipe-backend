import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// 커스텀 메트릭
const recommendDuration = new Trend('recommend_duration');
const errorRate = new Rate('error_rate');

export const options = {
  scenarios: {
    // 캐시 워밍업 (처음 10초는 가볍게)
    warmup: {
      executor: 'constant-vus',
      vus: 1,
      duration: '10s',
      tags: { phase: 'warmup' },
    },
    // 본 측정 (30초간 가상 유저 10명)
    load_test: {
      executor: 'constant-vus',
      vus: 10,
      duration: '30s',
      startTime: '10s',
      tags: { phase: 'load' },
    },
    // 피크 측정 (10초간 가상 유저 30명)
    peak_test: {
      executor: 'constant-vus',
      vus: 30,
      duration: '10s',
      startTime: '40s',
      tags: { phase: 'peak' },
    },
  },
  thresholds: {
    // p95 응답시간이 2000ms 이하여야 통과
    'recommend_duration': ['p(95)<2000'],
    // 에러율 5% 이하
    'error_rate': ['rate<0.05'],
  },
};

// 테스트 전 로그인해서 토큰 발급
export function setup() {
  const loginRes = http.post(
    'http://localhost:8080/studio-recipe/auth/login',
    JSON.stringify({ id: __ENV.TEST_USER_ID, password: __ENV.TEST_USER_PW }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  const token = loginRes.json('accessToken');
  if (!token) {
    throw new Error('로그인 실패 - 테스트 계정을 확인하세요');
  }
  return { token };
}

export default function (data) {
  const headers = {
    'Authorization': `Bearer ${data.token}`,
    'Content-Type': 'application/json',
  };

  // 추천 API 호출
  const res = http.get(
    'http://localhost:8080/studio-recipe/recommend-recipes?k=10&lambda=0.8',
    { headers, tags: { name: 'recommend' } }
  );

  recommendDuration.add(res.timings.duration);
  errorRate.add(res.status !== 200);

  check(res, {
    '상태코드 200': (r) => r.status === 200,
    '응답시간 2초 이내': (r) => r.timings.duration < 2000,
    '응답 배열': (r) => {
      try {
        const body = JSON.parse(r.body);
        return Array.isArray(body);
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);
}

export function teardown(data) {
  console.log('테스트 완료');
}