package com.example.socialimpact.di.module

import dagger.Module

/**
 * AnalyticsModule — collects all analytics providers into a Set.
 *
 * Currently empty. When you add analytics:
 * 1. Create interface: domain/plugin/AnalyticsPlugin.kt
 *    interface AnalyticsPlugin { fun track(event: String) }
 *
 * 2. Create implementation: data/plugin/FirebaseAnalyticsPlugin.kt
 *    class FirebaseAnalyticsPlugin @Inject constructor() : AnalyticsPlugin
 *
 * 3. Add @Binds @IntoSet here — AuthViewModel gets ALL plugins automatically
 *
 * Why @IntoSet?
 * Every time you add a new analytics provider, just add one line here.
 * AuthViewModel never changes — it receives Set<AnalyticsPlugin> and
 * calls forEach { it.track(event) } automatically. ✅
 *
 * ─── FUTURE ───────────────────────────────────────────────────────────
 * @Binds @IntoSet
 * abstract fun bindFirebaseAnalytics(
 *     impl: FirebaseAnalyticsPlugin
 * ): AnalyticsPlugin
 *
 * @Binds @IntoSet
 * abstract fun bindMixpanel(
 *     impl: MixpanelPlugin
 * ): AnalyticsPlugin
 *
 * @Binds @IntoSet
 * abstract fun bindCrashlytics(
 *     impl: CrashlyticsPlugin
 * ): AnalyticsPlugin
 * ──────────────────────────────────────────────────────────────────────
 */
@Module
abstract class AnalyticsModule
