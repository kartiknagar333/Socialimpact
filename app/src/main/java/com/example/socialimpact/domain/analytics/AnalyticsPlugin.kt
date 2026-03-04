/*
 * File: domain/analytics/AnalyticsPlugin.kt
 * Purpose: Interface for analytics tracking.
 * Why: To allow plugging in different analytics services (Firebase, Mixpanel, etc.) 
 *      without modifying the core logic.
 * Future: Add methods like trackEvent(name, params) here.
 */
package com.example.socialimpact.domain.analytics

interface AnalyticsPlugin {
    fun trackEvent(name: String, params: Map<String, Any> = emptyMap())
}
