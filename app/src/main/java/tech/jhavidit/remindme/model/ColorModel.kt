package tech.jhavidit.remindme.model

object ColorModel {
    fun getColorList(): List<String> {
        return listOf("#FFFFFF", "#D5FFE6", "#FFD5FD", "#FCFFD5", "#D5D7FF","#C3DBD9","#EFEFEF","#FDCEB9")
    }
    fun getColorIndex(color:String):Int{
        val colorList = getColorList();
        for(i in colorList.indices){
            if(colorList[i] ==color)
                return i;
        }
        return -1;
    }

}

