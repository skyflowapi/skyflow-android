package Skyflow

@Description("Sets the padding for the collect element which takes all the left, top, right, bottom padding values.")
public class Padding(
    @Description("Value for left padding.")
    val left:Int=10,
    @Description("Value for top padding.")
    val top:Int=10,
    @Description("Value for right padding.")
    val right:Int=10,
    @Description("Value for bottom padding.")
    val bottom:Int=10
) {}