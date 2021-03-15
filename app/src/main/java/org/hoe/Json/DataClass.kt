package org.hoe.Json

data class DataClass(var id:Int?=null)

data class User4(var ID:Int?=null, var name:String?=null, var image:String?=null, var crew_ID:Int?=null)

data class ResultString(var result:String?=null)

data class ResultInt(var result:Int?=null)

data class Feed9(var ID:Int?=null, var des:String?=null, var start_time:String?=null, var end_time:String?=null, var created_at:String?=null,
                 var image:String?=null, var user_ID:Int?=null, var user_ID__name:String?=null, var user_ID__image:String?=null)

data class Token(var token:String?=null)

data class FeedComment(var ID:Int?=null, var comment:String?=null, var created_at: String?=null, var feed_ID:Int?=null, var user_ID: Int?=null, var user_ID__name: String?=null, var user_ID__image: String?=null)

data class Crew6New(var ID: Int?=null, var name: String?=null, var des: String?=null, var image: String?=null, var count:Int?=null, var gathering_count: Int?=null)

data class Crew6(var ID: Int?=null, var name: String?=null, var des: String?=null, var image: String?=null, var mem_count:Int?=null, var gathering_count:Int?=null)

data class FeedIDComment(var feed_ID: Int?=null, var comment: String?=null)

data class Equipment6(var ID: Int?=null, var category:Int?=null, var location_X:Float?=null, var location_Y:Float?=null, var image: String?=null, var user_ID: Int?=null)

data class Gathering8(var ID: Int?=null, var name:String?=null, var created_at: String?=null, var date:String?=null, var crew_ID_id:Int?=null, var user_ID_id:Int?=null, var count: Int?=null, var participate:Int?=null)

data class GatheringComment7(var ID: Int?=null, var comment:String?=null, var created_at: String?=null, var gathering_ID:Int?=null, var user_ID: Int?=null, var user_ID__name: String?=null, var user_ID__image: String?=null)