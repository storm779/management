/**
 * Form Utilities - Shared functionality for form pages
 * Handles validation, character counting, auto-resize, and form interactions
 */

// Character counter functionality
function addCharacterCounter(field, limit) {
    const counterId = field.id + 'Counter';
    let counter = document.getElementById(counterId);
    
    if (!counter) {
        counter = document.createElement('div');
        counter.id = counterId;
        counter.className = 'character-counter form-text';
        field.parentNode.appendChild(counter);
    }
    
    function updateCounter() {
        const current = field.value.length;
        const remaining = limit - current;
        
        counter.textContent = `${current}/${limit} characters`;
        
        // Update styling based on remaining characters
        counter.className = 'character-counter form-text';
        
        if (remaining < 0) {
            counter.classList.add('text-danger');
            field.classList.add('is-invalid');
        } else if (remaining < 20) {
            counter.classList.add('text-warning');
            field.classList.remove('is-invalid');
        } else {
            counter.classList.add('text-muted');
            field.classList.remove('is-invalid');
        }
    }
    
    updateCounter();
    field.addEventListener('input', updateCounter);
    field.addEventListener('paste', () => setTimeout(updateCounter, 0));
}

// Auto-resize textarea functionality
function makeTextareaAutoResize(textarea) {
    function resize() {
        textarea.style.height = 'auto';
        textarea.style.height = Math.max(textarea.scrollHeight, 100) + 'px';
    }
    
    textarea.addEventListener('input', resize);
    textarea.addEventListener('change', resize);
    setTimeout(resize, 0);
}

// Field validation
function validateField(field) {
    const isValid = field.checkValidity();
    
    if (isValid) {
        field.classList.remove('is-invalid');
        field.classList.add('is-valid');
    } else {
        field.classList.remove('is-valid');
        field.classList.add('is-invalid');
    }
    
    return isValid;
}

// Priority preview setup
function setupPriorityPreview(prioritySelect) {
    const previewContainer = document.createElement('div');
    previewContainer.className = 'mt-2';
    previewContainer.innerHTML = '<small class="text-muted">Preview:</small><br><span id="priorityPreview" class="priority-preview"></span>';
    prioritySelect.parentNode.appendChild(previewContainer);
    
    const preview = document.getElementById('priorityPreview');
    
    function updatePriorityPreview() {
        const value = prioritySelect.value;
        const text = prioritySelect.options[prioritySelect.selectedIndex].text;
        
        preview.textContent = text;
        preview.className = 'priority-preview';
        
        if (value) {
            preview.classList.add(`priority-${value}`);
        }
    }
    
    prioritySelect.addEventListener('change', updatePriorityPreview);
    updatePriorityPreview();
}

// Scrolling message preview
function setupScrollingPreview(messageField) {
    const previewContainer = document.createElement('div');
    previewContainer.id = 'scrollingPreview';
    previewContainer.className = 'scrolling-preview';
    previewContainer.innerHTML = `
        <h6><i class="bi bi-eye me-2"></i>Scrolling Message Preview</h6>
        <div class="scrolling-text" id="scrollingText">Enter your message above to see the preview</div>
    `;
    messageField.parentNode.appendChild(previewContainer);
    
    const scrollingText = document.getElementById('scrollingText');
    
    function updateScrollingPreview() {
        const message = messageField.value.trim();
        scrollingText.textContent = message || 'Enter your message above to see the preview';
    }
    
    messageField.addEventListener('input', updateScrollingPreview);
    updateScrollingPreview();
}

// Additional utility functions for forms
function initializeDateDefaults() {
    const validFromInput = document.getElementById('validFrom');
    if (validFromInput && !validFromInput.value) {
        const today = new Date().toISOString().split('T')[0];
        validFromInput.value = today;
        validFromInput.min = today;
    }

    // Set minimum date for validTo based on validFrom
    if (validFromInput) {
        validFromInput.addEventListener('change', function() {
            const validToInput = document.getElementById('validTo');
            if (validToInput) {
                validToInput.min = this.value;
                if (validToInput.value && validToInput.value < this.value) {
                    validToInput.value = '';
                }
            }
        });
    }
}

function initializeCharacterCounters() {
    // Setup counters for common fields
    setupFieldCounter('header', 'headerCount', 200);
    setupFieldCounter('headerHindi', 'headerHindiCount', 200);
    setupFieldCounter('message', 'messageCount', 1000);
    setupFieldCounter('messageHindi', 'messageHindiCount', 1000);
    setupFieldCounter('title', 'titleCount', 200);
    setupFieldCounter('description', 'descriptionCount', 1000);
}

function setupFieldCounter(inputId, counterId, maxLength) {
    const input = document.getElementById(inputId);
    const counter = document.getElementById(counterId);
    
    if (input && counter) {
        input.addEventListener('input', () => {
            updateCharacterCount(input, counter, maxLength);
            
            // Update preview if this is a preview field
            if (inputId === 'header' || inputId === 'message') {
                updateMessagePreview();
            }
        });
        
        // Initialize count
        updateCharacterCount(input, counter, maxLength);
    }
}

function updateCharacterCount(input, counter, maxLength) {
    const currentLength = input.value.length;
    counter.textContent = currentLength;
    
    // Color coding based on usage
    const parent = counter.parentElement;
    parent.classList.remove('text-warning', 'text-danger');
    
    if (currentLength > maxLength * 0.9) {
        parent.classList.add('text-warning');
    }
    
    if (currentLength >= maxLength) {
        parent.classList.add('text-danger');
    }
}

function updateAllCharCounts() {
    updateCharCount('header', 'headerCount', 200);
    updateCharCount('headerHindi', 'headerHindiCount', 200);
    updateCharCount('message', 'messageCount', 1000);
    updateCharCount('messageHindi', 'messageHindiCount', 1000);
}

function updateCharCount(inputId, counterId, maxLength) {
    const input = document.getElementById(inputId);
    const counter = document.getElementById(counterId);
    if (input && counter) {
        updateCharacterCount(input, counter, maxLength);
    }
}

function initializeMessagePreview() {
    updateMessagePreview();
}

function updateMessagePreview() {
    const header = document.getElementById('header');
    const message = document.getElementById('message');
    const previewHeader = document.getElementById('previewHeader');
    const previewMessage = document.getElementById('previewMessage');
    
    if (header && message && previewHeader && previewMessage) {
        previewHeader.textContent = header.value || 'Message Header';
        previewMessage.textContent = message.value || 'Message content will appear here...';
    }
}

function initializeAlertAutoHide() {
    setTimeout(() => {
        const alerts = document.querySelectorAll('.alert');
        alerts.forEach(alert => {
            if (window.bootstrap && window.bootstrap.Alert) {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }
        });
    }, 5000);
}

// Export functions for global use
window.FormUtils = {
    addCharacterCounter,
    makeTextareaAutoResize,
    validateField,
    setupPriorityPreview,
    setupScrollingPreview,
    initializeDateDefaults,
    initializeCharacterCounters,
    initializeMessagePreview,
    initializeAlertAutoHide,
    updateMessagePreview,
    updateAllCharCounts
}; 