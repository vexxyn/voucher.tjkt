document.addEventListener('DOMContentLoaded', function() {
    // Initialize all components
    initTabs();
    loadVoucherPrices();
    initNetworkMonitoring();
    setupFormSubmissions();
    addGlowEffect();
});

function initTabs() {
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabContents = document.querySelectorAll('.tab-content');
    
    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            const tabId = button.getAttribute('data-tab');
            
            // Update active tab button
            tabButtons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');
            
            // Show corresponding tab content
            tabContents.forEach(content => {
                content.classList.remove('active');
                if (content.id === tabId) {
                    content.classList.add('active');
                    // Trigger animation
                    content.style.animation = 'none';
                    void content.offsetWidth; // Trigger reflow
                    content.style.animation = 'fadeIn 0.5s ease';
                }
            });
        });
    });
}

function loadVoucherPrices() {
    // In a real implementation, this would fetch from your API
    const vouchers = [
        { duration: '1 Hour', price: '5.000' },
        { duration: '3 Hours', price: '12.000' },
        { duration: '6 Hours', price: '20.000' },
        { duration: '12 Hours', price: '35.000' },
        { duration: '1 Day', price: '50.000' },
        { duration: '3 Days', price: '120.000' },
        { duration: '1 Week', price: '250.000' },
        { duration: '1 Month', price: '800.000' }
    ];
    
    const voucherList = document.getElementById('voucher-list');
    voucherList.innerHTML = '';
    
    vouchers.forEach(voucher => {
        const li = document.createElement('li');
        li.className = 'animate__animated animate__fadeIn';
        li.style.animationDelay = `${0.1 * vouchers.indexOf(voucher)}s`;
        li.innerHTML = `
            <span class="duration">${voucher.duration}</span>
            <span class="price">Rp${voucher.price}</span>
        `;
        voucherList.appendChild(li);
    });
}

function initNetworkMonitoring() {
    // This would be replaced with actual network monitoring via MikroTik API
    // For demo purposes, we'll simulate network data
    
    // Update speed every 2 seconds
    setInterval(() => {
        // Simulate random speed between 5-100 Mbps
        const speed = Math.floor(Math.random() * 96) + 5;
        const speedElement = document.getElementById('current-speed');
        if (speedElement) {
            speedElement.textContent = `${speed} Mbps`;
            speedElement.classList.add('pulse');
            setTimeout(() => speedElement.classList.remove('pulse'), 500);
        }
        
        // Simulate data usage (in MB)
        const usage = Math.floor(Math.random() * 500) + 100;
        const usageElement = document.getElementById('data-usage');
        if (usageElement) {
            usageElement.textContent = `${usage} MB`;
        }
    }, 2000);
}

function setupFormSubmissions() {
    // User login form
    const userForm = document.getElementById('userForm');
    if (userForm) {
        userForm.addEventListener('submit', handleUserLogin);
    }
    
    // Voucher login form
    const voucherForm = document.getElementById('voucherForm');
    if (voucherForm) {
        voucherForm.addEventListener('submit', handleVoucherLogin);
    }
}

function handleUserLogin(e) {
    e.preventDefault();
    
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const submitBtn = e.target.querySelector('button[type="submit"]');
    
    // Simple validation
    if (!username || !password) {
        showNotification('Please fill in all fields', 'error');
        return;
    }
    
    // Show loading state
    submitBtn.disabled = true;
    submitBtn.textContent = 'Logging in...';
    
    // Simulate API call with timeout
    setTimeout(() => {
        // Randomly simulate success or failure for demo
        const isSuccess = Math.random() > 0.3;
        
        if (isSuccess) {
            showNotification('Login successful! Redirecting...', 'success');
            // Redirect after successful login
            setTimeout(() => {
                window.location.href = 'http://www.example.com';
            }, 2000);
        } else {
            showNotification('Invalid username or password', 'error');
            submitBtn.disabled = false;
            submitBtn.textContent = 'Login';
        }
    }, 1500);
}

function handleVoucherLogin(e) {
    e.preventDefault();
    
    const voucherCode = document.getElementById('voucher-code').value;
    const submitBtn = e.target.querySelector('button[type="submit"]');
    
    if (!voucherCode) {
        showNotification('Please enter a voucher code', 'error');
        return;
    }
    
    // Show loading state
    submitBtn.disabled = true;
    submitBtn.textContent = 'Processing...';
    
    // Simulate API call with timeout
    setTimeout(() => {
        // Randomly simulate success or failure for demo
        const isSuccess = Math.random() > 0.3;
        
        if (isSuccess) {
            showNotification('Voucher accepted! Connecting...', 'success');
            // Start countdown timer (1 hour for demo)
            startVoucherTimer(3600);
            // Redirect after successful login
            setTimeout(() => {
                window.location.href = 'http://www.example.com';
            }, 2000);
        } else {
            showNotification('Invalid voucher code', 'error');
            submitBtn.disabled = false;
            submitBtn.textContent = 'Use Voucher';
        }
    }, 1500);
}

function startVoucherTimer(durationInSeconds) {
    const timeElement = document.getElementById('time-remaining');
    if (!timeElement) return;
    
    let remaining = durationInSeconds;
    
    const timer = setInterval(() => {
        remaining--;
        
        if (remaining <= 0) {
            clearInterval(timer);
            timeElement.textContent = '00:00:00';
            showNotification('Your voucher time has expired', 'warning');
            return;
        }
        
        const hours = Math.floor(remaining / 3600);
        const minutes = Math.floor((remaining % 3600) / 60);
        const seconds = remaining % 60;
        
        timeElement.textContent = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }, 1000);
}

function showNotification(message, type = 'success', duration = 5000) {
    const container = document.getElementById('notification-container');
    if (!container) return;
    
    const notification = document.createElement('div');
    notification.className = `notification ${type} animate__animated animate__fadeInRight`;
    notification.innerHTML = `
        <span>${message}</span>
        <button class="close-btn">&times;</button>
    `;
    
    container.appendChild(notification);
    
    // Close button
    notification.querySelector('.close-btn').addEventListener('click', () => {
        notification.style.animation = 'slideOut 0.5s forwards';
        setTimeout(() => notification.remove(), 500);
    });
    
    // Auto-remove after duration
    if (duration) {
        setTimeout(() => {
            notification.style.animation = 'slideOut 0.5s forwards';
            setTimeout(() => notification.remove(), 500);
        }, duration);
    }
}

function addGlowEffect() {
    const loginButtons = document.querySelectorAll('.login-button');
    loginButtons.forEach(button => {
        button.classList.add('glow-on-hover');
    });
}