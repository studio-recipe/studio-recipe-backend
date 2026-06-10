import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
  vus: 1,
  duration: '10s',
};

export function setup() {
  const loginRes = http.post(
    'http://localhost:8080/studio-recipe/auth/login',
    JSON.stringify({ id: 'adminuser1', password: 'Admin1234!' }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  return { token: loginRes.json('accessToken') };
}

export default function (data) {
  const headers = { 'Authorization': `Bearer ${data.token}` };

  const minId = 7016813;
  const maxId = 7041374;
  const recipeId = Math.floor(Math.random() * (maxId - minId + 1)) + minId;

  const r1 = http.get('http://localhost:8080/studio-recipe/main-pages?page=0&size=10');
  const r2 = http.get(`http://localhost:8080/studio-recipe/recipes/${recipeId}`);
  const r3 = http.get('http://localhost:8080/studio-recipe/recommend-recipes?k=10&lambda=0.8',
    { headers });

  console.log(`main:${r1.status} detail:${r2.status} recommend:${r3.status} failed:${r1.error||r2.error||r3.error}`);

  sleep(1);
}
