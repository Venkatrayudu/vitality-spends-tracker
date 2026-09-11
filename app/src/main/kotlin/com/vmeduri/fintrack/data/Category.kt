package com.vmeduri.fintrack.data

/**
 * The spend categories tracked by the app.
 *
 * HEALTHY_FOOD and HEALTHY_CARE exist so you can see, at a glance, how much of your
 * card spend this month falls into those two categories (handy for programmes like
 * Discovery's Vitality Money that reward healthy-category spend). OTHER is everything
 * else. All three categories count towards the weekly and monthly total spend goals.
 */
enum class Category(val displayName: String) {
    HEALTHY_FOOD("Healthy Food"),
    HEALTHY_CARE("Healthy Care"),
    OTHER("Other")
}
