package tech.jhavidit.remindme.model

object RepeatHourModel {
    fun getRepeatingHours(): Array<String> {
        val list = Array<String>(23) { _ -> "" }
        for (i in 0..22) {
            list[i] = (i+1).toString()
        }
        return list
    }


}