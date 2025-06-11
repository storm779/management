/**
 * Form utilities - Lightweight utilities specific to forms
 * Basic functionality only - complex features in form-enhancements.js
 */

function initializeDateDefaults() {
    const dateInputs = document.querySelectorAll('input[type="date"]');
    const today = new Date().toISOString().split('T')[0];
    
    dateInputs.forEach(input => {
        if (input.hasAttribute('data-default-today') && !input.value) {
            input.value = today;
        }
    });
    
    // Set minimum date for validTo fields
    const validFromInput = document.getElementById('validFrom');
    const validToInput = document.getElementById('validTo');
    
    if (validFromInput && validToInput) {
        validFromInput.addEventListener('change', function() {
            validToInput.min = this.value;
        });
    }
}

function initializeCharacterCounters() {
    // Initialize character counters for common fields
    setupFieldCounter('title', 'titleCount', 200);
    setupFieldCounter('description', 'descCount', 1000);
    setupFieldCounter('header', 'headerCount', 200);
    setupFieldCounter('message', 'messageCount', 1000);
}

function setupFieldCounter(inputId, counterId, maxLength) {
    const input = document.getElementById(inputId);
    const counter = document.getElementById(counterId);
    
    if (input && counter) {
        input.addEventListener('input', function() {
            updateCharacterCount(input, counter, maxLength);
        });
        
        // Initialize count
        updateCharacterCount(input, counter, maxLength);
    }
}

function updateCharacterCount(input, counter, maxLength) {
    const currentLength = input.value.length;
    const remaining = maxLength - currentLength;
    
    counter.textContent = `${currentLength}/${maxLength}`;
    
    if (remaining < 0) {
        counter.classList.add('text-danger');
        counter.classList.remove('text-warning', 'text-muted');
    } else if (remaining < 50) {
        counter.classList.add('text-warning');
        counter.classList.remove('text-danger', 'text-muted');
    } else {
        counter.classList.add('text-muted');
        counter.classList.remove('text-danger', 'text-warning');
    }
}

function updateAllCharCounts() {
    // Update all character counters
    ['title', 'description', 'header', 'message'].forEach(fieldId => {
        updateCharCount(fieldId, fieldId + 'Count', fieldId === 'title' || fieldId === 'header' ? 200 : 1000);
    });
}

function updateCharCount(inputId, counterId, maxLength) {
    const input = document.getElementById(inputId);
    const counter = document.getElementById(counterId);
    if (input && counter) {
        updateCharacterCount(input, counter, maxLength);
    }
}

function initializeMessagePreview() {
    const messageInput = document.getElementById('message');
    if (messageInput) {
        messageInput.addEventListener('input', updateMessagePreview);
        updateMessagePreview(); // Initialize
    }
}

function updateMessagePreview() {
    const messageInput = document.getElementById('message');
    const preview = document.getElementById('messagePreview');
    
    if (messageInput && preview) {
        const message = messageInput.value || 'Your message will appear here...';
        preview.textContent = message;
    }
}

function initializeAlertAutoHide() {
    // Auto-hide alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert:not(.alert-permanent)');
    alerts.forEach(alert => {
        setTimeout(() => {
            if (alert.parentNode) {
                alert.style.transition = 'opacity 0.5s ease-out';
                alert.style.opacity = '0';
                setTimeout(() => {
                    if (alert.parentNode) {
                        alert.remove();
                    }
                }, 500);
            }
        }, 5000);
    });
}

// Export essential functions only
window.FormUtils = {
    initializeDateDefaults,
    initializeCharacterCounters,
    setupFieldCounter,
    updateCharacterCount,
    updateAllCharCounts,
    updateCharCount,
    initializeMessagePreview,
    updateMessagePreview,
    initializeAlertAutoHide
}; 