package nl.greaper.bnplanner.model.osu

/**
 * avatar_url
 * country_code
 * default_group
 * id
 * is_active
 * is_bot
 * is_online
 * is_supporter
 * last_visit
 * pm_friends_only
 * profile_colour
 * username
 * cover_url
 * discord
 * has_supported
 * interests
 * join_date
 * kudosu
 * lastfm
 * location
 * max_blocks
 * max_friends
 * occupation
 * playmode
 * playstyle
 * post_count
 * profile_order
 * skype
 * title
 * twitter
 * website
 * country
 * cover
 * account_history
 * active_tournament_banner
 * badges
 * favourite_beatmapset_count
 * follower_count
 * graveyard_beatmapset_count
 * group_badge
 * loved_beatmapset_count
 * monthly_playcounts
 * page
 * previous_usernames
 * ranked_and_approved_beatmapset_count
 * replays_watched_counts
 * scores_first_count
 * statistics
 * support_level
 * unranked_beatmapset_count
 * user_achievements
 * rankHistory
 */
data class Me(
        val id: Long,
        val username: String,
        val group_badge: GroupBadge?, // FIXME when https://github.com/ppy/osu-web/pull/6013 is merged
        val previous_usernames: List<String>
)