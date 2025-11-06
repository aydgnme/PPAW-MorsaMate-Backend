# 💳 MorseMate Payment & Stripe Simulation System

## Overview

MorseMate implements a **fully simulated payment and Stripe integration system** for testing and development purposes. This allows the entire payment flow to be tested without actual Stripe API keys or real transactions.

**Status:** ✅ SIMULATION MODE (Ready for production testing)

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     FRONTEND CLIENT                          │
│  (Web/Mobile App - sends payment requests)                  │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│              PAYMENT CONTROLLERS                             │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │ PaymentController│  │ StripeController │                │
│  │  10 endpoints    │  │  10 endpoints    │                │
│  └────────┬─────────┘  └────────┬─────────┘                │
└───────────┼────────────────────┼──────────────────────────┘
            │                     │
            ▼                     ▼
┌───────────────────────┐  ┌──────────────────────────────┐
│   PaymentService      │  │    StripeService             │
│   - Payment CRUD      │  │    - Payment Intent          │
│   - Statistics        │  │    - Customer Management     │
│   - Refunds           │  │    - Webhook Simulation      │
└───────────┬───────────┘  └──────────┬───────────────────┘
            │                          │
            ▼                          ▼
┌──────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                         │
│  ┌──────────────┐    ┌─────────────────────────────┐    │
│  │   Payment    │    │  In-Memory Stripe Objects   │    │
│  │   Entity     │    │  (PaymentIntent, Customer)  │    │
│  └──────────────┘    └─────────────────────────────┘    │
└──────────────────────────────────────────────────────────┘
```

---

## 📦 Components

### 1. Payment System (Core)

**Location:** `src/main/java/me/aydgn/MorseMate/service/PaymentService.java`

**Features:**
- ✅ Create payments (simulated)
- ✅ Payment history tracking
- ✅ Payment statistics (totalSpent, totalRefunded, etc.)
- ✅ Refund processing (simulated)
- ✅ Multi-currency support
- ✅ User ownership validation
- ✅ Admin/User role-based access

**DTOs:**
- `CreatePaymentRequest.java` - Payment creation
- `RefundPaymentRequest.java` - Refund request
- `PaymentResponse.java` - Payment response

**Entity:** `Payment.java`
- Status: PENDING, COMPLETED, FAILED, REFUNDED
- Amount, currency, payment method
- Stripe payment ID (simulated)
- Metadata (JSON)

### 2. Stripe Integration (Simulated)

**Location:** `src/main/java/me/aydgn/MorseMate/service/StripeService.java`

**Features:**
- ✅ Payment Intent creation (simulated)
- ✅ Payment Intent confirmation
- ✅ Payment Intent cancellation
- ✅ Customer management (auto-create)
- ✅ Refund processing
- ✅ Webhook event simulation
- ✅ In-memory object storage

**DTOs:**
- `CreatePaymentIntentRequest.java` - Payment Intent creation
- `PaymentIntentResponse.java` - Payment Intent response
- `StripeCustomerResponse.java` - Customer response

**Simulated Objects:**
- Payment Intent (pi_sim_xxx)
- Customer (cus_sim_xxx)
- Webhook Events (evt_sim_xxx)

---

## 🚀 API Endpoints

### Payment Endpoints (10)

#### User Endpoints
```http
POST   /v1/payments/create              # Create payment
GET    /v1/payments/my                  # Get my payments
GET    /v1/payments/my/stats            # Get my payment stats
GET    /v1/payments/{id}                # Get payment by ID
GET    /v1/payments/subscriptions/{id}  # Get subscription payments
```

#### Admin Endpoints
```http
GET    /v1/payments                     # Get all payments (paginated)
GET    /v1/payments/users/{userId}      # Get user payments
GET    /v1/payments/users/{userId}/stats # Get user stats
POST   /v1/payments/{id}/refund         # Refund payment
```

### Stripe Endpoints (10)

#### User Endpoints
```http
POST   /v1/stripe/create-payment-intent       # Create Payment Intent
POST   /v1/stripe/confirm-payment-intent/{id} # Confirm Payment Intent
POST   /v1/stripe/cancel-payment-intent/{id}  # Cancel Payment Intent
GET    /v1/stripe/payment-intent/{id}         # Get Payment Intent
GET    /v1/stripe/customer                    # Get customer info
POST   /v1/stripe/simulate-webhook            # Simulate webhook (testing)
```

#### Admin Endpoints
```http
GET    /v1/stripe/stats                       # Get simulation stats
POST   /v1/stripe/refund/{id}                 # Refund Payment Intent
DELETE /v1/stripe/clear                       # Clear simulated data
```

#### Webhook Endpoint
```http
POST   /v1/stripe/webhook                     # Webhook handler
```

**Total:** 20 payment-related endpoints

---

## 🎯 Usage Examples

### 1. Direct Payment (Simple Flow)

```bash
# Create a payment directly
curl -X POST http://localhost:8080/v1/payments/create \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "subscriptionId": 1,
    "amount": 9.99,
    "currency": "USD",
    "paymentMethod": "credit_card",
    "simulateFailure": false
  }'

# Response:
{
  "id": 1,
  "userId": 1,
  "amount": 9.99,
  "status": "COMPLETED",
  "stripePaymentId": "sim_abc123",
  "simulated": true
}
```

### 2. Stripe Flow (Standard Payment Intent)

```bash
# Step 1: Create Payment Intent
curl -X POST http://localhost:8080/v1/stripe/create-payment-intent \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 9.99,
    "currency": "usd",
    "planId": 1
  }'

# Response:
{
  "id": "pi_sim_abc123",
  "clientSecret": "pi_sim_abc123_secret_xyz",
  "amount": 999,
  "currency": "usd",
  "status": "requires_confirmation",
  "simulated": true
}

# Step 2: Confirm Payment Intent (frontend would use clientSecret)
curl -X POST http://localhost:8080/v1/stripe/confirm-payment-intent/pi_sim_abc123 \
  -H "Authorization: Bearer {token}"

# Response:
{
  "id": "pi_sim_abc123",
  "status": "succeeded",
  "simulated": true
}
```

### 3. Simulate Payment Failure

```bash
curl -X POST http://localhost:8080/v1/stripe/create-payment-intent \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 9.99,
    "currency": "usd",
    "simulateFailure": true
  }'

# Status will be "requires_payment_method" and confirmation will fail
```

### 4. Get Payment Statistics

```bash
curl -X GET http://localhost:8080/v1/payments/my/stats \
  -H "Authorization: Bearer {token}"

# Response:
{
  "totalPayments": 10,
  "completedPayments": 8,
  "failedPayments": 2,
  "refundedPayments": 1,
  "totalSpent": 79.92,
  "totalRefunded": 9.99,
  "netSpent": 69.93
}
```

### 5. Refund Payment (Admin)

```bash
curl -X POST http://localhost:8080/v1/payments/1/refund \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 9.99,
    "reason": "Customer request"
  }'
```

### 6. Simulate Webhook Event

```bash
curl -X POST http://localhost:8080/v1/stripe/simulate-webhook \
  -H "Authorization: Bearer {token}" \
  -d "eventType=payment_intent.succeeded&paymentIntentId=pi_sim_abc123"
```

---

## 🧪 Testing

### Controller Tests

**PaymentControllerTest.java** (12 test cases)
- ✅ Create payment success
- ✅ Create payment with failure simulation
- ✅ Get my payments
- ✅ Get payment statistics
- ✅ Get payment by ID (ownership check)
- ✅ Admin operations (get all, refund)
- ✅ Authorization tests

**StripeControllerTest.java** (15 test cases)
- ✅ Create Payment Intent
- ✅ Confirm Payment Intent
- ✅ Cancel Payment Intent
- ✅ Get Payment Intent
- ✅ Get customer
- ✅ Webhook handling
- ✅ Admin operations (stats, refund, clear)

### Running Tests

```bash
# Run all payment tests
./gradlew test --tests "*Payment*"

# Run Stripe tests
./gradlew test --tests "*Stripe*"

# Run with coverage
./gradlew test jacocoTestReport
```

---

## 🔐 Security

### Access Control

**User Endpoints:**
- ✅ JWT authentication required
- ✅ User can only access their own payments
- ✅ User ID extracted from JWT token

**Admin Endpoints:**
- ✅ Requires `ROLE_ADMIN`
- ✅ Can access all users' payments
- ✅ Can perform refunds
- ✅ Can view statistics

### Data Protection

- ✅ Payment amounts stored as DECIMAL(10,2)
- ✅ Currency validation (3-letter ISO codes)
- ✅ Payment status validation (enum-based)
- ✅ Metadata stored as JSONB
- ✅ Transaction timestamps tracked

---

## 📊 Database Schema

### Payment Table

```sql
CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subscription_id BIGINT REFERENCES user_subscriptions(id),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(20) CHECK (status IN ('PENDING','COMPLETED','FAILED','REFUNDED')),
    payment_method VARCHAR(50),
    stripe_payment_id VARCHAR(100),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    INDEX idx_payments_user (user_id)
);
```

---

## 🎨 Simulation Features

### What's Simulated?

1. **Payment Processing**
   - No real money charged
   - Instant "payment" completion
   - Configurable success/failure

2. **Stripe Objects**
   - Payment Intents (pi_sim_xxx)
   - Customers (cus_sim_xxx)
   - Webhook Events (evt_sim_xxx)
   - All stored in-memory

3. **Stripe API Behavior**
   - Payment Intent lifecycle (requires_confirmation → succeeded)
   - Customer auto-creation
   - Webhook event generation
   - Refund processing

4. **Responses**
   - All responses marked with `"simulated": true`
   - Realistic data structure
   - Proper status codes

### What's Real?

1. **Database Storage**
   - Payment records stored in PostgreSQL
   - Transaction history preserved
   - Statistics calculations

2. **Authentication**
   - Real JWT validation
   - Role-based access control
   - User ownership checks

3. **Business Logic**
   - Payment validation
   - Refund rules
   - Currency handling
   - Metadata tracking

---

## 🔄 Migration to Real Stripe

When ready to integrate real Stripe:

1. **Add Stripe API Key**
   ```properties
   stripe.api.key=${STRIPE_API_KEY}
   stripe.webhook.secret=${STRIPE_WEBHOOK_SECRET}
   ```

2. **Replace StripeService Logic**
   ```java
   // Before (simulated):
   String paymentIntentId = "pi_sim_" + UUID.randomUUID();

   // After (real Stripe):
   PaymentIntent intent = PaymentIntent.create(params);
   String paymentIntentId = intent.getId();
   ```

3. **Add Webhook Signature Verification**
   ```java
   Event event = Webhook.constructEvent(
       payload, sigHeader, webhookSecret
   );
   ```

4. **Update Tests**
   - Mock Stripe SDK calls
   - Use Stripe test API keys
   - Test webhook signature validation

---

## 📈 Statistics & Monitoring

### Available Metrics

**Payment Statistics:**
- Total payments count
- Completed payments
- Failed payments
- Refunded payments
- Total amount spent
- Total refunded
- Net spent

**Stripe Simulation Stats:**
- Total Payment Intents
- Total Customers
- Succeeded payments
- Failed payments
- Canceled payments

### Admin Dashboard Integration

Statistics can be displayed in the admin panel:

```java
GET /v1/payments/users/{userId}/stats  // User payment stats
GET /v1/stripe/stats                   // Stripe simulation stats
```

---

## 🐛 Troubleshooting

### Common Issues

**Issue:** "User not authenticated"
- **Solution:** Make sure JWT token is included in Authorization header

**Issue:** "Payment Intent not found"
- **Solution:** Payment Intents are stored in-memory. They're cleared on app restart.

**Issue:** "Cannot refund non-completed payment"
- **Solution:** Only payments with status COMPLETED can be refunded

### Testing Tips

1. **Use simulateFailure flag** to test error handling
2. **Clear simulated data** regularly during testing
3. **Check admin stats** to verify simulation behavior
4. **Use webhook simulator** to test event handling

---

## 📚 Documentation

- [API Endpoints](API_ENDPOINTS.md) - Complete API reference
- [API Testing Guide](API-TESTING.md) - Testing examples
- [Postman Collection](POSTMAN_GUIDE.md) - Ready-to-use collections

---

## ✅ Checklist

Payment & Stripe Simulation:
- [x] PaymentService implementation
- [x] PaymentController (10 endpoints)
- [x] StripeService implementation
- [x] StripeController (10 endpoints)
- [x] Payment DTOs (3 files)
- [x] Stripe DTOs (3 files)
- [x] Payment Entity integration
- [x] User ownership validation
- [x] Role-based access control
- [x] Payment statistics
- [x] Refund processing
- [x] Webhook simulation
- [x] Controller tests (27 test cases)
- [x] Comprehensive documentation
- [x] Build & compile successful

**Status:** ✅ 100% Complete (Simulation Mode)

---

## 🎯 Future Enhancements

When real Stripe is integrated:
- [ ] Real Stripe API integration
- [ ] Webhook signature verification
- [ ] Payment receipts (PDF generation)
- [ ] Subscription billing automation
- [ ] Payment retry logic
- [ ] 3D Secure authentication
- [ ] Apple Pay / Google Pay
- [ ] Multiple payment methods
- [ ] Invoice generation
- [ ] Tax calculation

---

**Made with ❤️ for MorseMate**
