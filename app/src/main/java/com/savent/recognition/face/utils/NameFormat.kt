package com.savent.recognition.face.utils

class NameFormat {

    companion object {
        fun format(name: String): String{
            val strBuilder = StringBuilder()
            try {
                if(name.isEmpty()) return strBuilder.toString()
                strBuilder.append(name[0].uppercaseChar())

                if (name.length < 2) return strBuilder.toString()
                strBuilder.append(name[1].lowercaseChar())

                for (i in 2 until name.length){
                    if(name[i-1].isWhitespace()) strBuilder.append(name[i].uppercaseChar())
                    else strBuilder.append(name[i].lowercaseChar())
                }
            }catch (e:Exception){

            }
            return strBuilder.toString()
        }
    }
}