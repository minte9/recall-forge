# Architecture / API / Webhooks / Webhook Basics

## 1. Push vs Polling

Polling asks "anything new?" on a timer -- wasteful when idle, laggy when busy. A webhook inverts control: the provider sends an HTTP POST to a URL you registered the moment an event happens. You trade a cron job for an always-available endpoint and the operational burden of receiving traffic.

```
polling:  GET /events?since=... every 30s   (mostly empty)
webhook:  provider -> POST https://you/hooks  on each event
```

## 2. Event Payloads & Delivery

A delivery is a POST whose JSON body describes one event: a type, an id, a timestamp, and the changed data or a reference to fetch it. Include enough to act without a callback, but treat the payload as a notification, not the system of record. Respond 2xx fast and do the real work asynchronously.

```json
{ "id": "evt_9f1", "type": "order.shipped",
  "created": 1699900000, "data": { "orderId": 42 } }
```

## 3. Subscription Management

Consumers register endpoints and choose which event types they want; providers offer a management API or dashboard for this. Support multiple endpoints per account, a way to disable a failing one, and a test ping. Send a challenge request at registration to confirm the subscriber controls the URL.

```
POST /webhook-endpoints
{ "url": "https://you/hooks", "events": ["order.*"] }
```
