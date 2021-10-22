package tech.jhavidit.remindme.model

object ColorModel {
    fun getColorList(): List<String> {
        return listOf("#FFFFFF", "#D5FFE6", "#FFD5FD", "#FCFFD5", "#D5D7FF", "#cabbe9", "#6c5fa7", "#e77c7c", "#293462", "#5be7c4","#1a2639")
    }
    fun getColorIndex(color:String):Int{
        val colorList = getColorList();
        for(i in 0..colorList.size){
            if(colorList[i] ==color)
                return i;
        }
        return -1;
    }

}

