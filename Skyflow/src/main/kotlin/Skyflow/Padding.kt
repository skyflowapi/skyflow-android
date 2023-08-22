package Skyflow

@Description("Padding for Collect Elements. Accepts left, top, right, and bottom padding values.")
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