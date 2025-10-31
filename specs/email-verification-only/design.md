# Design Document - Email Verification Only System

## Overview

This design implements a streamlined authentication system focused exclusively on email verification for new account registration. The system eliminates complex authentication methods and provides a single, reliable path for user onboarding through email confirmation.

## Architecture

### System Components

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Backend       │    │   Email         │
│   (app.html)    │◄──►│   (server.js)   │◄──►│   Service       │
│                 │    │                 │    │   (SMTP)        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Language      │    │   Database      │    │   Email         │
│   System        │    │   (SQLite)      │    │   Templates     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Data Flow

1. **Registration Request** → Frontend validates → Backend processes
2. **Account Creation** → Database stores → Email service triggers
3. **Code Delivery** → Email sent → User receives code
4. **Verification** → User enters code → Backend validates → Account activated

## Components and Interfaces

### Frontend Components

#### 1. Registration Form
```javascript
// Registration form with validation
{
  name: string,           // User's full name
  email: string,          // Email address (validated)
  password: string,       // Password (min 8 chars)
  language: string        // Selected language (ar/en/el)
}
```

#### 2. Verification Interface
```javascript
// Code input interface
{
  verificationCode: string,  // 4-digit code
  email: string,            // User's email
  resendAvailable: boolean  // Can resend code
}
```

#### 3. Language Switcher
```javascript
// Multilingual support
{
  currentLanguage: 'ar' | 'en' | 'el',
  translations: {
    ar: { /* Arabic translations */ },
    en: { /* English translations */ },
    el: { /* Greek translations */ }
  }
}
```

### Backend API Endpoints

#### 1. POST /auth/register
```javascript
// Create new account and send verification
Request: {
  name: string,
  email: string,
  password: string,
  language?: string
}

Response: {
  success: boolean,
  message: string,
  userId: string,
  emailSent: boolean,
  verificationCode?: string  // Only in development
}
```

#### 2. POST /auth/verify
```javascript
// Verify email code and activate account
Request: {
  userId: string,
  code: string
}

Response: {
  success: boolean,
  message: string,
  token?: string,
  user?: {
    id: string,
    name: string,
    email: string,
    isVerified: boolean
  }
}
```

#### 3. POST /auth/resend-code
```javascript
// Resend verification code
Request: {
  userId: string,
  email: string
}

Response: {
  success: boolean,
  message: string,
  emailSent: boolean,
  newCode?: string  // Only in development
}
```

## Data Models

### User Model
```sql
CREATE TABLE users (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    is_verified INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### Verification Code Model
```sql
CREATE TABLE verification_codes (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    code TEXT NOT NULL,
    type TEXT DEFAULT 'registration',
    expires_at DATETIME NOT NULL,
    used INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id)
);
```

### Session Model
```sql
CREATE TABLE sessions (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    token TEXT NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id)
);
```

## Email Templates

### Arabic Template (RTL)
```html
<div dir="rtl" style="font-family: Arial, sans-serif;">
    <h1 style="color: #667eea;">🛡️ Secure Guardian Pro</h1>
    <h2>رمز التحقق</h2>
    <div style="font-size: 32px; color: #667eea;">{{CODE}}</div>
    <p>هذا الرمز صالح لمدة 10 دقائق</p>
</div>
```

### English Template (LTR)
```html
<div style="font-family: Arial, sans-serif;">
    <h1 style="color: #667eea;">🛡️ Secure Guardian Pro</h1>
    <h2>Verification Code</h2>
    <div style="font-size: 32px; color: #667eea;">{{CODE}}</div>
    <p>This code is valid for 10 minutes</p>
</div>
```

### Greek Template (LTR)
```html
<div style="font-family: Arial, sans-serif;">
    <h1 style="color: #667eea;">🛡️ Secure Guardian Pro</h1>
    <h2>Κωδικός Επαλήθευσης</h2>
    <div style="font-size: 32px; color: #667eea;">{{CODE}}</div>
    <p>Αυτός ο κωδικός ισχύει για 10 λεπτά</p>
</div>
```

## Error Handling

### Frontend Error Handling
```javascript
const errorHandling = {
  validation: {
    invalidEmail: "Please enter a valid email address",
    weakPassword: "Password must be at least 8 characters",
    missingFields: "Please fill all required fields"
  },
  
  network: {
    connectionError: "Connection failed, please try again",
    serverError: "Server error, please try again later",
    timeout: "Request timeout, please try again"
  },
  
  verification: {
    invalidCode: "Invalid verification code",
    expiredCode: "Code expired, please request a new one",
    tooManyAttempts: "Too many attempts, please wait"
  }
};
```

### Backend Error Handling
```javascript
const errorResponses = {
  400: { error: "Bad Request - Invalid input data" },
  409: { error: "Email already exists" },
  429: { error: "Too many requests - Rate limited" },
  500: { error: "Internal server error" }
};
```

## Testing Strategy

### Unit Tests
- Email validation functions
- Password strength validation
- Code generation and verification
- Database operations

### Integration Tests
- Registration flow end-to-end
- Email sending and verification
- Language switching functionality
- Error handling scenarios

### Manual Testing
- Test with real email addresses
- Verify multilingual support
- Test error conditions
- Verify security measures

### Development Testing
```javascript
// Test codes that always work
const testCodes = ['1234', '0000', '9999'];

// Console logging for development
if (process.env.NODE_ENV === 'development') {
    console.log(`Verification code: ${generatedCode}`);
}
```

## Security Considerations

### Password Security
- Minimum 8 characters required
- Bcrypt hashing with salt rounds ≥ 12
- No password storage in plain text

### Code Security
- 4-digit random codes (1000-9999)
- 10-minute expiration time
- Single-use codes (marked as used after verification)
- Rate limiting on code requests

### Email Security
- SMTP over TLS/SSL
- No sensitive data in email content
- Secure email templates
- Proper error handling for email failures

### Session Security
- JWT tokens with expiration
- Secure token storage
- Session cleanup on logout
- HTTPS enforcement in production