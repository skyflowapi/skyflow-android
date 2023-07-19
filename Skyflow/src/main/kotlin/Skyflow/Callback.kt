package Skyflow

@Description("Contains the results of the implementation of callback.")
interface Callback {

        @Description("Implementation when callback results in success.")
        fun onSuccess(
            @Description("The success response.")
            responseBody: Any
        )

        @Description("Implementation when callback results in failure.")
        fun onFailure(
            @Description("The failure response.")
            exception: Any
        )
    }