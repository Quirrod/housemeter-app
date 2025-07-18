# Push Notifications Implementation

This document describes the push notification system implemented in the HouseMeter application.

## Overview

The push notification system uses Firebase Cloud Messaging (FCM) to send real-time notifications to users about:
- New debts assigned to their apartment
- Payment approval/rejection status
- Debt due date reminders
- New payment receipts (for admins)

## Architecture

### Frontend (Android)
- **Firebase Cloud Messaging**: Handles receiving and displaying notifications
- **NotificationManager**: Manages FCM token registration and permissions
- **FirebaseMessagingService**: Custom service to handle incoming notifications
- **DataStoreManager**: Stores FCM tokens locally

### Backend (Node.js)
- **Firebase Admin SDK**: Sends notifications to specific users or groups
- **NotificationService**: Centralized service for all notification logic
- **Database**: Stores FCM tokens in the users table

## Setup Instructions

### 1. Firebase Project Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select existing project
3. Enable Cloud Messaging in the project
4. Generate a service account key:
   - Project Settings > Service Accounts > Generate New Private Key
   - Download the JSON file

### 2. Android App Configuration

1. Update `google-services.json` with real Firebase project values
2. Ensure these permissions are in AndroidManifest.xml:
   ```xml
   <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
   ```

### 3. Backend Configuration

1. Copy `.env.example` to `.env`
2. Set Firebase environment variables from the service account JSON:
   ```env
   FIREBASE_PROJECT_ID=your-project-id
   FIREBASE_PRIVATE_KEY_ID=your-private-key-id
   FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\nyour-private-key-here\n-----END PRIVATE KEY-----"
   FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxxxx@your-project.iam.gserviceaccount.com
   FIREBASE_CLIENT_ID=your-client-id
   FIREBASE_CLIENT_X509_CERT_URL=https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-xxxxx%40your-project.iam.gserviceaccount.com
   ```

## API Endpoints

### POST /api/notifications/register-token
Register FCM token for the authenticated user.

**Request:**
```json
{
  "fcm_token": "firebase-token-here"
}
```

**Response:**
```json
{
  "message": "FCM token registered successfully"
}
```

### POST /api/notifications/remove-token
Remove FCM token for the authenticated user (logout).

**Response:**
```json
{
  "message": "FCM token removed successfully"
}
```

### POST /api/notifications/test
Send a test notification to the authenticated user.

**Request:**
```json
{
  "title": "Test Title",
  "body": "Test message body"
}
```

## Notification Triggers

### 1. New Debt Created
- **Trigger**: Admin creates a new debt
- **Recipient**: User assigned to the apartment
- **Title**: "Nueva deuda asignada"
- **Body**: "Se te ha asignado una deuda de $[amount]. Vence el [due_date]."

### 2. Payment Approved
- **Trigger**: Admin approves a payment
- **Recipient**: User who made the payment
- **Title**: "Pago aprobado"
- **Body**: "Tu pago de $[amount] para '[debt_description]' ha sido aprobado."

### 3. Payment Rejected
- **Trigger**: Admin rejects a payment
- **Recipient**: User who made the payment
- **Title**: "Pago rechazado"
- **Body**: "Tu pago de $[amount] para '[debt_description]' ha sido rechazado. [reason]"

### 4. New Payment Submitted
- **Trigger**: User submits a payment
- **Recipient**: All admin users
- **Title**: "Nuevo pago recibido"
- **Body**: "[user_name] (Apto [apartment_number]) ha enviado un pago de $[amount]."

### 5. Debt Due Reminder
- **Trigger**: Scheduled job (to be implemented)
- **Recipient**: User with overdue debt
- **Title**: "Recordatorio de vencimiento"
- **Body**: "Tu deuda de $[amount] vence el [due_date]. '[description]'"

## Database Schema

The `users` table includes an FCM token field:

```sql
ALTER TABLE users ADD COLUMN fcm_token VARCHAR(255);
```

## Usage in Android App

### Initialize notifications:
```kotlin
// In MainActivity
val dataStoreManager = DataStoreManager(this)
val notificationManager = NotificationManager(this, dataStoreManager)
notificationManager.requestNotificationPermission(this)
notificationManager.initializeFirebaseMessaging()
```

### Handle notification data:
```kotlin
// In FirebaseMessagingService
override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val title = remoteMessage.notification?.title ?: "HouseMeter"
    val body = remoteMessage.notification?.body ?: "New notification"
    val data = remoteMessage.data
    
    // Handle different notification types based on data
    when (data["type"]) {
        "new_debt" -> {
            // Navigate to debts screen
        }
        "payment_approved" -> {
            // Navigate to payment history
        }
        // ... other types
    }
}
```

## Testing

### Manual Testing
1. Use the test notification endpoint:
   ```bash
   curl -X POST https://your-backend.com/api/notifications/test \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"title": "Test", "body": "Test message"}'
   ```

2. Create a debt or approve a payment to trigger automatic notifications

### Notification Test Screen
The app includes a NotificationTestScreen for testing:
- Send custom test notifications
- View FCM token status
- Refresh FCM tokens

## Deployment Notes

### Railway Deployment
1. Set all Firebase environment variables in Railway dashboard
2. Ensure DATABASE_URL includes FCM token column
3. Deploy backend with notification service

### Important Security Notes
- Never commit Firebase private keys to version control
- Use environment variables for all sensitive configuration
- Validate FCM tokens before storing in database
- Handle token refresh and cleanup for invalid tokens

## Troubleshooting

### Common Issues

1. **Notifications not received**
   - Check FCM token registration in database
   - Verify Firebase project configuration
   - Ensure notification permissions are granted

2. **Token registration fails**
   - Check authentication token validity
   - Verify backend API endpoint is accessible
   - Check network connectivity

3. **Firebase initialization errors**
   - Verify service account key configuration
   - Check Firebase project ID and credentials
   - Ensure Firebase Admin SDK is properly installed

### Debug Steps
1. Check FCM token in app logs
2. Verify token is stored in database
3. Test notification endpoint directly
4. Check Firebase console for delivery status
5. Review backend logs for notification sending errors

## Future Enhancements

1. **Scheduled Notifications**: Implement cron jobs for debt reminders
2. **Notification History**: Store notification history in database
3. **User Preferences**: Allow users to configure notification types
4. **Rich Notifications**: Add images and action buttons
5. **Notification Analytics**: Track delivery and engagement metrics