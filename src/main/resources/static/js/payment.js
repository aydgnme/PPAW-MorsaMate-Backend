/**
 * MorseMate Payment System - JavaScript Client
 *
 * SIMULATION MODE:
 * This client handles simulated payment processing without actual Stripe integration.
 *
 * Features:
 * - Create payments
 * - Create Payment Intents (Stripe flow)
 * - Confirm payments
 * - View payment history
 * - Display payment statistics
 */

class PaymentClient {
    constructor() {
        this.baseUrl = '/v1';
        this.token = this.getToken();
    }

    /**
     * Get JWT token from localStorage
     */
    getToken() {
        return localStorage.getItem('jwt_token') || '';
    }

    /**
     * Get authorization headers
     */
    getHeaders() {
        return {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.token}`
        };
    }

    /**
     * Create a direct payment (simulated)
     *
     * @param {Object} paymentData - Payment information
     * @param {number} paymentData.amount - Payment amount
     * @param {string} paymentData.currency - Currency code (USD, EUR, etc.)
     * @param {string} paymentData.paymentMethod - Payment method
     * @param {number} paymentData.subscriptionId - Subscription ID (optional)
     * @param {boolean} paymentData.simulateFailure - Simulate failure (optional)
     */
    async createPayment(paymentData) {
        try {
            const response = await fetch(`${this.baseUrl}/payments/create`, {
                method: 'POST',
                headers: this.getHeaders(),
                body: JSON.stringify(paymentData)
            });

            if (!response.ok) {
                throw new Error(`Payment failed: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Create payment error:', error);
            throw error;
        }
    }

    /**
     * Create a Stripe Payment Intent (simulated)
     *
     * @param {Object} intentData - Payment Intent information
     * @param {number} intentData.amount - Payment amount
     * @param {string} intentData.currency - Currency code
     * @param {number} intentData.planId - Plan ID (optional)
     * @param {string} intentData.promoCode - Promo code (optional)
     */
    async createPaymentIntent(intentData) {
        try {
            const response = await fetch(`${this.baseUrl}/stripe/create-payment-intent`, {
                method: 'POST',
                headers: this.getHeaders(),
                body: JSON.stringify(intentData)
            });

            if (!response.ok) {
                throw new Error(`Payment Intent creation failed: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Create Payment Intent error:', error);
            throw error;
        }
    }

    /**
     * Confirm a Payment Intent (simulated)
     *
     * @param {string} paymentIntentId - Payment Intent ID
     */
    async confirmPaymentIntent(paymentIntentId) {
        try {
            const response = await fetch(`${this.baseUrl}/stripe/confirm-payment-intent/${paymentIntentId}`, {
                method: 'POST',
                headers: this.getHeaders()
            });

            if (!response.ok) {
                throw new Error(`Payment confirmation failed: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Confirm Payment Intent error:', error);
            throw error;
        }
    }

    /**
     * Get user's payment history
     */
    async getMyPayments() {
        try {
            const response = await fetch(`${this.baseUrl}/payments/my`, {
                headers: this.getHeaders()
            });

            if (!response.ok) {
                throw new Error(`Failed to fetch payments: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Get payments error:', error);
            throw error;
        }
    }

    /**
     * Get payment statistics
     */
    async getPaymentStats() {
        try {
            const response = await fetch(`${this.baseUrl}/payments/my/stats`, {
                headers: this.getHeaders()
            });

            if (!response.ok) {
                throw new Error(`Failed to fetch stats: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Get payment stats error:', error);
            throw error;
        }
    }

    /**
     * Get payment by ID
     *
     * @param {number} paymentId - Payment ID
     */
    async getPayment(paymentId) {
        try {
            const response = await fetch(`${this.baseUrl}/payments/${paymentId}`, {
                headers: this.getHeaders()
            });

            if (!response.ok) {
                throw new Error(`Failed to fetch payment: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Get payment error:', error);
            throw error;
        }
    }

    /**
     * Get Stripe customer info
     */
    async getCustomer() {
        try {
            const response = await fetch(`${this.baseUrl}/stripe/customer`, {
                headers: this.getHeaders()
            });

            if (!response.ok) {
                if (response.status === 404) {
                    return null; // No customer yet
                }
                throw new Error(`Failed to fetch customer: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Get customer error:', error);
            throw error;
        }
    }

    /**
     * Format amount for display
     *
     * @param {number} amount - Amount to format
     * @param {string} currency - Currency code
     */
    formatAmount(amount, currency = 'USD') {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: currency
        }).format(amount);
    }

    /**
     * Format date for display
     *
     * @param {string} dateString - ISO date string
     */
    formatDate(dateString) {
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }
}

/**
 * Payment UI Helper Functions
 */
class PaymentUI {
    constructor(client) {
        this.client = client;
    }

    /**
     * Show loading indicator
     */
    showLoading(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            element.innerHTML = '<div class="loading">Processing...</div>';
            element.classList.add('loading-state');
        }
    }

    /**
     * Hide loading indicator
     */
    hideLoading(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            element.classList.remove('loading-state');
        }
    }

    /**
     * Show success message
     */
    showSuccess(message, elementId = 'payment-message') {
        const element = document.getElementById(elementId);
        if (element) {
            element.innerHTML = `
                <div class="alert alert-success">
                    <span>✓</span> ${message}
                </div>
            `;
            setTimeout(() => {
                element.innerHTML = '';
            }, 5000);
        }
    }

    /**
     * Show error message
     */
    showError(message, elementId = 'payment-message') {
        const element = document.getElementById(elementId);
        if (element) {
            element.innerHTML = `
                <div class="alert alert-error">
                    <span>✗</span> ${message}
                </div>
            `;
        }
    }

    /**
     * Render payment history
     */
    renderPaymentHistory(payments, containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;

        if (!payments || payments.length === 0) {
            container.innerHTML = '<p class="empty-state">No payments yet</p>';
            return;
        }

        const html = `
            <div class="payment-list">
                ${payments.map(payment => `
                    <div class="payment-item ${payment.status.toLowerCase()}">
                        <div class="payment-info">
                            <div class="payment-amount">
                                ${this.client.formatAmount(payment.amount, payment.currency)}
                            </div>
                            <div class="payment-method">${payment.paymentMethod}</div>
                            <div class="payment-date">${this.client.formatDate(payment.transactionDate)}</div>
                        </div>
                        <div class="payment-status">
                            <span class="status-badge status-${payment.status.toLowerCase()}">
                                ${payment.status}
                            </span>
                            ${payment.simulated ? '<span class="simulated-badge">SIMULATED</span>' : ''}
                        </div>
                    </div>
                `).join('')}
            </div>
        `;

        container.innerHTML = html;
    }

    /**
     * Render payment statistics
     */
    renderPaymentStats(stats, containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;

        const html = `
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-label">Total Payments</div>
                    <div class="stat-value">${stats.totalPayments}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Completed</div>
                    <div class="stat-value">${stats.completedPayments}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Failed</div>
                    <div class="stat-value">${stats.failedPayments}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Total Spent</div>
                    <div class="stat-value">${this.client.formatAmount(stats.totalSpent)}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Total Refunded</div>
                    <div class="stat-value">${this.client.formatAmount(stats.totalRefunded)}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Net Spent</div>
                    <div class="stat-value">${this.client.formatAmount(stats.netSpent)}</div>
                </div>
            </div>
        `;

        container.innerHTML = html;
    }
}

// Initialize global payment client
window.paymentClient = new PaymentClient();
window.paymentUI = new PaymentUI(window.paymentClient);

console.log('MorseMate Payment System initialized (Simulation Mode)');
