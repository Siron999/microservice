import http from "k6/http";
import { check, sleep } from "k6";

export let options = {
  stages: [
    { duration: "1m", target: 1000 }, // Ramp-up to 1000 users
    { duration: "1m", target: 2000 }, // Hold at 2000 users
    { duration: "1m", target: 0 }, // Ramp-down
  ],
  thresholds: {
    http_req_duration: ["p(95)<500"], // 95% requests <500ms
  },
};

export default function () {
  const headers = {
    "Content-Type": "application/json",
    Authorization:
      "Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJzaXJvbnNoYWt5YSIsImV4cCI6MTc0MjAyOTM2Mn0.feDdp9olbY2aJj-ur44bZU3JXpn_wQmPoJ-63Coix4dbC-Qs0MEl7jG3QVVMhhkt",
    Accept: "application/json",
  };

  let res = http.get("http://localhost:8081/api/auth/authenticate", {
    headers,
  });

  check(res, {
    "status is 200": (r) => r.status === 200,
    "response time < 500ms": (r) => r.timings.duration < 500,
  });

  sleep(1);
}
