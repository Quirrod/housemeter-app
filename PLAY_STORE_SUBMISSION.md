# Google Play Store Submission Guide

This guide will help you submit HouseMeter to the Google Play Store.

## Prerequisites

1. **Google Play Console Account**: You need a Google Play Console account ($25 one-time fee)
2. **Keystore**: Generate a release keystore for signing your app
3. **App Content**: Prepare app descriptions, screenshots, and store listing materials

## Step 1: Generate Release Keystore

```bash
# Generate keystore
./generate_keystore.sh

# Copy template and fill in details
cp keystore.properties.template keystore.properties
# Edit keystore.properties with your actual values
```

## Step 2: Build Release Application

```bash
# Build release AAB and APK
./build_release.sh
```

This will generate:
- `app/build/outputs/bundle/release/app-release.aab` (for Play Store)
- `app/build/outputs/apk/release/app-release.apk` (for testing)

## Step 3: Test Release Build

Before submitting, test the release APK on a physical device:

```bash
# Install on device
adb install app/build/outputs/apk/release/app-release.apk
```

## Step 4: Prepare Store Listing Materials

### App Information
- **App name**: HouseMeter
- **Package name**: com.jarrod.house
- **Category**: Business / Productivity
- **Content rating**: Everyone
- **Target audience**: Adults (property managers, residents)

### Descriptions

#### Short Description (80 characters)
```
Gestión de pagos y deudas para condominios y apartamentos
```

#### Long Description (4000 characters)
```
HouseMeter es una aplicación integral para la gestión de pagos y deudas en condominios y edificios de apartamentos. Diseñada para administradores de propiedades y residentes, facilita el seguimiento de pagos, la gestión de deudas y la comunicación eficiente.

🏢 FUNCIONALIDADES PRINCIPALES:

Para Administradores:
• Gestión completa de apartamentos y pisos
• Creación y seguimiento de deudas
• Aprobación y rechazo de pagos
• Visualización de métricas y reportes
• Notificaciones push automáticas
• Historial completo de transacciones

Para Residentes:
• Visualización de deudas pendientes
• Carga de comprobantes de pago
• Historial de pagos realizados
• Notificaciones de nuevas deudas
• Recordatorios de vencimientos

🔔 NOTIFICACIONES INTELIGENTES:
• Nuevas deudas asignadas
• Confirmación de pagos aprobados
• Recordatorios de vencimientos
• Actualizaciones en tiempo real

🛡️ SEGURIDAD:
• Autenticación segura
• Datos encriptados
• Gestión de permisos por rol
• Backup automático en la nube

📱 INTERFAZ MODERNA:
• Diseño intuitivo y fácil de usar
• Compatible con dispositivos móviles
• Modo claro y oscuro
• Navegación simplificada

HouseMeter simplifica la administración de propiedades y mejora la comunicación entre administradores y residentes, proporcionando transparencia y eficiencia en la gestión de pagos.

Perfecto para:
• Administradores de condominios
• Propietarios de edificios
• Residentes de apartamentos
• Empresas de gestión inmobiliaria
```

### Screenshots Requirements
You'll need to provide screenshots for:
- Phone (at least 2, maximum 8)
- 7-inch tablet (optional)
- 10-inch tablet (optional)

Screenshot dimensions:
- Phone: 1080 x 1920 pixels (portrait) or 1920 x 1080 pixels (landscape)
- 7-inch tablet: 1024 x 1600 pixels
- 10-inch tablet: 1280 x 1920 pixels

### App Icon
- Size: 512 x 512 pixels
- Format: PNG
- Must be high-quality and represent your app

### Feature Graphic
- Size: 1024 x 500 pixels
- Format: PNG or JPEG
- Used in Play Store promotions

## Step 5: Create Google Play Console Account

1. Go to [Google Play Console](https://play.google.com/console)
2. Pay the $25 one-time registration fee
3. Complete developer profile
4. Verify your identity

## Step 6: Create New App in Console

1. Click "Create app"
2. Fill in app details:
   - App name: HouseMeter
   - Default language: Spanish (España)
   - App or game: App
   - Free or paid: Free
   - Declarations: Complete privacy policy and content guidelines

## Step 7: Upload App Bundle

1. Go to "Production" in the left menu
2. Click "Create new release"
3. Upload your AAB file: `app-release.aab`
4. Fill in release notes
5. Review and save

## Step 8: Complete Store Listing

1. Go to "Store listing" in the left menu
2. Fill in all required fields:
   - App name and description
   - Screenshots
   - App icon
   - Feature graphic
   - Categorization
   - Contact details

## Step 9: Content Rating

1. Go to "Content rating"
2. Complete the content rating questionnaire
3. Select "Everyone" rating

## Step 10: Target Audience

1. Go to "Target audience"
2. Select age groups: 18+
3. Complete children's app compliance

## Step 11: Privacy Policy

Create a privacy policy for your app. Key points to include:
- Data collection practices
- How user data is used
- Data storage and security
- User rights and contact information

## Step 12: App Content

1. Go to "App content"
2. Complete all required sections:
   - Privacy policy
   - Ads (if applicable)
   - Content rating
   - Target audience
   - News apps (if applicable)

## Step 13: Release Management

1. Go to "Release" > "Production"
2. Review all sections have green checkmarks
3. Click "Start rollout to production"
4. Confirm release

## Step 14: Review Process

- Google will review your app (typically 1-3 days)
- You'll receive email notifications about the review status
- If approved, your app will be live on Play Store
- If rejected, address the issues and resubmit

## Important Notes

### Security Requirements
- Your app must use HTTPS for all network communications
- Implement proper permission requests
- Follow Android security best practices

### Store Policies
- No misleading content
- Proper age ratings
- Clear privacy policy
- Appropriate content for category

### Testing
- Test on multiple devices
- Test all features thoroughly
- Ensure stable performance
- No crashes or major bugs

### Updates
- Keep your keystore file secure for future updates
- Increment version code for each update
- Provide clear release notes

## Troubleshooting

### Common Rejection Reasons
1. **Missing privacy policy**: Ensure you have a valid privacy policy URL
2. **Permission issues**: Justify all requested permissions
3. **Content violations**: Ensure content follows Play Store policies
4. **Technical issues**: Fix crashes and performance issues

### Support Resources
- [Google Play Console Help](https://support.google.com/googleplay/android-developer)
- [Android Developer Documentation](https://developer.android.com/distribute/googleplay)
- [Play Store Policies](https://play.google.com/about/developer-content-policy/)

## Post-Launch

After your app is live:
1. Monitor reviews and ratings
2. Respond to user feedback
3. Track analytics in Play Console
4. Plan regular updates
5. Monitor crash reports

Good luck with your app submission!