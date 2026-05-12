import http from 'k6/http';
import {check, sleep} from 'k6';
import {Rate} from 'k6/metrics';

const rateLimitSuccessRate = new Rate('rate_limit_429_success');
const cacheHitSuccessRate = new Rate('cache_200_success');

export const options = {
    scenarios: {
        redis_cache_test: {
            executor: 'constant-vus',
            vus: 20,
            duration: '30s',
            exec: 'testRedis',
        },
        rate_limit_test: {
            executor: 'shared-iterations',
            vus: 1,
            iterations: 30,
            maxDuration: '10s',
            exec: 'testRateLimit',
        }
    }
};

const BASE_URL = 'http://localhost:8080';

export function setup() {
    const entRes = http.post(`${BASE_URL}/auth/token`, JSON.stringify({
        username: "bob",
        apiKey: "key-bob-002"
    }), {headers: {'Content-Type': 'application/json'}});

    const freeRes = http.post(`${BASE_URL}/auth/token`, JSON.stringify({
        username: "alice",
        apiKey: "key-alice-001"
    }), {headers: {'Content-Type': 'application/json'}});

    const entToken = entRes.json('token');
    const freeToken = freeRes.json('token');

    const trackingNumber = `SH-${__VU}-${Date.now()}`;
    http.post(`${BASE_URL}/shipments`, JSON.stringify({
        trackingNumber: trackingNumber,
        carrier: "DHL",
        origin: "Hamburg",
        destination: "Berlin"
    }), {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${entToken}`
        }
    });

    return {entToken, freeToken, trackingNumber};
}

export function testRedis(data) {
    const res = http.get(`${BASE_URL}/shipments/${data.trackingNumber}`, {
        headers: {'Authorization': `Bearer ${data.entToken}`}
    });

    const success = check(res, {
        'is status 200 (Cache)': (r) => r.status === 200,
    });
    cacheHitSuccessRate.add(success);
    sleep(0.1);
}

export function testRateLimit(data) {
    const res = http.get(`${BASE_URL}/shipments/${data.trackingNumber}`, {
        headers: {'Authorization': `Bearer ${data.freeToken}`}
    });

    const remaining = res.headers['X-Rate-Limit-Remaining'];

    if (res.status === 429) {
        rateLimitSuccessRate.add(1);
    } else if (res.status === 200) {
        rateLimitSuccessRate.add(0);
    }
}
