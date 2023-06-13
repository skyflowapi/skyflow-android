package Skyflow

@Description("This is the description for Callback interface")
interface Callback {

        @Description("This is the description for onSuccess function")
        fun onSuccess(
            @Description("Description for responseBody param")
            responseBody: Any
        )

        @Description("This is the description for onFailure function")
        fun onFailure(
            @Description("Description for exception param")
            exception: Any
        )
    }