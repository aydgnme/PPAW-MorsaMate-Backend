// Admin Login Functionality

// API Configuration
const API_BASE_URL = window.location.origin;
const API_VERSION = 'v1';

// DOM Elements
const loginForm = document.getElementById('loginForm');
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const loginButton = document.getElementById('loginButton');
const loginText = document.getElementById('loginText');
const loginLoader = document.getElementById('loginLoader');
const errorMessage = document.getElementById('errorMessage');
const errorText = document.getElementById('errorText');
const successMessage = document.getElementById('successMessage');

// Check if already logged in
document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('adminToken');
    if (token) {
        // Verify token is still valid
        verifyToken(token).then(valid => {
            if (valid) {
                window.location.href = '/admin/dashboard';
            } else {
                localStorage.removeItem('adminToken');
                localStorage.removeItem('adminUser');
            }
        });
    }
});

// Toggle Password Visibility
function togglePassword() {
    const passwordInput = document.getElementById('password');
    const eyeIcon = document.getElementById('eye-icon');

    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        eyeIcon.innerHTML = `
            <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
            <line x1="1" y1="1" x2="23" y2="23"/>
        `;
    } else {
        passwordInput.type = 'password';
        eyeIcon.innerHTML = `
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
            <circle cx="12" cy="12" r="3"/>
        `;
    }
}

// Verify Token
async function verifyToken(token) {
    try {
        const response = await fetch(`${API_BASE_URL}/${API_VERSION}/users/me`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        return response.ok;
    } catch (error) {
        console.error('Token verification failed:', error);
        return false;
    }
}

// Show/Hide Messages
function showError(message) {
    errorText.textContent = message;
    errorMessage.style.display = 'flex';
    successMessage.style.display = 'none';
}

function showSuccess(message) {
    if (message) {
        successMessage.querySelector('span').textContent = message;
    }
    successMessage.style.display = 'flex';
    errorMessage.style.display = 'none';
}

function hideMessages() {
    errorMessage.style.display = 'none';
    successMessage.style.display = 'none';
}

// Set Loading State
function setLoading(isLoading) {
    loginButton.disabled = isLoading;

    if (isLoading) {
        loginText.style.display = 'none';
        loginLoader.style.display = 'block';
    } else {
        loginText.style.display = 'block';
        loginLoader.style.display = 'none';
    }
}

// Handle Login Form Submit
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMessages();

    const username = usernameInput.value.trim();
    const password = passwordInput.value;
    const rememberMe = document.getElementById('rememberMe').checked;

    // Validation
    if (!username || !password) {
        showError('Please enter both username and password');
        return;
    }

    setLoading(true);

    try {
        // Call login API
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        const data = await response.json();

        if (response.ok) {
            // Login successful
            const { token, user } = data;

            // Check if user is admin
            if (user.role !== 'ADMIN') {
                showError('Access denied. Admin privileges required.');
                setLoading(false);
                return;
            }

            // Store token and user info
            localStorage.setItem('adminToken', token);
            localStorage.setItem('adminUser', JSON.stringify(user));

            if (rememberMe) {
                // Set expiry for 30 days
                const expiry = new Date();
                expiry.setDate(expiry.getDate() + 30);
                localStorage.setItem('adminTokenExpiry', expiry.toISOString());
            }

            showSuccess('Login successful! Redirecting to dashboard...');

            // Redirect to dashboard after 1 second
            setTimeout(() => {
                window.location.href = '/admin/dashboard';
            }, 1000);

        } else {
            // Login failed
            let errorMsg = 'Invalid username or password';

            if (data.message) {
                errorMsg = data.message;
            } else if (response.status === 401) {
                errorMsg = 'Invalid credentials. Please try again.';
            } else if (response.status === 429) {
                errorMsg = 'Too many login attempts. Please try again later.';
            } else if (response.status >= 500) {
                errorMsg = 'Server error. Please try again later.';
            }

            showError(errorMsg);
            setLoading(false);
        }

    } catch (error) {
        console.error('Login error:', error);
        showError('Connection error. Please check your internet connection and try again.');
        setLoading(false);
    }
});

// Input validation feedback
usernameInput.addEventListener('input', () => {
    if (errorMessage.style.display === 'flex') {
        hideMessages();
    }
});

passwordInput.addEventListener('input', () => {
    if (errorMessage.style.display === 'flex') {
        hideMessages();
    }
});

// Forgot Password Handler
document.querySelector('.forgot-password')?.addEventListener('click', (e) => {
    e.preventDefault();
    alert('Password reset functionality coming soon! Please contact system administrator.');
});

// Enter key in username field should focus password
usernameInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        e.preventDefault();
        passwordInput.focus();
    }
});
