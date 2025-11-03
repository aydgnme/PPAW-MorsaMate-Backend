// Login Form Handler
document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const loginBtn = document.getElementById('loginBtn');
    const errorMessage = document.getElementById('errorMessage');
    const successMessage = document.getElementById('successMessage');

    // Check if already logged in
    checkExistingAuth();

    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;

        if (!username || !password) {
            showError('Please enter both username and password');
            return;
        }

        await performLogin(username, password);
    });
});

/**
 * Check if user is already authenticated
 */
function checkExistingAuth() {
    const token = localStorage.getItem('authToken');
    const userRole = localStorage.getItem('userRole');

    if (token && userRole) {
        console.log('User already logged in, redirecting...');
        redirectToDashboard(userRole);
    }
}

/**
 * Perform login API call
 */
async function performLogin(username, password) {
    const loginBtn = document.getElementById('loginBtn');
    const btnText = loginBtn.querySelector('.btn-text');
    const btnLoader = loginBtn.querySelector('.btn-loader');

    // Show loading state
    loginBtn.disabled = true;
    btnText.style.display = 'none';
    btnLoader.style.display = 'inline-block';
    hideMessages();

    try {
        const response = await fetch('/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        const data = await response.json();

        if (response.ok) {
            // Login successful
            handleLoginSuccess(data);
        } else {
            // Login failed
            handleLoginError(data);
        }
    } catch (error) {
        console.error('Login error:', error);
        showError('Network error. Please check your connection and try again.');
    } finally {
        // Reset button state
        loginBtn.disabled = false;
        btnText.style.display = 'inline';
        btnLoader.style.display = 'none';
    }
}

/**
 * Handle successful login
 */
function handleLoginSuccess(data) {
    console.log('Login successful:', data);

    // Save auth data
    localStorage.setItem('authToken', data.token);
    localStorage.setItem('userId', data.userId);
    localStorage.setItem('username', data.username);
    localStorage.setItem('userRole', data.role);

    // Show success message
    showSuccess(`Welcome back, ${data.username}! Redirecting to your dashboard...`);

    // Redirect based on role after 1 second
    setTimeout(() => {
        redirectToDashboard(data.role);
    }, 1000);
}

/**
 * Handle login error
 */
function handleLoginError(data) {
    console.error('Login failed:', data);
    const message = data.message || 'Login failed. Please check your credentials.';
    showError(message);
}

/**
 * Redirect to appropriate dashboard based on user role
 */
function redirectToDashboard(role) {
    console.log('Redirecting user with role:', role);

    switch(role) {
        case 'ADMIN':
            window.location.href = '/dashboard/admin';
            break;
        case 'PREMIUM':
            window.location.href = '/dashboard/premium';
            break;
        case 'USER':
        default:
            window.location.href = '/dashboard/user';
            break;
    }
}

/**
 * Fill demo credentials
 */
function fillDemoCredentials(username, password) {
    document.getElementById('username').value = username;
    document.getElementById('password').value = password;

    // Focus on login button
    document.getElementById('loginBtn').focus();

    // Optional: Auto-submit after a short delay
    setTimeout(() => {
        document.getElementById('loginForm').dispatchEvent(new Event('submit'));
    }, 300);
}

/**
 * Show error message
 */
function showError(message) {
    const errorMessage = document.getElementById('errorMessage');
    const successMessage = document.getElementById('successMessage');

    errorMessage.textContent = message;
    errorMessage.style.display = 'block';
    successMessage.style.display = 'none';

    // Auto-hide after 5 seconds
    setTimeout(() => {
        errorMessage.style.display = 'none';
    }, 5000);
}

/**
 * Show success message
 */
function showSuccess(message) {
    const errorMessage = document.getElementById('errorMessage');
    const successMessage = document.getElementById('successMessage');

    successMessage.textContent = message;
    successMessage.style.display = 'block';
    errorMessage.style.display = 'none';
}

/**
 * Hide all messages
 */
function hideMessages() {
    document.getElementById('errorMessage').style.display = 'none';
    document.getElementById('successMessage').style.display = 'none';
}

/**
 * Logout function (can be called from dashboard)
 */
function logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('userRole');
    window.location.href = '/auth/login';
}
