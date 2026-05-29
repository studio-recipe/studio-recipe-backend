import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

const recommendDuration = new Trend('recommend_duration');
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
      vus: 100,
      duration: '30s',
      startTime: '10s',
      tags: { phase: 'load' },
    },
    peak_test: {
      executor: 'constant-vus',
      vus: 200,
      duration: '20s',
      startTime: '40s',
      tags: { phase: 'peak' },
    },
  },
  thresholds: {
    'recommend_duration': ['p(95)<5000'],
    'error_rate': ['rate<0.05'],
  },
};

export default function () {
  const userId = Math.floor(Math.random() * 100) + 1;

  const res = http.get(
    `http://localhost:5000/api/recommend?userId=${userId}&k=10&lambda=0.8`,
    { tags: { name: 'flask_recommend' } }
  );

  recommendDuration.add(res.timings.duration);
  errorRate.add(res.status !== 200);

  check(res, {
    '상태코드 200': (r) => r.status === 200,
    '응답시간 5초 이내': (r) => r.timings.duration < 5000,
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
