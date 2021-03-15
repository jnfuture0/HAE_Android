package org.hoe.Json

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface RetrofitService {

    @GET("/user/")
    fun getUser(@Header("token")token:String): Call<User4>

    @GET("/user/login/{id}/{pw}/")
    fun login(
        @Path("id")id:String,
        @Path("pw")pw:String): Call<Token>

    @Multipart
    @PUT("/user/put/")
    fun modifyUser(@Header("token")token:String,
                   @Part("name") name:RequestBody,
                   @Part image:MultipartBody.Part?):Call<ResultString>

    @Multipart
    @POST("/user/create/")
    fun createUser(@Part("ID") id:RequestBody,
                   @Part("password") password:RequestBody,
                   @Part("name")name : RequestBody,
                   @Part image:MultipartBody.Part?):Call<ResultString>






    @GET("/feed/user/")
    fun getMyFeedList(@Header("token")token:String):Call<List<Feed9>>

    @Multipart
    @POST("/feed/create/")
    fun createFeed(@Header("token")token:String,
                   @Part("des") des:RequestBody,
                   @Part("start_time") start_time:RequestBody,
                   @Part("end_time") end_time:RequestBody,
                   @Part image:MultipartBody.Part?):Call<ResultString>

    @Multipart
    @PUT("/feed/put/{feed_id}/")
    fun modifyFeed(@Header("token")token:String,
                   @Path("feed_id")feed_id: Int,
                   @Part("des") des:RequestBody,
                   @Part("start_time") start_time:RequestBody,
                   @Part("end_time") end_time:RequestBody,
                   @Part image:MultipartBody.Part?) :Call<ResultString>

    @DELETE("/feed/delete/{feed_id}/")
    fun deleteFeed(@Header("token")token:String,
                   @Path("feed_id")feed_id:Int):Call<ResultString>



    @GET("/feed_comment/{feed_id}/")
    fun getFeedComment(
        @Path("feed_id")feed_id:Int
    ):Call<List<FeedComment>>


    @Multipart
    @POST("/feed_comment/create/{feed_ID}/")
    fun createFeedComment(@Header("token")token:String,
                          @Path("feed_ID") feed_id: Int,
                          @Part("comment") comment:RequestBody):Call<ResultString>

    @Multipart
    @PUT("/feed_comment/put/{feed_comment_id}/")
    fun modifyFeedComment(@Header("token")token:String,
                          @Path("feed_comment_id")feed_comment_id: Int,
                          @Part("comment") comment:RequestBody):Call<ResultString>

    @DELETE("/feed_comment/delete/{feed_comment_id}/")
    fun deleteFeedComment(@Header("token")token:String,
    @Path("feed_comment_id")feed_comment_id:Int):Call<ResultString>



    @GET("/crew/user/")
    fun getUserCrew(@Header("token")token:String):Call<List<Crew6>>

    @GET("/crew_header/")
    fun isCrewHeader(@Header("token")token:String):Call<ResultString>

    @Multipart
    @POST("/crew/user/create/")
    fun createCrew(@Header("token")token:String,
                   @Part("name")name:RequestBody,
                   @Part("des")des:RequestBody,
                   @Part image:MultipartBody.Part?):Call<ResultString>///////

    @PUT("/user/crew/put/{crew_id}/")
    fun joinCrew(@Header("token")token:String,
                 @Path("crew_id")crew_id : String):Call<ResultString>

    @GET("/feed/crew/")
    fun getCrewFeedList(@Header("token")token:String):Call<List<Feed9>>

    @GET("/crew/")
    fun getAllCrewList():Call<List<Crew6New>>

    @GET("/crew/{crew_name}/")
    fun getSearchCrewList(@Path("crew_name")crew_name:String):Call<List<Crew6New>>

    @DELETE("/crew/user/delete/")
    fun leaveCrew(@Header("token")token:String):Call<ResultString>

    @DELETE("/crew/header/delete/")
    fun deleteCrew(@Header("token")token:String):Call<ResultString>




    @Multipart
    @POST("/gathering/create/")
    fun createGathering(@Header("token")token:String,
                        @Part("name")name:RequestBody,
                        @Part("date")date:RequestBody) : Call<ResultString> ///

    @DELETE("/gathering/delete/{gathering_id}/")
    fun deleteGathering(@Header("token")token:String,
                        @Path("gathering_id")gathering_id:String):Call<ResultString>//

    @GET("/gathering/")
    fun getGathering(@Header("token")token:String):Call<List<Gathering8>>

    @GET("/gathering_comment/{gathering_id}/")
    fun getGatheringComment(@Path("gathering_id")gathering_id: String):Call<List<GatheringComment7>>

    @Multipart
    @POST("/gathering_comment/create/{gathering_id}/")
    fun createGatheringComment(@Header("token")token:String,
                               @Path("gathering_id") gathering_id: String,
                               @Part("comment")comment:RequestBody):Call<ResultString>

    @Multipart
    @PUT("/gathering_comment/put/{gathering_comment_id}/")
    fun modifyGatheringComment(@Header("token")token:String,
                               @Path("gathering_comment_id")gathering_comment_id:String,
                               @Part("comment")comment:RequestBody):Call<ResultString>

    @DELETE("/gathering_comment/delete/{gathering_comment_id}/")
    fun deleteGatheringComment(@Header("token")token:String,
                               @Path("gathering_comment_id")gathering_comment_id:String):Call<ResultString>

    @GET("/gathering_participant/{gathering_id}/")
    fun getGatheringParticipant(@Path("gathering_id")gathering_id: String):Call<List<User4>>

    @PUT("/gathering_participant/put/{gathering_id}/")
    fun joinGathering(@Header("token")token:String,
                      @Path("gathering_id")gathering_id: String):Call<ResultString>

    @DELETE("/gathering_participant/delete/{gathering_id}/")
    fun leaveGathering(@Header("token")token: String,
                       @Path("gathering_id")gathering_id: String):Call<ResultString>





    @Multipart
    @POST("/equipment/create/{location_x}/{location_y}/{category}/")
    fun createEquipment(@Header("token")token:String,
                        @Path("location_x")location_x:String,
                        @Path("location_y")location_y:String,
                        @Path("category")category:String,
                        @Part image:MultipartBody.Part?):Call<ResultString>///

    @DELETE("/equipment/delete/{equipment_id}/")
    fun deleteEquipment(@Header("token")token:String,
                        @Path("equipment_id")equipment_id:String):Call<ResultString>///

    @GET("/equipment/{location_x}/{location_y}/{distance}/")
    fun getEquipment(@Path("location_x")location_x:String,
                     @Path("location_y")location_y:String,
                     @Path("distance")distance2510:String):Call<List<Equipment6>>

    @GET("/equipment/{location_x}/{location_y}/{distance}/{category}/")
    fun getEquipmentWithCategory(@Path("location_x")location_x:String,
                                 @Path("location_y")location_y:String,
                                 @Path("distance")distance2510:String,
                                 @Path("category")category:String):Call<List<Equipment6>>

    @Multipart
    @POST("/equipment/judge/")
    fun judgeEquipment(@Part image:MultipartBody.Part?):Call<ResultInt>

}