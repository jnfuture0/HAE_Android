package org.hoe.Struct

data class FeedStruct (
    val feed_id:Int,
    val des:String,
    val start_time:String,
    val end_time:String,
    val feed_img:String,
    val user_id:Int,
    val user_name:String,
    val user_img:String?,
    val isMine: Boolean
)

data class Comment8(
    val comment_id:Int,
    val comment:String,
    val created_at:String,
    val feed_id:Int,
    val user_id:Int,
    val user_name:String,
    val user_img:String?,
    val isMine:Boolean
)

data class CrewCard(
    val crew_id:Int,
    val img:String?,
    val title:String,
    val content:String,
    val memberCount:Int?,
    val gatheringCount:Int?
)

data class GatheringCard(
    val gathering_id:Int,
    val owner_id:Int,
    val title:String,
    val date : String,
    val isParticipation:Boolean,
    val memberCount: Int?,
    val isMine:Boolean,
    val temp:String? = null
)

data class MemberCard(
    val img:String?,
    val name:String
)